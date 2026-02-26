package com.akhara.data.intelligence

import com.akhara.data.MuscleGroup
import com.akhara.data.db.entity.PlannedExercise
import com.akhara.data.repository.WorkoutRepository
import java.time.DayOfWeek
import java.time.LocalDate
import java.time.temporal.ChronoUnit
import java.time.temporal.TemporalAdjusters

class InsightEngine(private val repository: WorkoutRepository) {

    suspend fun generateAllInsights(): List<Insight> {
        val insights = mutableListOf<Insight>()
        try {
            insights.addAll(muscleRecoveryStatus())
            calculateFatigueScore()?.let { insights.add(it) }
            weeklyAdherence()?.let { insights.add(it) }
            insights.addAll(overloadSuggestions())
            insights.addAll(detectPlateaus())
            insights.addAll(volumeBalance())

            val recentSessions = repository.getRecentSessions(1)
            if (recentSessions.isNotEmpty()) {
                sessionCompletionRate(recentSessions.first().id)?.let { insights.add(it) }
            }

            val todayDow = LocalDate.now().dayOfWeek.value
            val planned = repository.getPlannedExercisesForDay(todayDow)
            if (planned.isNotEmpty()) {
                predictDuration(planned)?.let { insights.add(it) }
            }

            insights.addAll(detectSubstitutions())
        } catch (_: Exception) { }
        return insights.sortedBy { it.priority }
    }

    suspend fun prCountdown(exerciseId: Int): Insight? {
        val currentPR = repository.getMaxWeightForExercise(exerciseId) ?: return null

        val now = LocalDate.now()
        val sixWeeksAgo = now.minusWeeks(6)
        val (start, _) = repository.dayBounds(sixWeeksAgo)
        val (_, end) = repository.dayBounds(now)
        val sets = repository.getExerciseSetsInRange(exerciseId, start, end)
        if (sets.size < 3) return null

        val sessions = sets.map { it.sessionId }.distinct()
        val points = sessions.mapIndexedNotNull { idx, sessionId ->
            val maxW = repository.getMaxWeightForExerciseInSession(exerciseId, sessionId)
            if (maxW != null && maxW > 0f) Pair(idx.toFloat(), maxW) else null
        }
        if (points.size < 3) return null

        val (slope, intercept) = MathUtils.linearRegression(points)
        if (slope <= 0f) return null

        val lastX = points.last().first
        val predicted = slope * (lastX + 1) + intercept
        val gap = currentPR - predicted
        if (gap <= 0f) return null

        val sessionsToGo = (gap / slope).toInt().coerceAtLeast(1)
        return Insight(
            type = InsightType.PR_COUNTDOWN,
            title = "PR Countdown",
            message = "~${String.format("%.1f", gap)}kg to PR. At your rate: ~$sessionsToGo sessions away",
            priority = 2,
            exerciseId = exerciseId,
            metadata = mapOf("gap" to gap, "sessionsToGo" to sessionsToGo)
        )
    }

    suspend fun muscleRecoveryStatus(): List<Insight> {
        val insights = mutableListOf<Insight>()
        val now = System.currentTimeMillis()

        for (group in MuscleGroup.entries) {
            val sessions = repository.getRecentSessionsForMuscleGroup(group.displayName, 1)
            if (sessions.isEmpty()) continue

            val lastDate = repository.epochMillisToDate(sessions.first().date)
            val hoursSince = ChronoUnit.HOURS.between(lastDate.atStartOfDay(), LocalDate.now().atStartOfDay())

            val status = when {
                hoursSince < 24 -> "just trained"
                hoursSince < 48 -> "recovering"
                hoursSince < 72 -> "recovered"
                else -> "fully ready"
            }
            val priority = if (hoursSince < 48) 2 else 3

            insights.add(
                Insight(
                    type = InsightType.MUSCLE_RECOVERY,
                    title = "${group.displayName} Recovery",
                    message = "${group.displayName}: $status (${hoursSince}h ago)",
                    priority = priority,
                    metadata = mapOf("muscleGroup" to group.displayName, "hoursSince" to hoursSince, "status" to status)
                )
            )
        }
        return insights
    }

