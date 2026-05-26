package com.example.todoapp.ai

import com.example.todoapp.BuildConfig
import com.example.todoapp.data.AICache
import com.example.todoapp.data.TaskDao
import com.google.ai.client.generativeai.GenerativeModel
import org.json.JSONObject
import java.text.SimpleDateFormat
import java.util.*
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class GeminiAssistant @Inject constructor(
    private val taskDao: TaskDao
) {
    private val apiKey = BuildConfig.GEMINI_API_KEY
    
    private val model = GenerativeModel(
        modelName = "gemini-1.5-flash",
        apiKey = apiKey
    )

    suspend fun parseTaskFromNaturalLanguage(input: String): TaskDetails? {
        if (apiKey == "YOUR_API_KEY_HERE" || apiKey.isEmpty()) {
            android.util.Log.e("GeminiAssistant", "Invalid API Key detected. Please set GEMINI_API_KEY in local.properties")
            return performOfflineParsing(input)
        }
        android.util.Log.d("GeminiAssistant", "Parsing input: $input")
        val hash = input.trim().lowercase().hashCode()

        // 1. Check Cache
        val cached = taskDao.getAICache(hash)
        if (cached != null) {
            android.util.Log.d("GeminiAssistant", "Cache hit for: $input")
            if (System.currentTimeMillis() - cached.timestamp < 7 * 24 * 60 * 60 * 1000) {
                return parseTaskFromJson(cached.responseJson)
            }
        }

        // 2. AI Request
        val today = SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.getDefault()).format(Date())
        val prompt = """
            Extract task details from this sentence: "$input"
            Today's date and time is $today.
            Respond ONLY with a JSON object containing:
            "title": string,
            "description": string,
            "priority": "HIGH" | "MEDIUM" | "LOW",
            "dueDate": "yyyy-MM-dd HH:mm" (string or null)
        """.trimIndent()

        return try {
            val response = model.generateContent(prompt)
            var jsonText = response.text ?: throw Exception("Empty response")
            
            val startIndex = jsonText.indexOf("{")
            val endIndex = jsonText.lastIndexOf("}")
            if (startIndex != -1 && endIndex != -1 && endIndex > startIndex) {
                jsonText = jsonText.substring(startIndex, endIndex + 1)
            }
            
            taskDao.insertAICache(AICache(hash, jsonText))
            
            parseTaskFromJson(jsonText)
        } catch (e: Exception) {
            android.util.Log.e("GeminiAssistant", "AI Parsing failed, using offline fallback", e)
            performOfflineParsing(input)
        }
    }

    private fun parseTaskFromJson(jsonText: String): TaskDetails {
        val json = JSONObject(jsonText)
        return TaskDetails(
            title = json.optString("title", "New Task"),
            description = json.optString("description", ""),
            priority = json.optString("priority", "MEDIUM"),
            dueDate = parseDate(json.optString("dueDate", ""))
        )
    }

    private fun performOfflineParsing(input: String): TaskDetails {
        val lowercase = input.lowercase()
        val priority = when {
            lowercase.contains("urgent") || lowercase.contains("high") -> "HIGH"
            lowercase.contains("low") -> "LOW"
            else -> "MEDIUM"
        }
        
        val cal = Calendar.getInstance()
        val dueDate = when {
            lowercase.contains("tomorrow") -> {
                cal.add(Calendar.DAY_OF_YEAR, 1)
                cal.timeInMillis
            }
            lowercase.contains("next week") -> {
                cal.add(Calendar.DAY_OF_YEAR, 7)
                cal.timeInMillis
            }
            else -> null
        }

        return TaskDetails(
            title = input.take(50).replaceFirstChar { it.uppercase() },
            description = "Auto-parsed: $input",
            priority = priority,
            dueDate = dueDate
        )
    }

    private fun parseDate(dateStr: String): Long? {
        if (dateStr.isEmpty() || dateStr == "null") return null
        val formats = listOf("yyyy-MM-dd HH:mm", "yyyy-MM-dd")
        for (format in formats) {
            try {
                return SimpleDateFormat(format, Locale.getDefault()).parse(dateStr)?.time
            } catch (e: Exception) {}
        }
        return null
    }

    suspend fun generateSummary(tasks: String): String? {
        val prompt = "Here are my tasks for today: $tasks. Give me a 1-sentence motivational summary or advice."
        return try {
            model.generateContent(prompt).text
        } catch (e: Exception) {
            null
        }
    }

    data class TaskDetails(
        val title: String,
        val description: String,
        val priority: String,
        val dueDate: Long?
    )
}
