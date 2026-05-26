package com.example.todoapp.notifications

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import com.example.todoapp.domain.TaskRepository
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import javax.inject.Inject

@AndroidEntryPoint
class BootReceiver : BroadcastReceiver() {

    @Inject
    lateinit var repository: TaskRepository

    override fun onReceive(context: Context, intent: Intent) {
        if (intent.action == Intent.ACTION_BOOT_COMPLETED) {
            rescheduleAllTasks(context)
        }
    }

    private fun rescheduleAllTasks(context: Context) {
        CoroutineScope(Dispatchers.IO).launch {
            val tasks = repository.getAllTasks().first()
            val scheduler = TaskScheduler(context)
            tasks.forEach { task ->
                if (!task.isCompleted && task.dueDate != null && task.dueDate > System.currentTimeMillis()) {
                    scheduler.scheduleReminder(task)
                }
            }
        }
    }
}
