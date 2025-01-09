package com.example.skilltracker.data

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase

@Database(entities = [Skill::class, Progress::class], version = 1)
abstract class AppDatabase : RoomDatabase() {
    abstract fun skillDao(): SkillDao
    abstract fun progressDao(): ProgressDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        fun getInstance(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance =
                    Room.databaseBuilder(context, AppDatabase::class.java, "skills_database")
                        .build()

                INSTANCE = instance
                instance
            }
        }
    }
}
