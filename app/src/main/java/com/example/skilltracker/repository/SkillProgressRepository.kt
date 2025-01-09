package com.example.skilltracker.repository

import android.content.ContentResolver
import android.content.ContentValues
import android.content.Context
import android.database.Cursor
import android.net.Uri
import android.util.Log
import com.example.skilltracker.data.Progress
import com.example.skilltracker.data.Skill
import com.example.skilltracker.data.SkillsContentProvider.Companion.CONTENT_URI_PROGRESS
import com.example.skilltracker.data.SkillsContentProvider.Companion.CONTENT_URI_SKILL
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow

class SkillProgressRepository(context: Context) {

    private val contentResolver: ContentResolver = context.contentResolver

    /* Getting all the skills via Flow. */
    suspend fun getAllSkills(): Flow<List<Skill>> = flow {
        val skills = mutableListOf<Skill>()
        val cursor: Cursor? = contentResolver.query(CONTENT_URI_SKILL, null, null, null, null)

        cursor?.let {
            while (it.moveToNext()) {
                val id = it.getLong(it.getColumnIndexOrThrow("id"))
                val name = it.getString(it.getColumnIndexOrThrow("name"))
                val description = it.getString(it.getColumnIndexOrThrow("description"))
                val lastEditDate = it.getString(it.getColumnIndexOrThrow("lastEditDate"))
                skills.add(Skill(id, name, description, lastEditDate))
            }
            it.close()
        }
        Log.d("getAllSkills", skills.toString())
        emit(skills) // send data to Flow
    }

    /* Getting a specific skill by id via Flow. */
    suspend fun getSkillById(id: Long): Flow<Skill?> = flow {
        val cursor: Cursor? = contentResolver.query(
            Uri.withAppendedPath(CONTENT_URI_SKILL, id.toString()), null, null, null, null
        )
        Log.d("getSkillById", id.toString())
        Log.d("getSkillById URI", Uri.withAppendedPath(CONTENT_URI_SKILL, id.toString()).toString())

        cursor?.let {
            if (it.moveToFirst()) {
                val name = it.getString(it.getColumnIndexOrThrow("name"))
                val description = it.getString(it.getColumnIndexOrThrow("description"))
                val lastEditDate = it.getString(it.getColumnIndexOrThrow("lastEditDate"))
                val skill = Skill(id, name, description, lastEditDate)
                Log.d("getSkillById skill", skill.toString())
                emit(skill)
            }
            it.close()
        } ?: emit(null)
    }

    /* Inserting a new skill with its progress via Flow. */
    suspend fun insertSkill(skill: Skill, progress: Progress): Flow<Uri?> = flow {
        val contentValues = ContentValues().apply {
            put("name", skill.name)
            put("description", skill.description)
            put("lastEditDate", skill.lastEditDate)
        }
        val uri = contentResolver.insert(CONTENT_URI_SKILL, contentValues)
        Log.d("insertSkill uri", uri.toString())
        val skillId = uri?.lastPathSegment?.toLongOrNull()
        skillId?.let {
            Log.d("insertSkill skillId", skillId.toString())
            insertProgress(Progress(skillId = skillId, status = progress.status,
                tracker = progress.tracker, personalNotes = progress.personalNotes))
        }
        emit(uri)
    }

    /* Updating an existing skill via Flow. */
    suspend fun updateSkill(skill: Skill): Flow<Int> = flow {
        val skillUri = Uri.withAppendedPath(
            CONTENT_URI_SKILL,
            skill.id.toString() // URI of a skill to delete
        )
        val contentValues = ContentValues().apply {
            put("name", skill.name)
            put("description", skill.description)
            put("lastEditDate", skill.lastEditDate)
        }
        val rowsUpdated = contentResolver.update(skillUri, contentValues, null, null)
        Log.d("rowsUpdated n", rowsUpdated.toString())
        emit(rowsUpdated)
    }

    /* Deleting a skill with its progress via Flow. */
    suspend fun deleteSkill(id: Long): Flow<Int> = flow {
        val skillUri = Uri.withAppendedPath(
            CONTENT_URI_SKILL,
            id.toString() // URI of a skill to delete
        )
        val rowsDeleted = contentResolver.delete(skillUri, null, null)
        Log.d("deleteSkill rowsDeleted", rowsDeleted.toString())

        val cursor: Cursor? = contentResolver.query(CONTENT_URI_PROGRESS, null, null,
            null, null)

        cursor?.let {
            if (it.moveToFirst()) {
                val progressId = it.getLong(it.getColumnIndexOrThrow("id"))
                deleteProgress(progressId)
            }
            it.close()
        }
        emit(rowsDeleted)
    }

    /* Getting a progress corresponding to a skill id via Flow. */
    suspend fun getProgressBySkillId(skillId: Long): Flow<Progress?> = flow {
        val selectionArgs = arrayOf(skillId.toString())
        val cursor: Cursor? = contentResolver.query(CONTENT_URI_PROGRESS, null, null,
            selectionArgs, null)

        cursor?.let {
            if (it.moveToFirst()) {
                val id = it.getLong(it.getColumnIndexOrThrow("id"))
                val skill = it.getLong(it.getColumnIndexOrThrow("skillId"))
                val status = it.getString(it.getColumnIndexOrThrow("status"))
                val tracker = it.getInt(it.getColumnIndexOrThrow("tracker"))
                val personalNotes = it.getString(it.getColumnIndexOrThrow("personalNotes"))
                val progress = Progress(id, skill, status, tracker, personalNotes)
                emit(progress)
            }
            it.close()
        } ?: emit(null)
    }

    /* Inserting new progress. */
    private fun insertProgress(progress: Progress) {
        Log.d("insertProgress skillId", progress.skillId.toString())
        val contentValues = ContentValues().apply {
            put("skillId", progress.skillId)
            put("status", progress.status)
            put("tracker", progress.tracker)
            put("personalNotes", progress.personalNotes)
        }
        contentResolver.insert(CONTENT_URI_PROGRESS, contentValues)
    }

    /* Updating an existing progress via Flow. */
    suspend fun updateProgress(progress: Progress): Flow<Int> = flow {
        val progressUri = Uri.withAppendedPath(
            CONTENT_URI_PROGRESS,
            progress.id.toString() // URI of a progress to delete
        )
        val contentValues = ContentValues().apply {
            put("skillId", progress.skillId)
            put("status", progress.status)
            put("tracker", progress.tracker)
            put("personalNotes", progress.personalNotes)
        }
        val rowsUpdated = contentResolver.update(progressUri, contentValues, null, null)
        Log.d("rowsUpdated n", rowsUpdated.toString())
        emit(rowsUpdated)
    }

    /* Deleting a progress. */
    private fun deleteProgress(id: Long) {
        val progressUri = Uri.withAppendedPath(
            CONTENT_URI_PROGRESS,
            id.toString() // URI of a progress to delete
        )
        contentResolver.delete(progressUri, null, null)
    }
}
