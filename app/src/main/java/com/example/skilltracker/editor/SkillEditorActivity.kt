package com.example.skilltracker.editor

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.widget.Button
import android.widget.TextView
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import com.example.skilltracker.MainActivity
import com.example.skilltracker.R
import com.example.skilltracker.data.Progress
import com.example.skilltracker.data.Skill
import com.example.skilltracker.skillDetail.SKILL_ID
import com.example.skilltracker.skillDetail.SkillDetailViewModel
import com.example.skilltracker.skillList.SkillListViewModel
import com.google.android.material.slider.Slider
import com.google.android.material.snackbar.Snackbar
import com.google.android.material.textfield.TextInputEditText
import com.google.android.material.textfield.TextInputLayout
import kotlinx.coroutines.launch
import org.koin.androidx.viewmodel.ext.android.viewModel
import org.koin.core.parameter.parametersOf
import java.text.NumberFormat
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale

/* Creates a new skill or edits an existing one if id was passed. */
class SkillEditorActivity : AppCompatActivity() {

    private val skillListViewModel by viewModel<SkillListViewModel> {
        parametersOf(applicationContext) // passing context to repository
    }
    private val skillDetailViewModel by viewModel<SkillDetailViewModel> {
        parametersOf(applicationContext) // passing context to repository
    }

    enum class MessageTag {
        ADD_ERROR, UPDATE_ERROR, TITLE_EMPTY, CONTENT_EMPTY
    }

    private var skillToEdit: Skill? = null
    private var editMode: Boolean = false

    private lateinit var skillName: TextInputEditText
    private lateinit var skillDescription: TextInputEditText
    private lateinit var progressLevelSlider: Slider
    private var personalNotes: TextInputEditText? = null

