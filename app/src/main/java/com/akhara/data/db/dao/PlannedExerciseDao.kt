package com.akhara.data.db.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.akhara.data.db.entity.PlannedExercise
import kotlinx.coroutines.flow.Flow

@Dao
interface PlannedExerciseDao {

    @Query("SELECT * FROM planned_exercises WHERE dayOfWeek = :dayOfWeek ORDER BY orderIndex")
    suspend fun getPlannedForDay(dayOfWeek: Int): List<PlannedExercise>

    @Query("SELECT * FROM planned_exercises ORDER BY dayOfWeek, orderIndex")
    fun getAllPlanned(): Flow<List<PlannedExercise>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertPlanned(exercises: List<PlannedExercise>)

    @Query("DELETE FROM planned_exercises WHERE dayOfWeek = :dayOfWeek")
    suspend fun deletePlannedForDay(dayOfWeek: Int)

    @Query("DELETE FROM planned_exercises")
    suspend fun deleteAll()
}
