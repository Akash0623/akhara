package com.akhara.data.db.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Transaction
import androidx.room.Update
import com.akhara.data.db.entity.SessionWithSets
import com.akhara.data.db.entity.WorkoutSession
import com.akhara.data.db.entity.WorkoutSet
import kotlinx.coroutines.flow.Flow

@Dao
interface WorkoutDao {

    @Insert
    suspend fun insertSession(session: WorkoutSession): Long

    @Insert
    suspend fun insertSets(sets: List<WorkoutSet>)

    @Update
    suspend fun updateSession(session: WorkoutSession)

    @Delete
    suspend fun deleteSession(session: WorkoutSession)

    @Query("SELECT * FROM workout_sessions ORDER BY date DESC")
    fun getAllSessions(): Flow<List<WorkoutSession>>

    @Query("SELECT * FROM workout_sessions WHERE date BETWEEN :startDate AND :endDate ORDER BY date DESC")
    fun getSessionsBetween(startDate: Long, endDate: Long): Flow<List<WorkoutSession>>

    @Query("SELECT * FROM workout_sessions WHERE date BETWEEN :startDate AND :endDate")
    suspend fun getSessionsBetweenSync(startDate: Long, endDate: Long): List<WorkoutSession>

    @Query("SELECT * FROM workout_sets WHERE sessionId = :sessionId ORDER BY exerciseId, setNumber")
    fun getSetsForSession(sessionId: Int): Flow<List<WorkoutSet>>

    @Query("SELECT * FROM workout_sets WHERE sessionId = :sessionId ORDER BY exerciseId, setNumber")
    suspend fun getSetsForSessionSync(sessionId: Int): List<WorkoutSet>

    @Transaction
    @Query("SELECT * FROM workout_sessions WHERE id = :sessionId")
    fun getSessionWithSets(sessionId: Int): Flow<SessionWithSets>

    @Query("SELECT COUNT(*) FROM workout_sessions WHERE date BETWEEN :startDate AND :endDate")
    suspend fun getSessionCountBetween(startDate: Long, endDate: Long): Int

    @Query("SELECT COUNT(DISTINCT date) FROM workout_sessions WHERE date BETWEEN :startDate AND :endDate")
    suspend fun getUniqueDayCountBetween(startDate: Long, endDate: Long): Int

    @Query(
        "SELECT AVG(restSeconds) FROM workout_sets WHERE sessionId IN " +
        "(SELECT id FROM workout_sessions WHERE date BETWEEN :startDate AND :endDate) AND restSeconds > 0"
    )
    suspend fun getAvgRestTimeBetween(startDate: Long, endDate: Long): Float?

    @Query(
        "SELECT SUM(reps * weight) FROM workout_sets WHERE sessionId IN " +
        "(SELECT id FROM workout_sessions WHERE date BETWEEN :startDate AND :endDate)"
    )
    suspend fun getTotalVolumeBetween(startDate: Long, endDate: Long): Float?

    @Query("SELECT MAX(weight) FROM workout_sets WHERE exerciseId = :exerciseId AND reps >= 1")
    suspend fun getMaxWeightForExercise(exerciseId: Int): Float?

    @Query("DELETE FROM workout_sets WHERE sessionId = :sessionId")
    suspend fun deleteSetsForSession(sessionId: Int)

    @Query("DELETE FROM workout_sets WHERE sessionId = :sessionId AND exerciseId = :exerciseId")
    suspend fun deleteSetsForExercise(sessionId: Int, exerciseId: Int)

    @Query(
        "SELECT e.muscleGroup FROM workout_sets ws " +
        "JOIN exercises e ON ws.exerciseId = e.id " +
        "WHERE ws.sessionId IN (SELECT id FROM workout_sessions WHERE date BETWEEN :startDate AND :endDate) " +
        "GROUP BY e.muscleGroup ORDER BY COUNT(*) DESC LIMIT 1"
    )
    suspend fun getMostTrainedMuscleGroup(startDate: Long, endDate: Long): String?

