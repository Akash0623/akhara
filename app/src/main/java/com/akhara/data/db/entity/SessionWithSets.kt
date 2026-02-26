package com.akhara.data.db.entity

import androidx.room.Embedded
import androidx.room.Relation

data class SessionWithSets(
    @Embedded val session: WorkoutSession,
    @Relation(parentColumn = "id", entityColumn = "sessionId")
    val sets: List<WorkoutSet>
)
