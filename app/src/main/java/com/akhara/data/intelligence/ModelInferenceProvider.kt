package com.akhara.data.intelligence

/**
 * Stub for future on-device or remote ML model inference.
 * Implement this to provide ML-powered insights (e.g., rep prediction,
 * workout classification, fatigue estimation from sensor data).
 */
class ModelInferenceProvider : InsightProvider {

    override val providerName: String = "ML Inference"

    override fun isAvailable(): Boolean = false

    override suspend fun generateInsights(): List<Insight> {
        if (!isAvailable()) return emptyList()

        return listOf(
            Insight(
                type = InsightType.ML_PREDICTION,
                title = "AI Prediction",
                message = "ML model inference not yet configured.",
                priority = 4
            )
        )
    }

    /**
     * Placeholder for loading a TFLite or ONNX model.
     * Future implementation would load the model asset and run inference.
     */
    fun loadModel(modelPath: String): Boolean {
        return false
    }

    /**
     * Placeholder for running prediction on workout data.
     * Returns null until a model is loaded.
     */
    fun predict(features: FloatArray): FloatArray? {
        return null
    }
}
