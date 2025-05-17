package com.example.mygreenhouse.data.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.example.mygreenhouse.data.model.Task
import kotlinx.coroutines.flow.Flow
import java.time.LocalDateTime

@Dao
interface TaskDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertTask(task: Task)
    
    @Update
    suspend fun updateTask(task: Task)
    
    @Delete
    suspend fun deleteTask(task: Task)
    
    @Query("SELECT * FROM tasks ORDER BY scheduledDateTime ASC")
    fun getAllTasks(): Flow<List<Task>>
    
    @Query("SELECT * FROM tasks WHERE id = :taskId")
    fun getTaskById(taskId: String): Flow<Task?>
    
    @Query("SELECT * FROM tasks WHERE plantId = :plantId ORDER BY scheduledDateTime ASC")
    fun getTasksForPlant(plantId: String): Flow<List<Task>>
    
    @Query("SELECT * FROM tasks WHERE isCompleted = 0 AND scheduledDateTime > :now ORDER BY scheduledDateTime ASC LIMIT :limit")
    fun getUpcomingTasks(now: LocalDateTime, limit: Int = 10): Flow<List<Task>>
    
    @Query("UPDATE tasks SET isCompleted = 1, completedDateTime = :completionTime WHERE id = :taskId")
    suspend fun markTaskAsCompleted(taskId: String, completionTime: LocalDateTime)

    @Query("UPDATE tasks SET isCompleted = 0, completedDateTime = NULL WHERE id = :taskId")
    suspend fun markTaskAsIncomplete(taskId: String)
} 