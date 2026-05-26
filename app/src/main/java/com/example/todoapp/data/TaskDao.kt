package com.example.todoapp.data

import androidx.room.*
import kotlinx.coroutines.flow.Flow

@Dao
interface TaskDao {
    @Query("SELECT * FROM tasks ORDER BY createdAt DESC")
    fun getAllTasks(): Flow<List<Task>>

    @Query("SELECT * FROM tasks WHERE id = :id")
    suspend fun getTaskById(id: Int): Task?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertTask(task: Task): Long

    @Update
    suspend fun updateTask(task: Task)

    @Delete
    suspend fun deleteTask(task: Task)

    @Query("SELECT * FROM tasks WHERE title LIKE '%' || :query || '%' OR description LIKE '%' || :query || '%'")
    fun searchTasks(query: String): Flow<List<Task>>

    // AI Cache Methods
    @Query("SELECT * FROM ai_cache WHERE promptHash = :hash")
    suspend fun getAICache(hash: Int): AICache?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAICache(cache: AICache)

    @Query("DELETE FROM ai_cache")
    suspend fun clearAICache()
}