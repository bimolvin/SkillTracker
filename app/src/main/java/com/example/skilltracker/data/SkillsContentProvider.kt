package com.example.skilltracker.data

import android.content.ContentProvider
import android.content.ContentValues
import android.content.UriMatcher
import android.database.Cursor
import android.net.Uri
import android.util.Log
import com.example.skilltracker.di.appModule
import org.koin.android.ext.android.inject
import org.koin.android.ext.koin.androidContext
import org.koin.core.context.GlobalContext.startKoin

class SkillsContentProvider : ContentProvider() {

    companion object {
        const val AUTHORITY = "com.example.skills-provider"
        val CONTENT_URI_SKILL: Uri = Uri.parse("content://$AUTHORITY/skill")
        val CONTENT_URI_PROGRESS: Uri = Uri.parse("content://$AUTHORITY/progress")

        const val SKILL = 1
        const val SKILL_ID = 2
        const val PROGRESS = 3
        const val PROGRESS_ID = 4

        private val uriMatcher = UriMatcher(UriMatcher.NO_MATCH).apply {
            addURI(AUTHORITY, "skill", SKILL)
            addURI(AUTHORITY, "skill/#", SKILL_ID)
            addURI(AUTHORITY, "progress", PROGRESS)
            addURI(AUTHORITY, "progress/#", PROGRESS_ID)
        }
    }

    /* Injecting Data Access Objects. */
    private val skillDao: SkillDao by inject()
    private val progressDao: ProgressDao by inject()

    override fun onCreate(): Boolean {
        /* Initializing the Koin module for the database. */
        startKoin {
            context?.let { androidContext(it) }
            modules(appModule)
        }
        return true
    }

    override fun query(
        uri: Uri,
        projection: Array<String>?,
        selection: String?,
        selectionArgs: Array<String>?,
        sortOrder: String?
    ): Cursor? {
        return when (uriMatcher.match(uri)) {
            SKILL -> {
                // return all skills
                val skills = skillDao.getAllSkills()
                Log.d("query", skills.toString())
                skills
            }
            SKILL_ID -> {
                // return a skill that corresponds to an id
                val id = uri.lastPathSegment?.toLongOrNull()
                if (id != null) {
                    skillDao.getSkillById(id)
                } else {
                    null
                }
            }
            PROGRESS -> {
                // return a progress that corresponds to a skill id
                progressDao.getProgressBySkillId(selectionArgs?.first()?.toLongOrNull() ?: -1)
            }
            PROGRESS_ID -> {
                // return a progress that corresponds to an id
                val id = uri.lastPathSegment?.toLongOrNull()
                if (id != null) {
                    progressDao.getProgressById(id)
                } else {
                    null
                }
            }
            else -> throw IllegalArgumentException("Unknown URI: $uri")
        }
    }

    override fun insert(uri: Uri, values: ContentValues?): Uri? {
        return when (uriMatcher.match(uri)) {
            SKILL -> {
                val skill = Skill(
                    name = values?.getAsString("name") ?: "",
                    description = values?.getAsString("description") ?: "",
                    lastEditDate = values?.getAsString("lastEditDate") ?: ""
                )
                val id = skillDao.insert(skill)
                Uri.withAppendedPath(CONTENT_URI_SKILL, id.toString())
            }
            PROGRESS -> {
                val progress = Progress(
                    skillId = values?.getAsLong("skillId") ?: -1,
                    status = values?.getAsString("status") ?: "",
                    tracker = values?.getAsInteger("tracker") ?: -1,
                    personalNotes = values?.getAsString("personalNotes") ?: ""
                )
                Log.d("insertProgress PROGRESS", progress.toString())
                val id = progressDao.insert(progress)
                Uri.withAppendedPath(CONTENT_URI_PROGRESS, id.toString())
            }
            else -> throw IllegalArgumentException("Unknown URI: $uri")
        }
    }

    override fun update(
        uri: Uri,
        values: ContentValues?,
        selection: String?,
        selectionArgs: Array<String>?
    ): Int {
        when (uriMatcher.match(uri)) {
            SKILL_ID -> {
                val skillId = uri.lastPathSegment?.toLongOrNull()
                if(skillId != null) {
                    val skill = Skill(
                        id = skillId,
                        name = values?.getAsString("name") ?: "",
                        description = values?.getAsString("description") ?: "",
                        lastEditDate = values?.getAsString("lastEditDate") ?: ""
                    )
                    val n = skillDao.update(skill)
                    Log.d("updateSkill n", n.toString())
                    return n
                }
            }
            PROGRESS_ID -> {
                val progressId = uri.lastPathSegment?.toLongOrNull()
                if(progressId != null) {
                    val progress = Progress(
                        id = progressId,
                        skillId = values?.getAsLong("skillId") ?: -1,
                        status = values?.getAsString("status") ?: "",
                        tracker = values?.getAsInteger("tracker") ?: -1,
                        personalNotes = values?.getAsString("personalNotes") ?: ""
                    )
                    return progressDao.update(progress)
                }
            }
            else -> throw IllegalArgumentException("Unknown URI: $uri")
        }
        return 0
    }

    override fun delete(uri: Uri, selection: String?, selectionArgs: Array<String>?): Int {
        when (uriMatcher.match(uri)) {
            SKILL_ID -> {
                val skillId = uri.lastPathSegment?.toLongOrNull()
                if(skillId != null) {
                    return skillDao.deleteById(skillId)
                }
            }
            PROGRESS_ID -> {
                val progressId = uri.lastPathSegment?.toLongOrNull()
                if(progressId != null) {
                    return progressDao.deleteById(progressId)
                }
            }
            else -> throw IllegalArgumentException("Unknown URI: $uri")
        }
        return 0
    }

    override fun getType(uri: Uri): String {
        return when (uriMatcher.match(uri)) {
            SKILL -> "vnd.android.cursor.dir/$AUTHORITY.skill"
            SKILL_ID -> "vnd.android.cursor.item/$AUTHORITY.skill"
            PROGRESS -> "vnd.android.cursor.dir/$AUTHORITY.progress"
            PROGRESS_ID -> "vnd.android.cursor.item/$AUTHORITY.progress"
            else -> throw IllegalArgumentException("Unknown URI: $uri")
        }
    }
}
