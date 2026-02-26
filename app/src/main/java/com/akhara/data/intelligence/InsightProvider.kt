package com.akhara.data.intelligence

interface InsightProvider {

    val providerName: String

    suspend fun generateInsights(): List<Insight>

    fun isAvailable(): Boolean = true
}
