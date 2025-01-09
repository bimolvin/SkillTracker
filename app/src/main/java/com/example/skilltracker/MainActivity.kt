package com.example.skilltracker

import android.content.Intent
import android.content.SharedPreferences
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Button
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.skilltracker.data.Skill
import com.example.skilltracker.data.populateDatabase
import com.example.skilltracker.editor.SkillEditorActivity
import com.example.skilltracker.skillDetail.SKILL_ID
import com.example.skilltracker.skillDetail.SkillDetailActivity
import com.example.skilltracker.skillList.SkillListViewModel
import com.example.skilltracker.skillList.SkillsAdapter
import com.google.android.material.snackbar.Snackbar
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import org.koin.androidx.viewmodel.ext.android.viewModel
import org.koin.core.parameter.parametersOf
import java.text.SimpleDateFormat
import java.util.Locale

const val APP_PREFERENCES = "user settings"
const val SORT_MODE_NEW = "new first"

class MainActivity : AppCompatActivity() {

    private val skillListViewModel by viewModel<SkillListViewModel> {
        parametersOf(applicationContext) // passing context to repository
    }

    private lateinit var prefs: SharedPreferences
    private lateinit var editor: SharedPreferences.Editor

    private val parser = SimpleDateFormat(
        "yyyy-MM-dd", Locale("ru", "RU")
    )

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
//        fillDB()

        prefs = getSharedPreferences(APP_PREFERENCES, MODE_PRIVATE)
        editor = prefs.edit()

        /* Handling sort mode changes. */
        findViewById<Button>(R.id.display_mode).setOnClickListener { _ ->
            val modeNewFirst = prefs.getBoolean(SORT_MODE_NEW, true)
            editor.putBoolean(SORT_MODE_NEW, !modeNewFirst).apply()
            reload()
        }

        /* Forming recycler view. */
        val recyclerView: RecyclerView = findViewById(R.id.recyclerView)
        recyclerView.layoutManager = LinearLayoutManager(this)

        val skillsAdapter = SkillsAdapter { skill -> adapterOnClick(skill) }
        recyclerView.adapter = skillsAdapter

        lifecycleScope.launch {
            lifecycle.repeatOnLifecycle(Lifecycle.State.STARTED) {
                skillListViewModel.skillsStateFlow.collect { skills ->
                    if (skills.isNotEmpty()) {
                        // updating adapter with new data sorted by user preferences
                        skillsAdapter.submitList(sortSkills(skills as MutableList<Skill>))
                    }
                }
            }
        }

        skillListViewModel.getAllSkills()

        /* Handling touch events. */
        ItemTouchHelper(object :
            ItemTouchHelper.SimpleCallback(ItemTouchHelper.UP or ItemTouchHelper.DOWN,
                ItemTouchHelper.LEFT or ItemTouchHelper.RIGHT) {

            override fun onMove(recyclerView: RecyclerView, viewHolder: RecyclerView.ViewHolder,
                                target: RecyclerView.ViewHolder): Boolean {
                return false
            }

            override fun onSwiped(viewHolder: RecyclerView.ViewHolder, swipeDir: Int) {
                val skillToSwipe = skillListViewModel.skillsStateFlow.value[viewHolder.bindingAdapterPosition]
                // deleting skill by left swipe
                if (swipeDir == ItemTouchHelper.LEFT) {
                    skillListViewModel.deleteSkill(skillToSwipe.id)

                    // check if delete operation was successful
                    lifecycleScope.launch {
                        lifecycle.repeatOnLifecycle(Lifecycle.State.STARTED) {
                            skillListViewModel.deleted.collect { deleted ->
                                deleted?.let {
                                    if(deleted) {
                                        skillsAdapter.notifyItemRemoved(viewHolder.bindingAdapterPosition)
                                    } else {
                                        val mainView = findViewById<ConstraintLayout>(R.id.main_page)
                                        Snackbar.make(mainView, R.string.message_delete_error, Snackbar.LENGTH_SHORT).show()
                                    }
                                }
                            }
                        }
                    }
                // editing skill by right swipe
                } else if(swipeDir == ItemTouchHelper.RIGHT) {
                    redirectToEditor(skillToSwipe)
                }
            }
        }).attachToRecyclerView(recyclerView)

        findViewById<Button>(R.id.button_new_skill).setOnClickListener {
            redirectToEditor()
        }
    }

    /* Opens detail info page when RecyclerView item was clicked. */
    private fun adapterOnClick(skill: Skill) {
        val intent = Intent(this, SkillDetailActivity()::class.java)
        intent.putExtra(SKILL_ID, skill.id)
        startActivity(intent)
    }

    /* Opens editor page in creating mode if button add was clicked
    * or
    * Opens editor page in editing mode if specific skill was swiped to right.*/
    private fun redirectToEditor(skill: Skill? = null) {
        val intent = Intent(this, SkillEditorActivity()::class.java)
        skill?.let {
            intent.putExtra(SKILL_ID, skill.id)
        }
        startActivity(intent)
    }

    /* Sorts skill list according to user preferences: new first/old first. */
    private fun sortSkills(skills: MutableList<Skill>) : MutableList<Skill> {
        if(prefs.getBoolean(SORT_MODE_NEW, true)) {
            skills.sortBy {
                parser.parse(it.lastEditDate)
            }
        } else {
            skills.sortByDescending {
                parser.parse(it.lastEditDate)
            }
        }
        return skills
    }

    /* Populates DB with initial data. */
    private fun fillDB() {
        CoroutineScope(Dispatchers.Main).launch {
            populateDatabase(applicationContext)
        }
    }

    private fun reload() {
        val refresh = Intent(this, MainActivity::class.java)
        startActivity(refresh)
        this.finish()
    }
}