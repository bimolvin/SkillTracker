package com.example.skilltracker.skillList

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.skilltracker.R
import com.example.skilltracker.data.Skill

class SkillsAdapter(private val onClick: (Skill) -> Unit) :
    ListAdapter<Skill, SkillsAdapter.ViewHolder>(SkillDiffCallback) {

        class ViewHolder(view: View, onClick: (Skill) -> Unit) : RecyclerView.ViewHolder(view) {
        private val title: TextView = view.findViewById(R.id.card_title)
        private val description: TextView = view.findViewById(R.id.description)
        private val date: TextView = view.findViewById(R.id.date)
        private var currentSkill: Skill? = null
        init {
            /* Define click listener for the ViewHolder's View. */
            itemView.setOnClickListener {
                currentSkill?.let {
                    onClick(it)
                }
            }
        }
        fun bind(skill: Skill) {
            currentSkill = skill
            title.text = skill.name
            description.text = skill.description
            date.text = skill.lastEditDate
        }
    }
    /* Create new views (invoked by the layout manager). */
    override fun onCreateViewHolder(viewGroup: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(viewGroup.context)
            .inflate(R.layout.recyclerview_item, viewGroup, false)
        return ViewHolder(view, onClick)
    }
    /* Replace the contents of a view (invoked by the layout manager). */
    override fun onBindViewHolder(viewHolder: ViewHolder, position: Int) {
        val article = getItem(position)
        viewHolder.bind(article)
    }
}
object SkillDiffCallback : DiffUtil.ItemCallback<Skill>() {
    override fun areItemsTheSame(oldItem: Skill, newItem: Skill): Boolean {
        return oldItem == newItem
    }
    override fun areContentsTheSame(oldItem: Skill, newItem: Skill): Boolean {
        return oldItem.id == newItem.id
    }
}
