package com.example.skilltracker.skillDetail

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.TypedValue
import android.widget.Button
import android.widget.ProgressBar
import android.widget.TextView
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import com.example.skilltracker.MainActivity
import com.example.skilltracker.R
import kotlinx.coroutines.launch
import org.koin.androidx.viewmodel.ext.android.viewModel
import org.koin.core.parameter.parametersOf

const val SKILL_ID = "skill id"

/* Displays skill chosen from list. */
class SkillDetailActivity : AppCompatActivity() {

    private val skillDetailViewModel by viewModel<SkillDetailViewModel> {
        parametersOf(applicationContext) // passing context to repository
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_skill_detail)

        var currentSkillId: Long? = null

        /* Connect variables to UI elements. */
        val skillName: TextView = findViewById(R.id.skill_title)
        val skillDescription: TextView = findViewById(R.id.skill_description)
        val progressStatus: TextView = findViewById(R.id.status)
        val progressBar: ProgressBar = findViewById(R.id.pBar)
        val progressTracker: TextView = findViewById(R.id.progress)
        val progressNote: TextView = findViewById(R.id.skill_note)

        val buttonBack : Button = findViewById(R.id.button_back)

        val bundle: Bundle? = intent.extras
        if (bundle != null) {
            currentSkillId = bundle.getLong(SKILL_ID)
        }

        /* If currentSkillId is not null, get corresponding skill and set name, description,
        progress level and personal notes. */
        currentSkillId?.let {
            skillDetailViewModel.getSkillById(it)

            // query repository to return current skill name and description from Skill table
            lifecycleScope.launch {
                lifecycle.repeatOnLifecycle(Lifecycle.State.STARTED) {
                    skillDetailViewModel.skillStateFlow.collect { skill ->
                        skill?.let {
                            skillName.text = skill.name
                            setTextSize(skillName)
                            skillDescription.text = skill.description
                        }
                    }
                }
            }

            // query viewModel to return current skill progress and personal notes from Progress table
            lifecycleScope.launch {
                lifecycle.repeatOnLifecycle(Lifecycle.State.STARTED) {
                    skillDetailViewModel.progressStateFlow.collect { progress ->
                        progress?.let {
                            progressStatus.text = progress.status
                            progressBar.progress = progress.tracker
                            progressTracker.text = String.format(
                                resources.getString(R.string.progress_value),
                                progress.tracker
                            )

                            if(!progress.personalNotes.isNullOrEmpty()) {
                                progressNote.text = progress.personalNotes
                            } else {
                                progressNote.text = resources.getString(R.string.note_empty)
                            }
                        }
                    }
                }
            }

            /* Go back to habit list. */
            buttonBack.setOnClickListener {_ ->
                val intent = Intent(this, MainActivity::class.java)
                startActivity(intent)
            }
        }
    }

    /* Makes font size smaller for long skill names. */
    private fun setTextSize(title: TextView) {
        if(title.text.length > 8) {
            title.setTextSize(TypedValue.COMPLEX_UNIT_SP, 56F)
        }
    }
}