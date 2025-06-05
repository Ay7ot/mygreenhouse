package com.example.mygreenhouse.workers

import android.content.Context
import android.util.Log
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import androidx.work.workDataOf
import com.example.mygreenhouse.data.AppDatabase
import com.example.mygreenhouse.utils.NotificationHelper
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class TaskReminderWorker(
    private val appContext: Context,
    workerParams: WorkerParameters
) : CoroutineWorker(appContext, workerParams) {

    companion object {
        const val TASK_ID_KEY = "TASK_ID"
    }

    override suspend fun doWork(): Result {
        val taskId = inputData.getString(TASK_ID_KEY)
        if (taskId.isNullOrBlank()) {
            Log.e("TaskReminderWorker", "Task ID is missing from inputData.")
            return Result.failure() // Missing crucial data
        }

        Log.d("TaskReminderWorker", "Starting work for task ID: $taskId")

        return try {
            val taskDao = AppDatabase.getDatabase(appContext).taskDao()
            val plantDao = AppDatabase.getDatabase(appContext).plantDao()

            // Ensure notification channel is created (idempotent)
            NotificationHelper.createNotificationChannel(appContext)

            val task = withContext(Dispatchers.IO) { taskDao.getTaskByIdOnce(taskId) }

            if (task == null) {
                Log.e("TaskReminderWorker", "Task with ID $taskId not found.")
                return Result.failure() // Task might have been deleted
            }

            if (task.isCompleted) {
                Log.d("TaskReminderWorker", "Task $taskId is already completed. No notification needed.")
                return Result.success() // No work needed if task is already complete
            }

            var plantName: String? = null
            if (task.plantId != null) {
                val plant = withContext(Dispatchers.IO) { plantDao.getPlantByIdOnce(task.plantId) }
                plantName = plant?.let { "${it.strainName} - ${it.batchNumber}" }
            }

            NotificationHelper.showTaskNotification(appContext, task, plantName)
            Log.d("TaskReminderWorker", "Notification shown for task: ${task.id}")
            Result.success()
        } catch (e: Exception) {
            Log.e("TaskReminderWorker", "Error during work for task $taskId: ${e.message}", e)
            Result.failure()
        }
    }
}
