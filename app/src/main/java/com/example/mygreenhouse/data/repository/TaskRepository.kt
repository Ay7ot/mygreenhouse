package com.example.mygreenhouse.data.repository

import com.example.mygreenhouse.data.dao.TaskDao
import com.example.mygreenhouse.data.model.Task
import kotlinx.coroutines.flow.Flow
import java.time.LocalDateTime

/**
 * Repository class for accessing task data
 */
class TaskRepository(private val taskDao: TaskDao) {
    
    val allTasks: Flow<List<Task>> = taskDao.getAllTasks()
    
    suspend fun insertTask(task: Task) {
        taskDao.insertTask(task)
    }
    
    suspend fun updateTask(task: Task) {
        taskDao.updateTask(task)
    }
    
    suspend fun deleteTask(task: Task) {
        taskDao.deleteTask(task)
    }
    
    fun getTaskById(id: String): Flow<Task?> {
        return taskDao.getTaskById(id)
    }
    
    fun getTasksForPlant(plantId: String): Flow<List<Task>> {
        return taskDao.getTasksForPlant(plantId)
    }
    
    fun getUpcomingTasks(limit: Int = 10): Flow<List<Task>> {
        return taskDao.getUpcomingTasks(LocalDateTime.now(), limit)
    }
    
    suspend fun markTaskAsCompleted(taskId: String) {
        taskDao.markTaskAsCompleted(taskId, LocalDateTime.now())
    }

    suspend fun markTaskAsIncomplete(taskId: String) {
        taskDao.markTaskAsIncomplete(taskId)
    }
    
    suspend fun getAllTasksOneShot(): List<Task> {
        return taskDao.getAllTasksOneShot()
    }
} 