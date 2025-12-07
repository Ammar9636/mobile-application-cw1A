package com.example.labexam3

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.labexam3.databinding.FragmentMoodJournalBinding

/**
 * Fragment for managing mood journal entries
 * Displays list of mood entries and allows adding new entries with emoji selection
 */
class MoodJournalFragment : Fragment() {

    private var _binding: FragmentMoodJournalBinding? = null
    private val binding get() = _binding!!

    private lateinit var preferenceRepository: PreferenceRepository
    private lateinit var moodAdapter: MoodAdapter
    private var moodEntries = mutableListOf<MoodEntry>()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentMoodJournalBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        preferenceRepository = PreferenceRepository(requireContext())

        setupRecyclerView()
        setupFab()
        loadMoodEntries()
        updateEmptyState()
    }

    /**
     * Sets up the RecyclerView with the mood adapter
     */
    private fun setupRecyclerView() {
        moodAdapter = MoodAdapter(
            moodEntries = moodEntries,
            onMoodShare = { moodEntry ->
                shareMoodEntry(moodEntry)
            },
            onMoodDelete = { moodEntry ->
                showDeleteConfirmationDialog(moodEntry)
            }
        )

        binding.recyclerViewMoods.apply {
            layoutManager = LinearLayoutManager(requireContext())
            adapter = moodAdapter
        }
    }

    /**
     * Sets up the floating action button for adding new mood entries
     */
    private fun setupFab() {
        binding.fabAddMood.setOnClickListener {
            showMoodEmojiSelectorDialog()
        }
    }

    /**
     * Loads mood entries from preferences and updates the adapter
     */
    private fun loadMoodEntries() {
        moodEntries.clear()
        moodEntries.addAll(preferenceRepository.loadMoodEntries())
        moodAdapter.notifyDataSetChanged()
    }

    /**
     * Updates the empty state visibility based on mood entries count
     */
    private fun updateEmptyState() {
        if (moodEntries.isEmpty()) {
            binding.textViewEmptyState.visibility = View.VISIBLE
            binding.recyclerViewMoods.visibility = View.GONE
        } else {
            binding.textViewEmptyState.visibility = View.GONE
            binding.recyclerViewMoods.visibility = View.VISIBLE
        }
    }

    /**
     * Shows the mood emoji selector dialog
     */
    private fun showMoodEmojiSelectorDialog() {
        val dialog = MoodEmojiSelectorDialog()
        dialog.setOnMoodSelectedListener { emoji, moodName, note ->
            addMoodEntry(emoji, moodName, note)
        }
        dialog.show(childFragmentManager, "MoodEmojiSelector")
    }

    /**
     * Adds a new mood entry
     * @param emoji Selected emoji
     * @param moodName Name of the mood
     * @param note Optional note
     */
    private fun addMoodEntry(emoji: String, moodName: String, note: String) {
        val moodEntry = MoodEntry(
            emoji = emoji,
            moodName = moodName,
            note = note
        )

        // Add to local list
        moodEntries.add(0, moodEntry) // Add at the beginning (newest first)
        moodAdapter.notifyItemInserted(0)

        // Save to preferences
        preferenceRepository.addMoodEntry(moodEntry)

        // Update empty state
        updateEmptyState()

        // Scroll to top to show new entry
        binding.recyclerViewMoods.scrollToPosition(0)

        // Show success message
        showSuccessMessage("Mood logged successfully! 🎉")
    }

    /**
     * Shares a mood entry using implicit intent
     * @param moodEntry The mood entry to share
     */
    private fun shareMoodEntry(moodEntry: MoodEntry) {
        val shareText = moodEntry.toShareableText()

        val shareIntent = Intent().apply {
            action = Intent.ACTION_SEND
            type = "text/plain"
            putExtra(Intent.EXTRA_TEXT, shareText)
            putExtra(Intent.EXTRA_SUBJECT, "My Mood Entry")
        }

        val chooser = Intent.createChooser(shareIntent, "Share your mood")
        startActivity(chooser)
    }

    /**
     * Shows confirmation dialog before deleting a mood entry
     * @param moodEntry The mood entry to delete
     */
    private fun showDeleteConfirmationDialog(moodEntry: MoodEntry) {
        val builder = android.app.AlertDialog.Builder(requireContext())
        builder.setTitle("Delete Mood Entry")
        builder.setMessage("Are you sure you want to delete this mood entry from ${moodEntry.getFormattedDate()}?")

        builder.setPositiveButton("Delete") { _, _ ->
            deleteMoodEntry(moodEntry)
        }

        builder.setNegativeButton("Cancel", null)
        builder.show()
    }

    /**
     * Deletes a mood entry
     * @param moodEntry The mood entry to delete
     */
    private fun deleteMoodEntry(moodEntry: MoodEntry) {
        val index = moodEntries.indexOfFirst { it.id == moodEntry.id }
        if (index >= 0) {
            moodEntries.removeAt(index)
            moodAdapter.notifyItemRemoved(index)
            preferenceRepository.deleteMoodEntry(moodEntry.id)
            updateEmptyState()
            showSuccessMessage("Mood entry deleted")
        }
    }

    /**
     * Shows a success message
     * @param message The message to show
     */
    private fun showSuccessMessage(message: String) {
        // Create a temporary TextView to show success message
        val successView = binding.textViewSuccessMessage
        successView.text = message
        successView.visibility = View.VISIBLE

        // Hide after 2 seconds
        successView.postDelayed({
            if (_binding != null) {
                successView.visibility = View.GONE
            }
        }, 2000)
    }

    /**
     * Gets mood statistics for display
     * @return formatted statistics string
     */
    private fun getMoodStats(): String {
        if (moodEntries.isEmpty()) return "No mood entries yet"

        val last7Days = moodEntries.filter {
            System.currentTimeMillis() - it.timestamp <= 7 * 24 * 60 * 60 * 1000
        }

        if (last7Days.isEmpty()) return "No mood entries in the last 7 days"

        val moodCounts = last7Days.groupBy { it.emoji }.mapValues { it.value.size }
        val mostCommon = moodCounts.maxByOrNull { it.value }

        return if (mostCommon != null) {
            "Most common mood this week: ${mostCommon.key} (${mostCommon.value} times)"
        } else {
            "${last7Days.size} entries in the last 7 days"
        }
    }

    override fun onResume() {
        super.onResume()
        // Refresh data when fragment becomes visible
        loadMoodEntries()
        updateEmptyState()

        // Update stats if available
        binding.textViewMoodStats?.text = getMoodStats()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}