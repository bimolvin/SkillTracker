package com.example.skilltracker.data

import android.database.Cursor
import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update

/* Data Access Objects. */
@Dao
interface SkillDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insert(skill: Skill): Long

    @Update
    fun update(skill: Skill): Int

    @Delete
    fun delete(skill: Skill): Int

    @Query(value = "DELETE FROM skill WHERE id = :id")
    fun deleteById(id: Long): Int

    @Query("SELECT * FROM skill")
    fun getAllSkills(): Cursor

    @Query("SELECT * FROM skill WHERE id = :id")
    fun getSkillById(id: Long): Cursor
}

@Dao
interface ProgressDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insert(progress: Progress): Long

    @Update
    fun update(progress: Progress): Int

    @Delete
    fun delete(progress: Progress): Int

    @Query(value = "DELETE FROM progress WHERE id = :id")
    fun deleteById(id: Long): Int

    @Query("SELECT * FROM progress WHERE skillId = :skillId")
    fun getProgressBySkillId(skillId: Long): Cursor

    @Query("SELECT * FROM progress WHERE id = :id")
    fun getProgressById(id: Long): Cursor
}
