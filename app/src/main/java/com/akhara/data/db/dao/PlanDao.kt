package com.akhara.data.db.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.akhara.data.db.entity.WeeklyPlan
import kotlinx.coroutines.flow.Flow

@Dao
interface PlanDao {

    @Query("SELECT * FROM weekly_plan ORDER BY dayOfWeek")
    fun getWeeklyPlan(): Flow<List<WeeklyPlan>>

    @Query("SELECT * FROM weekly_plan WHERE dayOfWeek = :dayOfWeek")
    suspend fun getPlanForDay(dayOfWeek: Int): WeeklyPlan?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertPlan(plan: WeeklyPlan)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAllPlans(plans: List<WeeklyPlan>)

    @Delete
    suspend fun deletePlan(plan: WeeklyPlan)
}
