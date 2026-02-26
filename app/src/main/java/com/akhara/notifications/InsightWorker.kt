package com.akhara.notifications

import android.content.Context
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.akhara.AkharaApp
import com.akhara.data.intelligence.InsightEngine
import java.time.LocalDate

class InsightWorker(
    private val appContext: Context,
    workerParams: WorkerParameters
) : CoroutineWorker(appContext, workerParams) {

    override suspend fun doWork(): Result {
        return try {
            val app = appContext.applicationContext as AkharaApp
            val repository = app.repository
            val engine = InsightEngine(repository)

            val insights = engine.generateAllInsights()
            val top = insights.firstOrNull()
            if (top != null) {
                NotificationHelper.showInsightNotification(
                    context = appContext,
                    title = top.title,
                    message = top.message
                )
            }

            val dayOfWeek = LocalDate.now().dayOfWeek.value
            if (dayOfWeek == 1) {
                NotificationHelper.showWeightReminder(appContext)
            }

            Result.success()
        } catch (_: Exception) {
            Result.retry()
        }
    }
}
