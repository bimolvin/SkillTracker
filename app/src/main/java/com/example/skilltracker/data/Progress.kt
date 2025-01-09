package com.example.skilltracker.data

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "progress")
data class Progress(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val skillId: Long,  // foreign key for linking to the Skill table
    val status: String,
    val tracker: Int,
    val personalNotes: String?
)