package com.example.skilltracker.skillDetail

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.skilltracker.data.Progress
import com.example.skilltracker.data.Skill
import com.example.skilltracker.repository.SkillProgressRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.launch

class SkillDetailViewModel(private val repository: SkillProgressRepository) : ViewModel() {

    // current skill
    private val _skillStateFlow = MutableStateFlow<Skill?>(null)
    val skillStateFlow: StateFlow<Skill?> get() = _skillStateFlow

    // progress of a current skill
    private val _progressStateFlow = MutableStateFlow<Progress?>(null)
    val progressStateFlow: StateFlow<Progress?> get() = _progressStateFlow

    // update success status
    private val _updated = MutableStateFlow<Boolean?>(null)
    val updated: StateFlow<Boolean?> get() = _updated

    /* Queries repository to return a skill that corresponds to an id. */
    fun getSkillById(id: Long) {
        viewModelScope.launch {
            repository.getSkillById(id)
                .flowOn(Dispatchers.IO) // asynchronous work with IO
                .collect { skill ->
                    _skillStateFlow.value = skill
                    skill?.let {
                        getProgressBySkillId(skill.id) // get progress if skill was found
                    }
                }
        }
    }

    /* Queries repository to return a progress that corresponds to a skill id. */
    private fun getProgressBySkillId(skillId: Long) {
        viewModelScope.launch {
            repository.getProgressBySkillId(skillId)
                .flowOn(Dispatchers.IO) // asynchronous work with IO
                .collect { progress ->
                    _progressStateFlow.value = progress
                }
        }
    }

    /* Queries repository to edit a skill. */
    fun updateSkill(skill: Skill) {
        viewModelScope.launch {
            repository.updateSkill(skill)
                .flowOn(Dispatchers.IO)
                .collect { rowsUpdated ->
                    Log.d("VM rowsUpdated", rowsUpdated.toString())
                    _updated.value = rowsUpdated > 0
                    getSkillById(skill.id) // update current skill after editing
                }
        }
    }

    /* Queries repository to edit a progress. */
    fun updateProgress(progress: Progress) {
        viewModelScope.launch {
            repository.updateProgress(progress)
                .flowOn(Dispatchers.IO)
                .collect { rowsUpdated ->
                    _updated.value = rowsUpdated > 0
                    getProgressBySkillId(progress.skillId) // update current progress after editing
                }
        }
    }
}