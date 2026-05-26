package com.example.todoapp.ui.screens.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.todoapp.ai.GeminiAssistant
import com.example.todoapp.data.Priority
import com.example.todoapp.data.Task
import com.example.todoapp.domain.TaskRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.util.*
import javax.inject.Inject

@OptIn(ExperimentalCoroutinesApi::class, FlowPreview::class)
@HiltViewModel
class HomeViewModel @Inject constructor(
    private val repository: TaskRepository,
    private val geminiAssistant: GeminiAssistant
) : ViewModel() {

    val userName = "User"

    private val _isAiLoading = MutableStateFlow(false)
    val isAiLoading = _isAiLoading.asStateFlow()

    private val _searchQuery = MutableStateFlow("")
    val searchQuery = _searchQuery.asStateFlow()

    val allTasks = repository.getAllTasks()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val tasks: StateFlow<List<Task>> = _searchQuery
        .debounce(300L)
        .flatMapLatest { query ->
            if (query.isBlank()) {
                repository.getAllTasks()
            } else {
                repository.searchTasks(query)
            }
        }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val stats = allTasks.map { list ->
        val completed = list.count { it.isCompleted }
        val pending = list.size - completed
        TaskStats(completed, pending, calculateStreak(list))
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), TaskStats())

    val greeting = flow {
        while (true) {
            val hour = Calendar.getInstance().get(Calendar.HOUR_OF_DAY)
            emit(when (hour) {
                in 0..11 -> "Good Morning"
                in 12..16 -> "Good Afternoon"
                else -> "Good Evening"
            })
            kotlinx.coroutines.delay(60000) // Update every minute
        }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), "Hello")

    val nextTaskCountdown: StateFlow<String?> = allTasks.map { list ->
        val nextTask = list.filter { !it.isCompleted && it.dueDate != null && it.dueDate > System.currentTimeMillis() }
            .minByOrNull { it.dueDate!! }
        nextTask
    }.flatMapLatest { nextTask ->
        if (nextTask == null) flowOf<String?>(null)
        else flow<String?> {
            while (true) {
                val remaining = nextTask.dueDate!! - System.currentTimeMillis()
                if (remaining < 0) {
                    emit(null)
                    break
                }
                val minutes = (remaining / (1000 * 60)) % 60
                val hours = (remaining / (1000 * 60 * 60))
                val result = if (hours > 0) "$hours h $minutes m" else "$minutes m"
                emit(result)
                kotlinx.coroutines.delay(60000)
            }
        }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), null)

    fun quickAddWithAi(input: String) {
        viewModelScope.launch {
            _isAiLoading.value = true
            val details = geminiAssistant.parseTaskFromNaturalLanguage(input)
            if (details != null) {
                repository.insertTask(
                    Task(
                        title = details.title,
                        description = details.description,
                        priority = try { Priority.valueOf(details.priority.uppercase()) } catch(e: Exception) { Priority.MEDIUM },
                        dueDate = details.dueDate
                    )
                )
            } else {
                repository.insertTask(Task(title = input))
            }
            _isAiLoading.value = false
        }
    }

    fun onTaskCheckedChange(task: Task, isCompleted: Boolean) {
        viewModelScope.launch {
            repository.updateTask(task.copy(
                isCompleted = isCompleted,
                completedAt = if (isCompleted) System.currentTimeMillis() else null
            ))
        }
    }

    fun onDeleteTask(task: Task) {
        viewModelScope.launch {
            repository.deleteTask(task)
        }
    }

    private fun calculateStreak(tasks: List<Task>): Int {
        val completedDates = tasks.filter { it.isCompleted && it.completedAt != null }
            .map { 
                val cal = Calendar.getInstance()
                cal.timeInMillis = it.completedAt!!
                cal.set(Calendar.HOUR_OF_DAY, 0)
                cal.set(Calendar.MINUTE, 0)
                cal.set(Calendar.SECOND, 0)
                cal.set(Calendar.MILLISECOND, 0)
                cal.timeInMillis
            }.distinct().sortedDescending()

        if (completedDates.isEmpty()) return 0

        var streak = 0
        val today = Calendar.getInstance().apply {
            set(Calendar.HOUR_OF_DAY, 0)
            set(Calendar.MINUTE, 0)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
        }.timeInMillis

        var currentDay = if (completedDates[0] == today) today else {
            val yesterday = today - 86400000
            if (completedDates[0] == yesterday) yesterday else return 0
        }

        for (date in completedDates) {
            if (date == currentDay) {
                streak++
                currentDay -= 86400000
            } else {
                break
            }
        }
        return streak
    }

    data class TaskStats(
        val completed: Int = 0,
        val pending: Int = 0,
        val streak: Int = 0
    )
}
