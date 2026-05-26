package com.example.todoapp.ui.screens.add_edit_task

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.todoapp.data.Priority
import com.example.todoapp.data.Task
import com.example.todoapp.domain.TaskRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class AddEditTaskViewModel @Inject constructor(
    private val repository: TaskRepository,
    savedStateHandle: SavedStateHandle
) : ViewModel() {

    private val taskId: Int = savedStateHandle["taskId"] ?: -1

    private val _title = MutableStateFlow("")
    val title = _title.asStateFlow()

    private val _description = MutableStateFlow("")
    val description = _description.asStateFlow()

    private val _priority = MutableStateFlow(Priority.MEDIUM)
    val priority = _priority.asStateFlow()

    private val _dueDate = MutableStateFlow<Long?>(null)
    val dueDate = _dueDate.asStateFlow()

    init {
        if (taskId != -1) {
            viewModelScope.launch {
                repository.getTaskById(taskId)?.let { task ->
                    _title.value = task.title
                    _description.value = task.description
                    _priority.value = task.priority
                    _dueDate.value = task.dueDate
                }
            }
        }
    }

    fun onTitleChange(newTitle: String) {
        _title.value = newTitle
    }

    fun onDescriptionChange(newDescription: String) {
        _description.value = newDescription
    }

    fun onPriorityChange(newPriority: Priority) {
        _priority.value = newPriority
    }

    fun onDueDateChange(newDate: Long?) {
        _dueDate.value = newDate
    }

    fun saveTask(onSuccess: () -> Unit) {
        viewModelScope.launch {
            if (_title.value.isBlank()) return@launch
            
            val task = if (taskId != -1) {
                repository.getTaskById(taskId)?.copy(
                    title = _title.value,
                    description = _description.value,
                    priority = _priority.value,
                    dueDate = _dueDate.value
                )
            } else {
                Task(
                    title = _title.value,
                    description = _description.value,
                    priority = _priority.value,
                    dueDate = _dueDate.value
                )
            }
            
            task?.let {
                if (taskId != -1) repository.updateTask(it)
                else repository.insertTask(it)
                onSuccess()
            }
        }
    }
}
