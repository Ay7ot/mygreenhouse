package com.example.mygreenhouse.utils

import android.content.Context
import android.os.Build
import android.util.Log
import androidx.work.ExistingWorkPolicy
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkManager
import androidx.work.workDataOf
import com.example.mygreenhouse.data.model.Task
import com.example.mygreenhouse.workers.TaskReminderWorker
import java.time.Duration
import java.time.LocalDateTime

object TaskNotificationScheduler {

    fun scheduleTaskNotification(context: Context, task: Task) {
        if (task.isCompleted) {
            Log.d("TaskNotificationScheduler", "Task ${task.id} is already completed. Not scheduling notification.")
            cancelTaskNotification(context, task.id) // Ensure any existing work is cancelled
            return
        }

        val now = LocalDateTime.now()
        val scheduledTime = task.scheduledDateTime

        if (scheduledTime.isBefore(now)) {
            Log.d("TaskNotificationScheduler", "Task ${task.id} scheduled time ${scheduledTime} is in the past. Not scheduling.")
            // Optionally, you could trigger an immediate notification here if it's just slightly past due
            // and not completed, but for now, we only schedule for future tasks.
            return
        }

        val delay = Duration.between(now, scheduledTime)

        if (delay.isNegative || delay.isZero) {
            Log.d("TaskNotificationScheduler", "Task ${task.id} scheduled time is now or past (delay: $delay). Not scheduling via WorkManager for future.")
            // Handle very near-term or past-due tasks if necessary (e.g. immediate notification if appropriate)
            return
        }
        
        val workRequestBuilder = OneTimeWorkRequestBuilder<TaskReminderWorker>()
            .setInputData(workDataOf(TaskReminderWorker.TASK_ID_KEY to task.id))
            .addTag(task.id) // Tag work with task ID for cancellation

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            workRequestBuilder.setInitialDelay(delay)
        } else {
            // For API < 26, schedule without initial delay if the delay is significant.
            // WorkManager will attempt to run it as soon as constraints are met.
            // If delay is very small, it might run almost immediately.
            // This is a trade-off for compatibility.
            if (delay.toMillis() > 0) {
                 // No setInitialDelay for older APIs, it will run when constraints are met.
                 // Log that we are not using setInitialDelay
                Log.d("TaskNotificationScheduler", "API level ${Build.VERSION.SDK_INT} < 26. Scheduling task ${task.id} without explicit initial delay. Actual delay: $delay")
            } else {
                // If delay is zero or negative (should be caught above, but as a safeguard)
                 Log.d("TaskNotificationScheduler", "Task ${task.id} delay is zero or negative, not scheduling with WorkManager.")
                return
            }
        }
        
        val workRequest = workRequestBuilder.build()

        WorkManager.getInstance(context).enqueueUniqueWork(
            task.id, // Use task ID as unique work name
            ExistingWorkPolicy.REPLACE, // Replace if task is updated
            workRequest
        )
        Log.d("TaskNotificationScheduler", "Scheduled notification for task ${task.id} with effective delay (if API >=26): $delay")
    }

    fun cancelTaskNotification(context: Context, taskId: String) {
        WorkManager.getInstance(context).cancelUniqueWork(taskId)
        // Also cancel by tag in case multiple workers were somehow scheduled with the same unique name but different tags (shouldn't happen with current setup)
        WorkManager.getInstance(context).cancelAllWorkByTag(taskId)
        Log.d("TaskNotificationScheduler", "Cancelled notification work for task ID: $taskId")
    }
} 