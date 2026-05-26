package com.example.todoapp.ui.screens.analytics

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.todoapp.domain.TaskRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import java.text.SimpleDateFormat
import java.util.*
import javax.inject.Inject

@HiltViewModel
class AnalyticsViewModel @Inject constructor(
    private val repository: TaskRepository
) : ViewModel() {

    val weeklyCompletionData: StateFlow<List<DayStats>> = repository.getAllTasks()
        .map { tasks ->
            val last7Days = (0..6).map { i ->
                val cal = Calendar.getInstance()
                cal.add(Calendar.DAY_OF_YEAR, -i)
                cal.set(Calendar.HOUR_OF_DAY, 0)
                cal.set(Calendar.MINUTE, 0)
                cal.set(Calendar.SECOND, 0)
                cal.set(Calendar.MILLISECOND, 0)
                cal.timeInMillis
            }.reversed()

            last7Days.map { day ->
                val count = tasks.count { task ->
                    task.isCompleted && task.completedAt != null && isSameDay(task.completedAt, day)
                }
                val dayName = SimpleDateFormat("EEE", Locale.getDefault()).format(Date(day))
                DayStats(dayName, count)
            }
        }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    private fun isSameDay(d1: Long, d2: Long): Boolean {
        val cal1 = Calendar.getInstance().apply { timeInMillis = d1 }
        val cal2 = Calendar.getInstance().apply { timeInMillis = d2 }
        return cal1.get(Calendar.YEAR) == cal2.get(Calendar.YEAR) &&
               cal1.get(Calendar.DAY_OF_YEAR) == cal2.get(Calendar.DAY_OF_YEAR)
    }

    data class DayStats(val day: String, val count: Int)
}
