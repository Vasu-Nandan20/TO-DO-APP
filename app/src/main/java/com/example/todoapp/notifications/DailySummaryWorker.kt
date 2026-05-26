package com.example.todoapp.notifications

import android.content.Context
import androidx.hilt.work.HiltWorker
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.example.todoapp.ai.GeminiAssistant
import com.example.todoapp.domain.TaskRepository
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject
import kotlinx.coroutines.flow.first
import java.util.*

@HiltWorker
class DailySummaryWorker @AssistedInject constructor(
    @Assisted context: Context,
    @Assisted params: WorkerParameters,
    private val repository: TaskRepository,
    private val geminiAssistant: GeminiAssistant
) : CoroutineWorker(context, params) {

    override suspend fun doWork(): Result {
        val tasks = repository.getAllTasks().first()
        val today = Calendar.getInstance().apply {
            set(Calendar.HOUR_OF_DAY, 0)
            set(Calendar.MINUTE, 0)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
        }.timeInMillis
        
        val tomorrow = today + 24 * 60 * 60 * 1000
        
        val pendingToday = tasks.filter { task ->
            !task.isCompleted && task.dueDate != null && task.dueDate >= today && task.dueDate < tomorrow
        }

        if (pendingToday.isNotEmpty()) {
            val taskListString = pendingToday.joinToString(", ") { it.title }
            val aiSummary = geminiAssistant.generateSummary(taskListString) 
            
            NotificationHelper(applicationContext).showDailySummary(pendingToday.size, aiSummary)
        }

        return Result.success()
    }
}
