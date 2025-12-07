package com.example.labexam3

import android.app.AlertDialog
import android.appwidget.AppWidgetManager
import android.content.ComponentName
import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import android.widget.LinearLayout
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.labexam3.databinding.FragmentHabitTrackerBinding
import com.google.android.material.floatingactionbutton.FloatingActionButton


class HabitTrackerFragment : Fragment() {

    private var _binding: FragmentHabitTrackerBinding? = null
    private val binding get() = _binding!!

    private lateinit var preferenceRepository: PreferenceRepository
    private lateinit var habitAdapter: HabitAdapter
    private var habits = mutableListOf<Habit>()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentHabitTrackerBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        preferenceRepository = PreferenceRepository(requireContext())

        setupRecyclerView()


        setupFab()
        loadHabits()
        updateProgressDisplay()

        // Initialize default data if first launch
        preferenceRepository.initializeDefaultData()
    }

    /**
     * Sets up the RecyclerView with the habit adapter
     */
    private fun setupRecyclerView() {
        habitAdapter = HabitAdapter(
            habits = habits,
            onHabitToggled = { habit, isCompleted ->
                if (isCompleted) {
                    habit.markCompletedToday()
                } else {
                    habit.unmarkCompletedToday()
                }
                preferenceRepository.saveHabit(habit)
                updateProgressDisplay()
                updateWidget()
            },
            onHabitEdit = { habit ->
                showEditHabitDialog(habit)
            },
            onHabitDelete = { habit ->
                showDeleteConfirmationDialog(habit)
            }
        )

        binding.recyclerViewHabits.apply {
            layoutManager = LinearLayoutManager(requireContext())
            adapter = habitAdapter
        }
    }

    /**
     * Sets up the floating action button for adding new habits
     */
    private fun setupFab() {
        binding.fabAddHabit.setOnClickListener {
            showAddHabitDialog()
        }
    }

    /**
     * Loads habits from preferences and updates the adapter
     */
    private fun loadHabits() {
        habits.clear()
        habits.addAll(preferenceRepository.loadHabits().filter { it.isActive })
        habitAdapter.notifyDataSetChanged()

        // Show empty state if no habits
        if (habits.isEmpty()) {
            binding.textViewEmptyState.visibility = View.VISIBLE
            binding.recyclerViewHabits.visibility = View.GONE
        } else {
            binding.textViewEmptyState.visibility = View.GONE
            binding.recyclerViewHabits.visibility = View.VISIBLE
        }
    }

    /**
     * Updates the progress display showing today's completion percentage
     */
    private fun updateProgressDisplay() {
        val (completed, total) = preferenceRepository.getTodayCompletionStats()
        val percentage = if (total > 0) (completed * 100) / total else 0

        binding.progressBarDaily.progress = percentage
        binding.textViewProgress.text = getString(R.string.progress_format, completed, total, percentage)

        // Update motivational message
        binding.textViewMotivation.text = when {
            percentage == 100 -> "🎉 Amazing! You completed all your habits today!"
            percentage >= 80 -> "🌟 Great job! You're almost there!"
            percentage >= 60 -> "💪 Keep going! You're doing well!"
            percentage >= 40 -> "🚀 Good start! Keep pushing forward!"
            percentage > 0 -> "🌱 Every step counts! Keep building your habits!"
            else -> "✨ Start your wellness journey today!"
        }
    }

    /**
     Shows dialog for adding a new habit
     */
    private fun showAddHabitDialog() {
        val dialogView = LayoutInflater.from(requireContext()).inflate(R.layout.dialog_add_edit_habit, null)
        val titleEditText = dialogView.findViewById<EditText>(R.id.editTextTitle)
        val descriptionEditText = dialogView.findViewById<EditText>(R.id.editTextDescription)

        AlertDialog.Builder(requireContext())
            .setTitle("Add New Habit")
            .setView(dialogView)
            .setPositiveButton("Add") { _, _ ->
                val title = titleEditText.text.toString().trim()
                val description = descriptionEditText.text.toString().trim()

                if (title.isNotEmpty()) {
                    val habit = Habit(title = title, description = description)
                    habits.add(habit)
                    preferenceRepository.saveHabit(habit)
                    habitAdapter.notifyItemInserted(habits.size - 1)
                    updateProgressDisplay()
                    updateWidget()

                    // Hide empty state if this was the first habit
                    if (habits.size == 1) {
                        binding.textViewEmptyState.visibility = View.GONE
                        binding.recyclerViewHabits.visibility = View.VISIBLE
                    }
                }
            }
            .setNegativeButton("Cancel", null)
            .show()
    }

    /**
     * Shows dialog for editing an existing habit
     * @param habit The habit to edit
     */
    private fun showEditHabitDialog(habit: Habit) {
        val dialogView = LayoutInflater.from(requireContext()).inflate(R.layout.dialog_add_edit_habit, null)
        val titleEditText = dialogView.findViewById<EditText>(R.id.editTextTitle)
        val descriptionEditText = dialogView.findViewById<EditText>(R.id.editTextDescription)

        // Pre-fill current values
        titleEditText.setText(habit.title)
        descriptionEditText.setText(habit.description)

        AlertDialog.Builder(requireContext())
            .setTitle("Edit Habit")
            .setView(dialogView)
            .setPositiveButton("Save") { _, _ ->
                val title = titleEditText.text.toString().trim()
                val description = descriptionEditText.text.toString().trim()

                if (title.isNotEmpty()) {
                    habit.title = title
                    habit.description = description
                    preferenceRepository.saveHabit(habit)

                    val index = habits.indexOfFirst { it.id == habit.id }
                    if (index >= 0) {
                        habitAdapter.notifyItemChanged(index)
                    }
                }
            }
            .setNegativeButton("Cancel", null)
            .show()
    }

    /**
     * Shows confirmation dialog before deleting a habit
     * @param habit The habit to delete
     */
    private fun showDeleteConfirmationDialog(habit: Habit) {
        AlertDialog.Builder(requireContext())
            .setTitle("Delete Habit")
            .setMessage("Are you sure you want to delete \"${habit.title}\"? This action cannot be undone.")
            .setPositiveButton("Delete") { _, _ ->
                val index = habits.indexOfFirst { it.id == habit.id }
                if (index >= 0) {
                    habits.removeAt(index)
                    habitAdapter.notifyItemRemoved(index)
                    preferenceRepository.deleteHabit(habit.id)
                    updateProgressDisplay()
                    updateWidget()

                    // Show empty state if no habits left
                    if (habits.isEmpty()) {
                        binding.textViewEmptyState.visibility = View.VISIBLE
                        binding.recyclerViewHabits.visibility = View.GONE
                    }
                }
            }
            .setNegativeButton("Cancel", null)
            .show()
    }

    /**
     * Updates the home screen widget with latest data
     */
    private fun updateWidget() {
        val context = requireContext()
        val appWidgetManager = AppWidgetManager.getInstance(context)
        val widgetComponent = ComponentName(context, HabitWidget::class.java)
        val widgetIds = appWidgetManager.getAppWidgetIds(widgetComponent)

        val intent = Intent(context, HabitWidget::class.java).apply {
            action = AppWidgetManager.ACTION_APPWIDGET_UPDATE
            putExtra(AppWidgetManager.EXTRA_APPWIDGET_IDS, widgetIds)
        }
        context.sendBroadcast(intent)
    }

    override fun onResume() {
        super.onResume()
        // Refresh data and progress when fragment becomes visible
        loadHabits()
        updateProgressDisplay()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}