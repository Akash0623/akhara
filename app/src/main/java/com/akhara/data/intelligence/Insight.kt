package com.akhara.data.intelligence

data class Insight(
    val type: InsightType,
    val title: String,
    val message: String,
    val priority: Int,
    val exerciseId: Int? = null,
    val metadata: Map<String, Any> = emptyMap()
)

enum class InsightType {
    PR_COUNTDOWN,
    MUSCLE_RECOVERY,
    FATIGUE_SCORE,
    DURATION_PREDICTION,
    REST_ADVISOR,
    VOLUME_BALANCE,
    PLATEAU_DETECTED,
    PROGRESSIVE_OVERLOAD,
    COMPLETION_RATE,
    ADHERENCE,
    EXERCISE_SUBSTITUTION,
    DELOAD_SUGGESTION,
    PR_ACHIEVED,
    ML_PREDICTION,
    LLM_SUMMARY
}
