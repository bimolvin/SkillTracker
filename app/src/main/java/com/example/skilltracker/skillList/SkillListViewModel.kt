package com.example.skilltracker.skillList

import androidx.lifecycle.ViewModel
import com.example.skilltracker.data.Progress
import com.example.skilltracker.data.Skill
import com.example.skilltracker.repository.SkillProgressRepository
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.flow.flowOn

class SkillListViewModel(private val repository: SkillProgressRepository) : ViewModel() {

    // StateFlow for skills
    private val _skillsStateFlow = MutableStateFlow<List<Skill>>(emptyList())
    val skillsStateFlow: StateFlow<List<Skill>> get() = _skillsStateFlow

    // insert success status
    private val _inserted = MutableStateFlow<Boolean?>(null)
    val inserted: StateFlow<Boolean?> get() = _inserted

    // delete success status
    private val _deleted = MutableStateFlow<Boolean?>(null)
    val deleted: StateFlow<Boolean?> get() = _deleted

    /* Queries repository to return all skills. */
    fun getAllSkills() {
        viewModelScope.launch {
            repository.getAllSkills()
                .flowOn(Dispatchers.IO) // asynchronous work with IO
                .collect { skills ->
                    _skillsStateFlow.value = skills
                }
        }
    }

    /* Queries repository to add a skill given a formed skill and progress. */
    fun insertSkill(skill: Skill, progress: Progress) {
        viewModelScope.launch {
            repository.insertSkill(skill, progress)
                .flowOn(Dispatchers.IO)
                .collect { uri ->
                    _inserted.value = uri != null
                    getAllSkills() // update skill list after inserting
                }
        }
    }

    /* Queries repository to remove a skill that corresponds to an id. */
    fun deleteSkill(id: Long) {
        viewModelScope.launch {
            repository.deleteSkill(id)
                .flowOn(Dispatchers.IO)
                .collect { rowsDeleted ->
                    _deleted.value = rowsDeleted > 0
                    getAllSkills() // update skill list after deleting
                }
        }
    }
}
