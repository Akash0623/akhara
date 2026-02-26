package com.akhara

import android.app.Application
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import com.akhara.data.db.AkharaDatabase
import com.akhara.data.repository.WorkoutRepository
import com.akhara.notifications.InsightWorker
import com.akhara.notifications.NotificationHelper
import java.util.concurrent.TimeUnit

class AkharaApp : Application() {

    val database by lazy { AkharaDatabase.getDatabase(this) }
    val repository by lazy { WorkoutRepository(database) }

    override fun onCreate() {
        super.onCreate()
        NotificationHelper.createChannel(this)
        scheduleInsightWorker()
    }

    private fun scheduleInsightWorker() {
        val request = PeriodicWorkRequestBuilder<InsightWorker>(24, TimeUnit.HOURS)
            .setInitialDelay(1, TimeUnit.HOURS)
            .build()

        WorkManager.getInstance(this).enqueueUniquePeriodicWork(
            "akhara_daily_insights",
            ExistingPeriodicWorkPolicy.KEEP,
            request
        )
    }
}
