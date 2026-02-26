package com.akhara.data.db.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "weekly_plan")
data class WeeklyPlan(
    @PrimaryKey val dayOfWeek: Int,
    val muscleGroups: String
)
