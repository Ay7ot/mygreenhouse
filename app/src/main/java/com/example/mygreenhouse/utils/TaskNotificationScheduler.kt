package com.example.mygreenhouse.utils

import android.content.Context
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
        
        val workRequest = OneTimeWorkRequestBuilder<TaskReminderWorker>()
            .setInitialDelay(delay)
            .setInputData(workDataOf(TaskReminderWorker.TASK_ID_KEY to task.id))
            .addTag(task.id) // Tag work with task ID for cancellation
            .build()

        WorkManager.getInstance(context).enqueueUniqueWork(
            task.id, // Use task ID as unique work name
            ExistingWorkPolicy.REPLACE, // Replace if task is updated
            workRequest
        )
        Log.d("TaskNotificationScheduler", "Scheduled notification for task ${task.id} with delay: $delay")
    }

    fun cancelTaskNotification(context: Context, taskId: String) {
        WorkManager.getInstance(context).cancelUniqueWork(taskId)
        // Also cancel by tag in case multiple workers were somehow scheduled with the same unique name but different tags (shouldn't happen with current setup)
        WorkManager.getInstance(context).cancelAllWorkByTag(taskId)
        Log.d("TaskNotificationScheduler", "Cancelled notification work for task ID: $taskId")
    }
} 