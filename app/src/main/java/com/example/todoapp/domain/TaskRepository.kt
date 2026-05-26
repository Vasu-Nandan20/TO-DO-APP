package com.example.todoapp.domain

import com.example.todoapp.data.Task
import kotlinx.coroutines.flow.Flow

interface TaskRepository {
    fun getAllTasks(): Flow<List<Task>>
    suspend fun getTaskById(id: Int): Task?
    suspend fun insertTask(task: Task): Long
    suspend fun updateTask(task: Task)
    suspend fun deleteTask(task: Task)
    fun searchTasks(query: String): Flow<List<Task>>
    suspend fun clearAICache()
    suspend fun syncFromFirestore()
}