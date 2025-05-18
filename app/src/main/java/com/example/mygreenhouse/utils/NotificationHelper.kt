package com.example.mygreenhouse.utils

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import com.example.mygreenhouse.MainActivity // Assuming MainActivity is your entry point
import com.example.mygreenhouse.R
import com.example.mygreenhouse.data.model.Task
import com.example.mygreenhouse.ui.screens.task.displayName
import android.util.Log

object NotificationHelper {

    private const val CHANNEL_ID = "greenhouse_task_reminders"
    private const val CHANNEL_NAME = "Task Reminders"
    private const val CHANNEL_DESCRIPTION = "Notifications for upcoming plant tasks"

    fun createNotificationChannel(context: Context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val importance = NotificationManager.IMPORTANCE_HIGH
            val channel = NotificationChannel(CHANNEL_ID, CHANNEL_NAME, importance).apply {
                description = CHANNEL_DESCRIPTION
            }
            val notificationManager: NotificationManager =
                context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            notificationManager.createNotificationChannel(channel)
        }
    }

    fun showTaskNotification(
        context: Context,
        task: Task,
        plantName: String? // Plant name can be passed if available
    ) {
        // Intent to open the app - ideally, navigate to the specific task or task list
        val intent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            // You could add extras here to navigate to a specific screen, e.g.:
            // putExtra("destination_route", NavDestination.TaskList.route)
            // putExtra("task_id", task.id) 
        }
        val pendingIntent: PendingIntent = PendingIntent.getActivity(
            context, task.id.hashCode(), intent, PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
        )

        val notificationTitle = task.type.displayName()
        var notificationText = task.description
        if (notificationText.isBlank()) {
            notificationText = "It's time for ${task.type.displayName().lowercase()}${if (plantName != null) " for $plantName" else ""}."
        }
        if (plantName != null && !notificationText.contains(plantName, ignoreCase = true)) {
            notificationText = "For $plantName: $notificationText"
        }


        val builder = NotificationCompat.Builder(context, CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_launcher_foreground) // Replace with your app's notification icon
            .setContentTitle(notificationTitle)
            .setContentText(notificationText)
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setContentIntent(pendingIntent)
            .setAutoCancel(true) // Dismiss notification when tapped
            .setOnlyAlertOnce(true)

        try {
            with(NotificationManagerCompat.from(context)) {
                // Notification ID needs to be unique for each notification
                notify(task.id.hashCode(), builder.build())
            }
        } catch (e: SecurityException) {
            // Handle missing POST_NOTIFICATIONS permission (though we request it elsewhere)
            Log.e("NotificationHelper", "Missing notification permission: ${e.message}")
        }
    }
} 