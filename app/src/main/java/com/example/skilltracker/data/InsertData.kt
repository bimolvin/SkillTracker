package com.example.skilltracker.data

import android.content.Context
import android.util.Log
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

/* Filling out the Skill and Progress tables with initial data. */
suspend fun populateDatabase(context: Context) {
    val db = AppDatabase.getInstance(context)

    // forming Skill table
    val skills = listOf(
        Skill(id = 0, name = "Kotlin", description = "Modern programming language", lastEditDate = "2024-12-24"),
        Skill(id = 1, name = "Java", description = "Popular object-oriented programming language", lastEditDate = "2024-12-24"),
        Skill(id = 2, name = "Python", description = "Versatile programming language", lastEditDate = "2024-12-24"),
        Skill(id = 3, name = "C++", description = "High-performance programming language", lastEditDate = "2024-12-24"),
        Skill(id = 4, name = "JavaScript", description = "Language for web development", lastEditDate = "2024-12-24"),
        Skill(id = 5, name = "SQL", description = "Language for managing databases", lastEditDate = "2024-12-24"),
        Skill(id = 6, name = "Android Development", description = "Creating mobile apps for Android", lastEditDate = "2024-12-24"),
        Skill(id = 7, name = "iOS Development", description = "Creating mobile apps for iOS", lastEditDate = "2024-12-24"),
        Skill(id = 8, name = "Machine Learning", description = "Field of artificial intelligence", lastEditDate = "2024-12-24"),
        Skill(id = 9, name = "Web Development", description = "Creating websites and web applications", lastEditDate = "2024-12-24")
    )

    // forming Progress table
    val progresses = listOf(
        Progress(id = 0, skillId = 0, status = "Beginner", tracker = 20, personalNotes = "Still learning the basics"),
        Progress(id = 1, skillId = 1, status = "Intermediate", tracker = 50, personalNotes = "Understand most concepts"),
        Progress(id = 2, skillId = 2, status = "Advanced", tracker = 80, personalNotes = "Able to create complex projects"),
        Progress(id = 3, skillId = 3, status = "Expert", tracker = 100, personalNotes = "Master of the language"),
        Progress(id = 4, skillId = 4, status = "Beginner", tracker = 30, personalNotes = "Working on simple programs"),
        Progress(id = 5, skillId = 5, status = "Intermediate", tracker = 60, personalNotes = "Learning frameworks and tools"),
        Progress(id = 6, skillId = 6, status = "Advanced", tracker = 90, personalNotes = "Building large-scale projects"),
        Progress(id = 7, skillId = 7, status = "Expert", tracker = 100, personalNotes = "Fluent in multiple libraries and tools"),
        Progress(id = 8, skillId = 8, status = "Beginner", tracker = 25, personalNotes = "Exploring algorithms and structures"),
        Progress(id = 9, skillId = 9, status = "Intermediate", tracker = 50, personalNotes = "Focused on specific libraries")
    )

    /* Adding data to the Skill table. */
    withContext(Dispatchers.IO) {
        skills.forEach { skill ->
            db.skillDao().insert(skill)
        }
        Log.d("skills insert", skills.toString())
    }

    /* Adding data to the Progress table. */
    withContext(Dispatchers.IO) {
        progresses.forEach { progress ->
            db.progressDao().insert(progress)
        }
        Log.d("progresses insert", progresses.toString())
    }
}
