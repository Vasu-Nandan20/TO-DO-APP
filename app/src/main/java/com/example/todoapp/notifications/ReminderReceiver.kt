package com.example.todoapp.notifications

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import com.example.todoapp.domain.TaskRepository
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import javax.inject.Inject

@AndroidEntryPoint
class ReminderReceiver : BroadcastReceiver() {

    @Inject
    lateinit var repository: TaskRepository

    override fun onReceive(context: Context, intent: Intent) {
        val taskId = intent.getIntExtra("taskId", -1)
        val title = intent.getStringExtra("title") ?: "Task Reminder"
        val description = intent.getStringExtra("description") ?: ""

        when (intent.action) {
            "ACTION_DONE" -> {
                markTaskAsDone(taskId)
                cancelNotification(context, taskId)
            }
            "ACTION_SNOOZE" -> {
                snoozeTask(context, taskId, title, description)
                cancelNotification(context, taskId)
            }
            else -> {
                if (taskId != -1) {
                    NotificationHelper(context).showTaskNotification(taskId, title, description)
                }
            }
        }
    }

    private fun markTaskAsDone(taskId: Int) {
        if (taskId == -1) return
        CoroutineScope(Dispatchers.IO).launch {
            val task = repository.getTaskById(taskId)
            task?.let {
                repository.updateTask(it.copy(
                    isCompleted = true,
                    completedAt = System.currentTimeMillis()
                ))
            }
        }
    }

    private fun snoozeTask(context: Context, taskId: Int, title: String, description: String) {
        val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
        val intent = Intent(context, ReminderReceiver::class.java).apply {
            putExtra("taskId", taskId)
            putExtra("title", title)
            putExtra("description", description)
        }
        
        val pendingIntent = PendingIntent.getBroadcast(
            context,
            taskId,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val snoozeTime = System.currentTimeMillis() + 10 * 60 * 1000 // 10 minutes
        alarmManager.setExactAndAllowWhileIdle(
            AlarmManager.RTC_WAKEUP,
            snoozeTime,
            pendingIntent
        )
    }

    private fun cancelNotification(context: Context, taskId: Int) {
        val manager = context.getSystemService(Context.NOTIFICATION_SERVICE) as android.app.NotificationManager
        manager.cancel(taskId)
    }
}
