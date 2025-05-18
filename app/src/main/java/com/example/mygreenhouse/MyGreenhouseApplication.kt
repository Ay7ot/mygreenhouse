package com.example.mygreenhouse

import android.app.Application
import com.example.mygreenhouse.utils.NotificationHelper

class MyGreenhouseApplication : Application() {

    override fun onCreate() {
        super.onCreate()

        // Create notification channel (important for Android O+)
        NotificationHelper.createNotificationChannel(this)

        // Periodic task reminder is no longer scheduled here
        // setupPeriodicTaskReminder() // This line is removed/commented out
    }

    // The setupPeriodicTaskReminder() method is removed entirely
    /*
    private fun setupPeriodicTaskReminder() {
        val taskReminderRequest =
            PeriodicWorkRequestBuilder<TaskReminderWorker>(1, TimeUnit.HOURS) 
                .build()

        WorkManager.getInstance(applicationContext).enqueueUniquePeriodicWork(
            "TaskReminderPeriodicWork", 
            ExistingPeriodicWorkPolicy.REPLACE, 
            taskReminderRequest
        )
    }
    */
} 