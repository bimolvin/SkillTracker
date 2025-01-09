package com.example.skilltracker.data

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "skill")
data class Skill(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val name: String,
    val description: String?,
    val lastEditDate: String
)