    @Query(
        "SELECT SUM(reps * weight) FROM workout_sets WHERE sessionId IN " +
        "(SELECT id FROM workout_sessions WHERE date BETWEEN :dayStart AND :dayEnd)"
    )
    suspend fun getDailyVolume(dayStart: Long, dayEnd: Long): Float?

    @Query("SELECT * FROM workout_sets WHERE sessionId = :sessionId AND plannedReps IS NOT NULL")
    suspend fun getPlannedSetsForSession(sessionId: Int): List<WorkoutSet>

    @Query(
        "SELECT * FROM workout_sets WHERE exerciseId = :exerciseId " +
        "AND sessionId IN (SELECT id FROM workout_sessions WHERE date BETWEEN :start AND :end) " +
        "ORDER BY sessionId, setNumber"
    )
    suspend fun getExerciseSetsInRange(exerciseId: Int, start: Long, end: Long): List<WorkoutSet>

    @Query("SELECT DISTINCT exerciseId FROM workout_sets WHERE sessionId = :sessionId")
    suspend fun getSessionExerciseIds(sessionId: Int): List<Int>

    @Query(
        "SELECT ws.* FROM workout_sets ws " +
        "JOIN exercises e ON ws.exerciseId = e.id " +
        "WHERE e.muscleGroup = :muscleGroup " +
        "AND ws.sessionId IN (SELECT id FROM workout_sessions ORDER BY date DESC) " +
        "ORDER BY ws.sessionId DESC LIMIT 1"
    )
    suspend fun getLatestSetForMuscleGroup(muscleGroup: String): WorkoutSet?

    @Query(
        "SELECT s.* FROM workout_sessions s " +
        "JOIN workout_sets ws ON ws.sessionId = s.id " +
        "JOIN exercises e ON ws.exerciseId = e.id " +
        "WHERE e.muscleGroup = :muscleGroup " +
        "GROUP BY s.id ORDER BY s.date DESC LIMIT :limit"
    )
    suspend fun getRecentSessionsForMuscleGroup(muscleGroup: String, limit: Int): List<WorkoutSession>

    @Query("SELECT * FROM workout_sessions ORDER BY date DESC LIMIT :limit")
    suspend fun getRecentSessions(limit: Int): List<WorkoutSession>

    @Query(
        "SELECT MAX(weight) as maxWeight FROM workout_sets " +
        "WHERE exerciseId = :exerciseId AND reps >= 1 AND sessionId = :sessionId"
    )
    suspend fun getMaxWeightForExerciseInSession(exerciseId: Int, sessionId: Int): Float?

    @Query(
        "SELECT * FROM workout_sets WHERE exerciseId = :exerciseId " +
        "AND sessionId = (" +
        "  SELECT ws.sessionId FROM workout_sets ws " +
        "  JOIN workout_sessions s ON s.id = ws.sessionId " +
        "  WHERE ws.exerciseId = :exerciseId " +
        "  ORDER BY s.date DESC, s.id DESC LIMIT 1" +
        ") ORDER BY setNumber"
    )
    suspend fun getLastSetsForExercise(exerciseId: Int): List<WorkoutSet>

    @Query("SELECT * FROM workout_sessions WHERE id = :sessionId")
    suspend fun getSessionByIdDirect(sessionId: Int): WorkoutSession?

    @Transaction
    suspend fun replaceSetsForExercise(sessionId: Int, exerciseId: Int, sets: List<WorkoutSet>) {
        deleteSetsForExercise(sessionId, exerciseId)
        insertSets(sets)
    }

    @Transaction
    suspend fun replaceAllSetsForSession(session: WorkoutSession, sets: List<WorkoutSet>) {
        updateSession(session)
        deleteSetsForSession(session.id)
        insertSets(sets)
    }
}