    suspend fun calculateFatigueScore(): Insight? {
        val sessions = repository.getRecentSessions(3)
        if (sessions.isEmpty()) return null

        val completionRates = sessions.map { session ->
            val sets = repository.getSetsForSessionSync(session.id)
            val planned = sets.filter { it.plannedReps != null }
            if (planned.isEmpty()) return@map 1f
            planned.count { it.reps >= (it.plannedReps ?: 0) }.toFloat() / planned.size
        }

        val weights = listOf(0.5f, 0.3f, 0.2f).take(completionRates.size)
        val avgCompletion = MathUtils.weightedAverage(completionRates, weights)

        val score = ((1f - avgCompletion) * 10f).coerceIn(1f, 10f).toInt()
        val label = when {
            score <= 3 -> "fresh"
            score <= 6 -> "normal"
            score <= 8 -> "fatigued"
            else -> "overreached"
        }

        val priority = if (score >= 7) 1 else 2

        return Insight(
            type = if (score >= 8) InsightType.DELOAD_SUGGESTION else InsightType.FATIGUE_SCORE,
            title = "Fatigue Score",
            message = "Fatigue: $score/10 ($label). Recent completion: ${(avgCompletion * 100).toInt()}%",
            priority = priority,
            metadata = mapOf("score" to score, "completion" to avgCompletion)
        )
    }

    suspend fun predictDuration(plannedExercises: List<PlannedExercise>): Insight? {
        val totalSets = plannedExercises.sumOf { it.targetSets }
        if (totalSets == 0) return null

        val recentSessions = repository.getRecentSessions(5)
        if (recentSessions.isNotEmpty()) {
            val durations = recentSessions.mapNotNull { s ->
                if (s.totalDurationMin > 0) s.totalDurationMin else null
            }
            if (durations.isNotEmpty()) {
                val avgDuration = durations.average().toInt()
                return Insight(
                    type = InsightType.DURATION_PREDICTION,
                    title = "Estimated Duration",
                    message = "${plannedExercises.size} exercises, $totalSets sets. ~${avgDuration} min",
                    priority = 3,
                    metadata = mapOf("estimatedMinutes" to avgDuration, "totalSets" to totalSets)
                )
            }
        }

        val estimatedMinPerSet = 2
        val avgRestSec = 90
        val estimated = totalSets * estimatedMinPerSet + (totalSets * avgRestSec / 60)
        return Insight(
            type = InsightType.DURATION_PREDICTION,
            title = "Estimated Duration",
            message = "${plannedExercises.size} exercises, $totalSets sets. ~${estimated} min (est.)",
            priority = 3,
            metadata = mapOf("estimatedMinutes" to estimated, "totalSets" to totalSets)
        )
    }

    suspend fun restTimeAdvice(exerciseId: Int): Insight? {
        val now = LocalDate.now()
        val eightWeeksAgo = now.minusWeeks(8)
        val (start, _) = repository.dayBounds(eightWeeksAgo)
        val (_, end) = repository.dayBounds(now)
        val sets = repository.getExerciseSetsInRange(exerciseId, start, end)
            .filter { it.restSeconds > 0 && it.plannedReps != null }
        if (sets.size < 10) return null

        data class Bucket(val label: String, val range: IntRange)
        val buckets = listOf(
            Bucket("0-60s", 0..60),
            Bucket("60-90s", 61..90),
            Bucket("90-120s", 91..120),
            Bucket("120s+", 121..Int.MAX_VALUE)
        )

        var bestBucket = ""
        var bestRate = 0f
        var worstBucket = ""
        var worstRate = 1f

        for (bucket in buckets) {
            val bucketSets = sets.filter { it.restSeconds in bucket.range }
            if (bucketSets.size < 3) continue
            val completionRate = bucketSets.count { it.reps >= (it.plannedReps ?: 0) }.toFloat() / bucketSets.size
            if (completionRate > bestRate) { bestRate = completionRate; bestBucket = bucket.label }
            if (completionRate < worstRate) { worstRate = completionRate; worstBucket = bucket.label }
        }

        if (bestBucket.isEmpty()) return null

        return Insight(
            type = InsightType.REST_ADVISOR,
            title = "Rest Time Insight",
            message = "${(bestRate * 100).toInt()}% completion at $bestBucket rest, ${(worstRate * 100).toInt()}% at $worstBucket",
            priority = 3,
            exerciseId = exerciseId,
            metadata = mapOf("bestBucket" to bestBucket, "bestRate" to bestRate)
        )
    }

