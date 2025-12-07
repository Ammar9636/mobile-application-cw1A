package com.example.labexam3

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.CheckBox
import android.widget.ImageButton
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView


class HabitAdapter(
    private val habits: MutableList<Habit>,
    private val onHabitToggled: (Habit, Boolean) -> Unit,
    private val onHabitEdit: (Habit) -> Unit,
    private val onHabitDelete: (Habit) -> Unit
) : RecyclerView.Adapter<HabitAdapter.HabitViewHolder>() {


    inner class HabitViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val checkBoxCompleted: CheckBox = itemView.findViewById(R.id.checkBoxCompleted)
        val textViewTitle: TextView = itemView.findViewById(R.id.textViewTitle)
        val textViewDescription: TextView = itemView.findViewById(R.id.textViewDescription)
        val textViewStreak: TextView = itemView.findViewById(R.id.textViewStreak)
        val buttonEdit: ImageButton = itemView.findViewById(R.id.buttonEdit)
        val buttonDelete: ImageButton = itemView.findViewById(R.id.buttonDelete)

        fun bind(habit: Habit) {
            // Set habit title and description
            textViewTitle.text = habit.title
            textViewDescription.text = habit.description

            // Show or hide description based on content
            if (habit.description.isNotEmpty()) {
                textViewDescription.visibility = View.VISIBLE
            } else {
                textViewDescription.visibility = View.GONE
            }

            // Set completion status
            checkBoxCompleted.isChecked = habit.isCompletedToday()

            // Update streak display
            val streak = habit.getStreak()
            if (streak > 0) {
                textViewStreak.text = "🔥 $streak day streak"
                textViewStreak.visibility = View.VISIBLE
            } else {
                textViewStreak.visibility = View.GONE
            }

            // Handle completion toggle
            checkBoxCompleted.setOnCheckedChangeListener { _, isChecked ->
                onHabitToggled(habit, isChecked)

                // Update streak display immediately
                val newStreak = if (isChecked) habit.getStreak() else 0
                if (newStreak > 0 && isChecked) {
                    textViewStreak.text = "🔥 $newStreak day streak"
                    textViewStreak.visibility = View.VISIBLE
                } else {
                    textViewStreak.visibility = View.GONE
                }
            }

            // Handle edit button click
            buttonEdit.setOnClickListener {
                onHabitEdit(habit)
            }

            // Handle delete button click
            buttonDelete.setOnClickListener {
                onHabitDelete(habit)
            }

            // Add visual feedback for completed habits
            if (habit.isCompletedToday()) {
                itemView.alpha = 0.7f
                textViewTitle.setTextColor(itemView.context.getColor(android.R.color.darker_gray))
            } else {
                itemView.alpha = 1.0f
                textViewTitle.setTextColor(itemView.context.getColor(android.R.color.black))
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): HabitViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_habit, parent, false)
        return HabitViewHolder(view)
    }

    override fun onBindViewHolder(holder: HabitViewHolder, position: Int) {
        holder.bind(habits[position])
    }

    override fun getItemCount(): Int = habits.size


    fun updateHabit(habit: Habit) {
        val index = habits.indexOfFirst { it.id == habit.id }
        if (index >= 0) {
            habits[index] = habit
            notifyItemChanged(index)
        }
    }


    fun removeHabit(habit: Habit) {
        val index = habits.indexOfFirst { it.id == habit.id }
        if (index >= 0) {
            habits.removeAt(index)
            notifyItemRemoved(index)
        }
    }


    fun addHabit(habit: Habit) {
        habits.add(habit)
        notifyItemInserted(habits.size - 1)
    }
}