package com.akhara.data.repository

import com.akhara.data.db.AkharaDatabase
import com.akhara.data.db.entity.BodyWeight
import com.akhara.data.db.entity.Exercise
import com.akhara.data.db.entity.PlannedExercise
import com.akhara.data.db.entity.UserSettings
import com.akhara.data.db.entity.WeeklyPlan
import com.akhara.data.db.entity.WorkoutSession
import com.akhara.data.db.entity.WorkoutSet
import kotlinx.coroutines.flow.Flow
import java.time.DayOfWeek
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId
import java.time.temporal.TemporalAdjusters

class WorkoutRepository(private val db: AkharaDatabase) {

    private val exerciseDao = db.exerciseDao()
    private val workoutDao = db.workoutDao()
    private val planDao = db.planDao()
    private val settingsDao = db.settingsDao()
    private val plannedExerciseDao = db.plannedExerciseDao()
    private val bodyWeightDao = db.bodyWeightDao()

    // ---- Exercises ----
    fun getAllExercises(): Flow<List<Exercise>> = exerciseDao.getAllExercises()

    fun getExercisesByMuscleGroup(muscleGroup: String): Flow<List<Exercise>> =
        exerciseDao.getExercisesByMuscleGroup(muscleGroup)

    fun getExercisesByMuscleGroups(groups: List<String>): Flow<List<Exercise>> =
        exerciseDao.getExercisesByMuscleGroups(groups)

    fun searchExercises(query: String): Flow<List<Exercise>> =
        exerciseDao.searchExercises(query)

    suspend fun addCustomExercise(name: String, muscleGroup: String) {
        exerciseDao.insertExercise(Exercise(name = name, muscleGroup = muscleGroup, isCustom = true))
    }

    suspend fun getExerciseById(id: Int): Exercise? {
        var result: Exercise? = null
        getAllExercises().collect { list ->
            result = list.find { it.id == id }
        }
        return result
    }

    // ---- Workout Sessions ----
    fun getAllSessions(): Flow<List<WorkoutSession>> = workoutDao.getAllSessions()

    fun getSessionsForDateRange(startDate: Long, endDate: Long): Flow<List<WorkoutSession>> =
        workoutDao.getSessionsBetween(startDate, endDate)

    fun getSetsForSession(sessionId: Int): Flow<List<WorkoutSet>> =
        workoutDao.getSetsForSession(sessionId)

    suspend fun getSetsForSessionSync(sessionId: Int): List<WorkoutSet> =
        workoutDao.getSetsForSessionSync(sessionId)

    suspend fun saveWorkout(session: WorkoutSession, sets: List<WorkoutSet>): Long {
        val sessionId = workoutDao.insertSession(session)
        val setsWithSessionId = sets.map { it.copy(sessionId = sessionId.toInt()) }
        workoutDao.insertSets(setsWithSessionId)
        return sessionId
    }

    suspend fun updateWorkout(session: WorkoutSession, sets: List<WorkoutSet>) {
        workoutDao.updateSession(session)
        workoutDao.deleteSetsForSession(session.id)
        val setsWithSessionId = sets.map { it.copy(sessionId = session.id) }
        workoutDao.insertSets(setsWithSessionId)
    }

    suspend fun getSessionById(sessionId: Int): WorkoutSession? =
        workoutDao.getSessionsBetweenSync(0, Long.MAX_VALUE).find { it.id == sessionId }

    suspend fun getExerciseByIdSync(id: Int): Exercise? =
        exerciseDao.getExerciseById(id)

    suspend fun deleteWorkout(session: WorkoutSession) {
        workoutDao.deleteSession(session)
    }

    // ---- Weekly Plan ----
    fun getWeeklyPlan(): Flow<List<WeeklyPlan>> = planDao.getWeeklyPlan()

    suspend fun getPlanForDay(dayOfWeek: Int): WeeklyPlan? = planDao.getPlanForDay(dayOfWeek)

    suspend fun saveDayPlan(dayOfWeek: Int, muscleGroups: List<String>) {
        planDao.insertPlan(WeeklyPlan(dayOfWeek = dayOfWeek, muscleGroups = muscleGroups.joinToString(",")))
    }

    // ---- Planned Exercises ----
    suspend fun getPlannedExercisesForDay(dayOfWeek: Int): List<PlannedExercise> =
        plannedExerciseDao.getPlannedForDay(dayOfWeek)

    fun getAllPlannedExercises(): Flow<List<PlannedExercise>> =
        plannedExerciseDao.getAllPlanned()

    suspend fun savePlannedExercises(dayOfWeek: Int, exercises: List<PlannedExercise>) {
        plannedExerciseDao.deletePlannedForDay(dayOfWeek)
        if (exercises.isNotEmpty()) {
            plannedExerciseDao.insertPlanned(exercises.map { it.copy(dayOfWeek = dayOfWeek) })
        }
    }

    suspend fun clearPlannedDay(dayOfWeek: Int) {
        plannedExerciseDao.deletePlannedForDay(dayOfWeek)
    }

    // ---- Body Weight ----
    suspend fun logBodyWeight(weightKg: Float, date: Long = todayEpochMillis()) {
        bodyWeightDao.insert(BodyWeight(weightKg = weightKg, date = date))
    }

    fun getAllBodyWeights(): Flow<List<BodyWeight>> = bodyWeightDao.getAll()