    suspend fun volumeBalance(): List<Insight> {
        val insights = mutableListOf<Insight>()
        val now = LocalDate.now()
        val fourWeeksAgo = now.minusWeeks(4)
        val (start, _) = repository.dayBounds(fourWeeksAgo)
        val (_, end) = repository.dayBounds(now)

        val pushGroups = setOf("Chest", "Shoulders", "Triceps")
        val pullGroups = setOf("Back", "Biceps", "Forearms")

        var pushVolume = 0f
        var pullVolume = 0f

        for (group in MuscleGroup.entries) {
            val sessions = repository.getRecentSessionsForMuscleGroup(group.displayName, 10)
            sessions.forEach { session ->
                val sets = repository.getSetsForSessionSync(session.id)
                val vol = sets.filter { set ->
                    val exerciseSets = repository.getExerciseSetsInRange(set.exerciseId, start, end)
                    exerciseSets.any { it.id == set.id }
                }.sumOf { (it.reps * it.weight).toDouble() }.toFloat()

                if (group.displayName in pushGroups) pushVolume += vol
                if (group.displayName in pullGroups) pullVolume += vol
            }
        }

        if (pushVolume > 0f && pullVolume > 0f) {
            val ratio = pushVolume / pullVolume
            if (ratio > 1.5f) {
                insights.add(Insight(
                    type = InsightType.VOLUME_BALANCE,
                    title = "Push/Pull Imbalance",
                    message = "Push volume is ${String.format("%.1f", ratio)}x your Pull volume. Add more rows/pullups.",
                    priority = 2,
                    metadata = mapOf("pushPullRatio" to ratio)
                ))
            } else if (ratio < 0.67f) {
                insights.add(Insight(
                    type = InsightType.VOLUME_BALANCE,
                    title = "Push/Pull Imbalance",
                    message = "Pull volume is ${String.format("%.1f", 1f / ratio)}x your Push volume. Add more pressing movements.",
                    priority = 2,
                    metadata = mapOf("pushPullRatio" to ratio)
                ))
            }
        }

        return insights
    }

    suspend fun detectPlateaus(): List<Insight> {
        val insights = mutableListOf<Insight>()
        val sessions = repository.getRecentSessions(20)
        if (sessions.size < 4) return insights

        val exerciseIds = mutableSetOf<Int>()
        sessions.forEach { exerciseIds.addAll(repository.getSessionExerciseIds(it.id)) }

        for (exerciseId in exerciseIds) {
            val maxWeights = sessions.mapNotNull { s ->
                repository.getMaxWeightForExerciseInSession(exerciseId, s.id)?.let { s.id to it }
            }
            if (maxWeights.size < 4) continue

            val recent4 = maxWeights.take(4).map { it.second }
            val maxRecent = recent4.max()
            val minRecent = recent4.min()

            if (maxRecent - minRecent < 1f && maxRecent > 0f) {
                insights.add(Insight(
                    type = InsightType.PLATEAU_DETECTED,
                    title = "Plateau Detected",
                    message = "Weight stuck at ${maxRecent.toInt()}kg for 4+ sessions. Try a deload or variation.",
                    priority = 2,
                    exerciseId = exerciseId,
                    metadata = mapOf("stuckWeight" to maxRecent)
                ))
            }
        }
        return insights
    }

