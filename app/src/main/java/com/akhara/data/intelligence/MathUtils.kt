package com.akhara.data.intelligence

object MathUtils {

    fun linearRegression(points: List<Pair<Float, Float>>): Pair<Float, Float> {
        if (points.size < 2) return Pair(0f, points.firstOrNull()?.second ?: 0f)
        val n = points.size.toFloat()
        val sumX = points.sumOf { it.first.toDouble() }.toFloat()
        val sumY = points.sumOf { it.second.toDouble() }.toFloat()
        val sumXY = points.sumOf { (it.first * it.second).toDouble() }.toFloat()
        val sumX2 = points.sumOf { (it.first * it.first).toDouble() }.toFloat()
        val denom = n * sumX2 - sumX * sumX
        if (denom == 0f) return Pair(0f, sumY / n)
        val slope = (n * sumXY - sumX * sumY) / denom
        val intercept = (sumY - slope * sumX) / n
        return Pair(slope, intercept)
    }

    fun movingAverage(values: List<Float>, window: Int): List<Float> {
        if (values.size < window) return values
        return values.windowed(window) { it.average().toFloat() }
    }

    fun weightedAverage(values: List<Float>, weights: List<Float>): Float {
        if (values.isEmpty()) return 0f
        val totalWeight = weights.sum()
        if (totalWeight == 0f) return 0f
        return values.zip(weights).sumOf { (v, w) -> (v * w).toDouble() }.toFloat() / totalWeight
    }
}
