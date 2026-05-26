package com.example.todoapp.data

import android.content.Context
import com.example.todoapp.domain.TaskRepository
import com.example.todoapp.notifications.TaskScheduler
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class TaskRepositoryImpl @Inject constructor(
    private val dao: TaskDao,
    @ApplicationContext private val context: Context
) : TaskRepository {
    private val scheduler = TaskScheduler(context)

    override fun getAllTasks(): Flow<List<Task>> = dao.getAllTasks()

    override suspend fun getTaskById(id: Int): Task? = dao.getTaskById(id)

    override suspend fun insertTask(task: Task): Long {
        val id = dao.insertTask(task)
        val insertedTask = task.copy(id = id.toInt())
        scheduler.scheduleReminder(insertedTask)
        return id
    }

    override suspend fun updateTask(task: Task) {
        dao.updateTask(task)
        if (task.isCompleted) {
            scheduler.cancelReminder(task.id)
        } else {
            scheduler.scheduleReminder(task)
        }
    }

    override suspend fun deleteTask(task: Task) {
        dao.deleteTask(task)
        scheduler.cancelReminder(task.id)
    }

    override fun searchTasks(query: String): Flow<List<Task>> = dao.searchTasks(query)

    override suspend fun clearAICache() {
        dao.clearAICache()
    }

    override suspend fun syncFromFirestore() {
        // No-op as auth is removed
    }
}
