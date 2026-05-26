package com.example.todoapp.data

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "ai_cache")
data class AICache(
    @PrimaryKey val promptHash: Int,
    val responseJson: String,
    val timestamp: Long = System.currentTimeMillis()
)