    suspend fun overloadSuggestions(): List<Insight> {
        val insights = mutableListOf<Insight>()
        val sessions = repository.getRecentSessions(10)
        if (sessions.size < 2) return insights

        val exerciseIds = mutableSetOf<Int>()
        sessions.forEach { exerciseIds.addAll(repository.getSessionExerciseIds(it.id)) }

        for (exerciseId in exerciseIds) {
            var consecutiveComplete = 0
            var lastWeight = 0f

            for (session in sessions) {
                val sets = repository.getSetsForSessionSync(session.id)
                    .filter { it.exerciseId == exerciseId && it.plannedReps != null }
                if (sets.isEmpty()) continue

                val allComplete = sets.all { it.reps >= (it.plannedReps ?: 0) }
                if (allComplete) {
                    consecutiveComplete++
                    if (lastWeight == 0f) lastWeight = sets.maxOf { it.weight }
                } else {
                    break
                }
            }

            if (consecutiveComplete >= 2 && lastWeight > 0f) {
                val increment = 2.5f
                insights.add(Insight(
                    type = InsightType.PROGRESSIVE_OVERLOAD,
                    title = "Ready to Increase",
                    message = "Completed plan for $consecutiveComplete sessions. Try ${lastWeight + increment}kg",
                    priority = 1,
                    exerciseId = exerciseId,
                    metadata = mapOf("currentWeight" to lastWeight, "suggestedWeight" to (lastWeight + increment))
                ))
            }
        }
        return insights
    }

    suspend fun sessionCompletionRate(sessionId: Int): Insight? {
        val sets = repository.getSetsForSessionSync(sessionId)
        val planned = sets.filter { it.plannedReps != null }
        if (planned.isEmpty()) return null

        val completed = planned.count { it.reps >= (it.plannedReps ?: 0) }
        val rate = (completed.toFloat() / planned.size * 100).toInt()

        return Insight(
            type = InsightType.COMPLETION_RATE,
            title = "Session Completion",
            message = "Last session: $rate% ($completed/${planned.size} planned sets hit)",
            priority = 2,
            metadata = mapOf("rate" to rate, "completed" to completed, "total" to planned.size)
        )
    }

    suspend fun weeklyAdherence(): Insight? {
        val now = LocalDate.now()
        val monday = now.with(TemporalAdjusters.previousOrSame(DayOfWeek.MONDAY))
        val (weekStart, weekEnd) = repository.weekBounds(monday)

        val sessions = repository.getSessionsBetweenSync(weekStart, weekEnd)
        val daysWorkedOut = sessions.map { repository.epochMillisToDate(it.date).dayOfWeek.value }.distinct()

        var plannedDays = 0
        var completedDays = 0
        for (dow in 1..7) {
            val planned = repository.getPlannedExercisesForDay(dow)
            if (planned.isNotEmpty()) {
                plannedDays++
                if (dow in daysWorkedOut) completedDays++
            }
        }

        if (plannedDays == 0) return null

        val rate = (completedDays.toFloat() / plannedDays * 100).toInt()
        return Insight(
            type = InsightType.ADHERENCE,
            title = "Weekly Adherence",
            message = "This week: $completedDays/$plannedDays planned workouts. $rate% adherence.",
            priority = 2,
            metadata = mapOf("completedDays" to completedDays, "plannedDays" to plannedDays, "rate" to rate)
        )
    }

    suspend fun detectSubstitutions(): List<Insight> {
        val insights = mutableListOf<Insight>()
        val sessions = repository.getRecentSessions(8)
        if (sessions.size < 3) return insights

        for (dow in 1..7) {
            val planned = repository.getPlannedExercisesForDay(dow)
            if (planned.isEmpty()) continue

            val sessionsOnDay = sessions.filter {
                repository.epochMillisToDate(it.date).dayOfWeek.value == dow
            }
            if (sessionsOnDay.size < 2) continue

            for (pe in planned) {
                var skippedCount = 0
                sessionsOnDay.take(4).forEach { session ->
                    val exerciseIds = repository.getSessionExerciseIds(session.id)
                    if (pe.exerciseId !in exerciseIds) skippedCount++
                }

                if (skippedCount >= 3) {
                    insights.add(Insight(
                        type = InsightType.EXERCISE_SUBSTITUTION,
                        title = "Frequently Skipped",
                        message = "A planned exercise was skipped $skippedCount times. Update your plan?",
                        priority = 3,
                        exerciseId = pe.exerciseId,
                        metadata = mapOf("skippedCount" to skippedCount, "dayOfWeek" to dow)
                    ))
                }
            }
        }
        return insights
    }
}