    private val parser = SimpleDateFormat(
        "yyyy-MM-dd", Locale("ru", "RU")
    )

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_skill_editor)

        var skillToEditId: Long? = null

        /* Connect variables to UI elements. */
        skillName = findViewById(R.id.add_skill_name)
        skillDescription = findViewById(R.id.add_skill_description)
        val progressLevel: TextView = findViewById(R.id.add_skill_level)
        progressLevelSlider = findViewById(R.id.add_skill_slider)
        personalNotes = findViewById(R.id.add_skill_notes)

        /* Display slider label as integer. */
        progressLevelSlider.setLabelFormatter { value: Float ->
            val format = NumberFormat.getIntegerInstance()
            format.maximumFractionDigits = 0
            format.format(value.toDouble())
        }

        /* Connecting progress level textView to slider value. */
        progressLevelSlider.addOnChangeListener { _, value, _ ->
            // responds to when slider's value is changed
            progressLevel.text = value.toInt().toString()
        }

        /* Go back to habit list. */
        findViewById<Button>(R.id.editor_button_back).setOnClickListener { _ ->
            redirectToMain()
        }

        val bundle: Bundle? = intent.extras
        if (bundle != null) {
            skillToEditId = bundle.getLong(SKILL_ID)
        }

        /* If skill id was passed, set edit mode, get corresponding skill and
        set existing info to edit fields. */
        skillToEditId?.let {
            editMode = true
            skillDetailViewModel.getSkillById(it)

            // query viewModel to return skill name and description from Skill table
            lifecycleScope.launch {
                lifecycle.repeatOnLifecycle(Lifecycle.State.STARTED) {
                    skillDetailViewModel.skillStateFlow.collect { skill ->
                        skill?.let {
                            skillToEdit = skill
                            skillName.setText(skill.name)
                            skillDescription.setText(skill.description)
                        }
                    }
                }
            }

            // query viewModel to return skill progress and personal notes from Progress table
            lifecycleScope.launch {
                lifecycle.repeatOnLifecycle(Lifecycle.State.STARTED) {
                    skillDetailViewModel.progressStateFlow.collect { progress ->
                        progress?.let {
                            progressLevel.text = progress.tracker.toString()
                            progressLevelSlider.value = progress.tracker.toFloat()

                            if(!progress.personalNotes.isNullOrEmpty()) {
                                personalNotes?.setText(progress.personalNotes)
                            }
                        }
                    }
                }
            }
        }

        /* Setting text fields hint visibility. */
        skillName.afterTextChanged { _ ->
            findViewById<TextInputLayout>(R.id.add_skill_name_layout).isHintEnabled = skillName.text.isNullOrEmpty()
        }
        skillDescription.afterTextChanged  { _ ->
            findViewById<TextInputLayout>(R.id.add_skill_description_layout).isHintEnabled = skillDescription.text.isNullOrEmpty()
        }
        personalNotes?.afterTextChanged { _ ->
            findViewById<TextInputLayout>(R.id.add_skill_notes_layout).isHintEnabled = personalNotes?.text.isNullOrEmpty()
        }

        /* If submit button was clicked, try to create or edit skill.  */
        findViewById<Button>(R.id.done_button).setOnClickListener {
            if(noEmptyFields()) {
                if(editMode && skillToEdit != null) {
                    editSkill()
                } else if(!editMode) {
                    addSkill()
                }
            }
        }
    }

    /* Queries viewModel to add a skill given a formed skill and progress. */
    private fun addSkill() {
        val newSkill = Skill(name = skillName.text.toString(),
            description = skillDescription.text.toString(),
            lastEditDate = parser.format(Calendar.getInstance().time))

        val level = progressLevelSlider.value.toInt()
        val newProgress = Progress(
            skillId = 0,
            status = getStatusByLevel(level),
            tracker = level,
            personalNotes = if (personalNotes != null) personalNotes!!.text.toString() else null
        )
        skillListViewModel.insertSkill(newSkill, newProgress)

        // check if insert operation was successful
        lifecycleScope.launch {
            lifecycle.repeatOnLifecycle(Lifecycle.State.STARTED) {
                skillListViewModel.inserted.collect { inserted ->
                    inserted?.let {
                        if(inserted) {
                            redirectToMain()
                        } else {
                            message(tag = MessageTag.ADD_ERROR)
                        }
                    }
                }
            }
        }
    }

    /* Queries viewModel to update an existing skill given a formed skill and progress with same ids. */
    private fun editSkill() {
        if(skillToEdit != null) {
            val newSkill = Skill(
                id = skillToEdit!!.id,
                name = skillName.text.toString(),
                description = skillDescription.text.toString(),
                lastEditDate = parser.format(Calendar.getInstance().time))

            val progressId = skillDetailViewModel.progressStateFlow.value?.id
            progressId?.let { id ->
                val level = progressLevelSlider.value.toInt()
                val newProgress = Progress(
                    id = id,
                    skillId = skillToEdit!!.id,
                    status = getStatusByLevel(level),
                    tracker = level,
                    personalNotes = if (personalNotes != null) personalNotes!!.text.toString() else null
                )
                skillDetailViewModel.updateSkill(newSkill)
                skillDetailViewModel.updateProgress(newProgress)
            }

            // check if update operation was successful
            lifecycleScope.launch {
                lifecycle.repeatOnLifecycle(Lifecycle.State.STARTED) {
                    skillDetailViewModel.updated.collect { updated ->
                        updated?.let {
                            if(updated) {
                                redirectToMain()
                            } else {
                                message(tag = MessageTag.UPDATE_ERROR)
                            }
                        }
                    }
                }
            }
        } else {
            message(tag = MessageTag.UPDATE_ERROR)
        }
    }

    /* Checks if skill name and description fields are filled. */
    private fun noEmptyFields() : Boolean {
        return if(!skillName.text.isNullOrEmpty() && !skillDescription.text.isNullOrEmpty()) {
            true
        } else {
            if(skillName.text.isNullOrEmpty()) {
                message(tag = MessageTag.TITLE_EMPTY)
            } else if(skillDescription.text.isNullOrEmpty()) {
                message(tag = MessageTag.CONTENT_EMPTY)
            }
            false
        }
    }

    /* Returns progress status that corresponds to a given knowledge level. */
    private fun getStatusByLevel(level: Int) : String {
        return when(level) {
            in 0..30 -> resources.getString(R.string.level_beginner)
            in 31..60 -> resources.getString(R.string.level_intermediate)
            in 61..90 -> resources.getString(R.string.level_advanced)
            in 91..100 -> resources.getString(R.string.level_expert)
            else -> resources.getString(R.string.level_unknown)
        }
    }

    private fun redirectToMain() {
        val intent = Intent(this, MainActivity::class.java)
        startActivity(intent)
    }

    private fun message(tag: MessageTag) {
        val editorView = findViewById<ConstraintLayout>(R.id.skill_editor)
        when(tag) {
            MessageTag.ADD_ERROR ->
                Snackbar.make(editorView, R.string.message_add_error, Snackbar.LENGTH_SHORT).show()

            MessageTag.UPDATE_ERROR ->
                Snackbar.make(editorView, R.string.message_update_error, Snackbar.LENGTH_SHORT).show()

            MessageTag.TITLE_EMPTY ->
                Snackbar.make(editorView, R.string.message_fill_title, Snackbar.LENGTH_SHORT).show()

            MessageTag.CONTENT_EMPTY ->
                Snackbar.make(editorView, R.string.message_fill_description, Snackbar.LENGTH_SHORT).show()
        }
    }

    /* An extension method in order to use a lambda expression. */
    private fun TextInputEditText.afterTextChanged(afterTextChanged: (String) -> Unit) {
        this.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {
            }

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
            }

            override fun afterTextChanged(editable: Editable?) {
                afterTextChanged.invoke(editable.toString())
            }
        })
    }
}