    suspend fun getLatestBodyWeight(): BodyWeight? = bodyWeightDao.getLatest()

    suspend fun getBodyWeightsForRange(startDate: Long, endDate: Long): List<BodyWeight> =
        bodyWeightDao.getForRange(startDate, endDate)

    // ---- Settings ----
    fun getSettings(): Flow<UserSettings?> = settingsDao.getSettings()

    suspend fun getSettingsSync(): UserSettings? = settingsDao.getSettingsSync()

    suspend fun updateWeeklyGoal(goal: Int) {
        settingsDao.insertSettings(UserSettings(weeklyGoal = goal))
    }

    // ---- Stats ----
    suspend fun getWorkoutCountForWeek(date: LocalDate = LocalDate.now()): Int {
        val (start, end) = weekBounds(date)
        return workoutDao.getSessionCountBetween(start, end)
    }

    suspend fun getAvgRestTimeForRange(startDate: Long, endDate: Long): Float? =
        workoutDao.getAvgRestTimeBetween(startDate, endDate)

    suspend fun getTotalVolumeForRange(startDate: Long, endDate: Long): Float? =
        workoutDao.getTotalVolumeBetween(startDate, endDate)

    suspend fun getDailyVolume(dayStart: Long, dayEnd: Long): Float? =
        workoutDao.getDailyVolume(dayStart, dayEnd)

    suspend fun getMostTrainedMuscleGroup(startDate: Long, endDate: Long): String? =
        workoutDao.getMostTrainedMuscleGroup(startDate, endDate)

    suspend fun getMaxWeightForExercise(exerciseId: Int): Float? =
        workoutDao.getMaxWeightForExercise(exerciseId)

    // ---- Analytics Queries ----
    suspend fun getPlannedSetsForSession(sessionId: Int): List<WorkoutSet> =
        workoutDao.getPlannedSetsForSession(sessionId)

    suspend fun getExerciseSetsInRange(exerciseId: Int, start: Long, end: Long): List<WorkoutSet> =
        workoutDao.getExerciseSetsInRange(exerciseId, start, end)

    suspend fun getSessionExerciseIds(sessionId: Int): List<Int> =
        workoutDao.getSessionExerciseIds(sessionId)

    suspend fun getRecentSessionsForMuscleGroup(muscleGroup: String, limit: Int): List<WorkoutSession> =
        workoutDao.getRecentSessionsForMuscleGroup(muscleGroup, limit)

    suspend fun getRecentSessions(limit: Int): List<WorkoutSession> =
        workoutDao.getRecentSessions(limit)

    suspend fun getMaxWeightForExerciseInSession(exerciseId: Int, sessionId: Int): Float? =
        workoutDao.getMaxWeightForExerciseInSession(exerciseId, sessionId)

    suspend fun getSessionsBetweenSync(startDate: Long, endDate: Long): List<WorkoutSession> =
        workoutDao.getSessionsBetweenSync(startDate, endDate)

    // ---- Streak Calculation ----
    suspend fun calculateStreak(): Pair<Int, Int> {
        val settings = settingsDao.getSettingsSync() ?: UserSettings()
        val goal = settings.weeklyGoal
        var currentStreak = 0
        var longestStreak = 0
        var tempStreak = 0

        var weekStart = LocalDate.now().with(TemporalAdjusters.previousOrSame(DayOfWeek.MONDAY))

        for (i in 0 until 104) {
            val (start, end) = weekBounds(weekStart)
            val count = workoutDao.getSessionCountBetween(start, end)
            if (count >= goal) {
                tempStreak++
                if (tempStreak > longestStreak) longestStreak = tempStreak
            } else {
                if (i == 0) {
                    // Current week in progress, don't break streak yet
                } else {
                    if (tempStreak > 0 && currentStreak == 0) currentStreak = tempStreak
                    tempStreak = 0
                }
            }
            weekStart = weekStart.minusWeeks(1)
        }

        if (currentStreak == 0) currentStreak = tempStreak

        return Pair(currentStreak, longestStreak)
    }

    // ---- Helpers ----
    fun weekBounds(date: LocalDate): Pair<Long, Long> {
        val monday = date.with(TemporalAdjusters.previousOrSame(DayOfWeek.MONDAY))
        val sunday = monday.plusDays(6)
        val zone = ZoneId.systemDefault()
        val start = monday.atStartOfDay(zone).toInstant().toEpochMilli()
        val end = sunday.atTime(23, 59, 59).atZone(zone).toInstant().toEpochMilli()
        return Pair(start, end)
    }

    fun dayBounds(date: LocalDate): Pair<Long, Long> {
        val zone = ZoneId.systemDefault()
        val start = date.atStartOfDay(zone).toInstant().toEpochMilli()
        val end = date.atTime(23, 59, 59).atZone(zone).toInstant().toEpochMilli()
        return Pair(start, end)
    }

    fun todayEpochMillis(): Long =
        LocalDate.now().atStartOfDay(ZoneId.systemDefault()).toInstant().toEpochMilli()

    fun dateToEpochMillis(date: LocalDate): Long =
        date.atStartOfDay(ZoneId.systemDefault()).toInstant().toEpochMilli()

    fun epochMillisToDate(millis: Long): LocalDate =
        Instant.ofEpochMilli(millis).atZone(ZoneId.systemDefault()).toLocalDate()
}
