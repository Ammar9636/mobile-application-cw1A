package com.example.labexam3

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView

/**
 * RecyclerView Adapter for displaying mood entries in the mood journal
 * Handles sharing and deletion of mood entries
 */
class MoodAdapter(
    private val moodEntries: MutableList<MoodEntry>,
    private val onMoodShare: (MoodEntry) -> Unit,
    private val onMoodDelete: (MoodEntry) -> Unit
) : RecyclerView.Adapter<MoodAdapter.MoodViewHolder>() {

    /**
     * ViewHolder class for mood entry items
     */
    inner class MoodViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val textViewEmoji: TextView = itemView.findViewById(R.id.textViewEmoji)
        val textViewMoodName: TextView = itemView.findViewById(R.id.textViewMoodName)
        val textViewNote: TextView = itemView.findViewById(R.id.textViewNote)
        val textViewDateTime: TextView = itemView.findViewById(R.id.textViewDateTime)
        val buttonShare: ImageButton = itemView.findViewById(R.id.buttonShare)
        val buttonDelete: ImageButton = itemView.findViewById(R.id.buttonDelete)

        fun bind(moodEntry: MoodEntry) {
            // Set mood emoji and name
            textViewEmoji.text = moodEntry.emoji
            textViewMoodName.text = moodEntry.moodName

            // Set date and time
            textViewDateTime.text = moodEntry.getFormattedDateTime()

            // Handle note visibility and content
            if (moodEntry.note.isNotEmpty()) {
                textViewNote.text = moodEntry.note
                textViewNote.visibility = View.VISIBLE
            } else {
                textViewNote.visibility = View.GONE
            }

            // Handle share button click
            buttonShare.setOnClickListener {
                onMoodShare(moodEntry)
            }

            // Handle delete button click
            buttonDelete.setOnClickListener {
                onMoodDelete(moodEntry)
            }

            // Add click listener to entire item for sharing (alternative way)
            itemView.setOnLongClickListener {
                onMoodShare(moodEntry)
                true
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MoodViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_mood_entry, parent, false)
        return MoodViewHolder(view)
    }

    override fun onBindViewHolder(holder: MoodViewHolder, position: Int) {
        holder.bind(moodEntries[position])
    }

    override fun getItemCount(): Int = moodEntries.size

    /**
     * Adds a new mood entry to the list
     * @param moodEntry The mood entry to add
     */
    fun addMoodEntry(moodEntry: MoodEntry) {
        moodEntries.add(0, moodEntry) // Add at the beginning (newest first)
        notifyItemInserted(0)
    }

    /**
     * Removes a mood entry from the list
     * @param moodEntry The mood entry to remove
     */
    fun removeMoodEntry(moodEntry: MoodEntry) {
        val index = moodEntries.indexOfFirst { it.id == moodEntry.id }
        if (index >= 0) {
            moodEntries.removeAt(index)
            notifyItemRemoved(index)
        }
    }

    /**
     * Updates the entire mood entries list
     * @param newMoodEntries The new list of mood entries
     */
    fun updateMoodEntries(newMoodEntries: List<MoodEntry>) {
        moodEntries.clear()
        moodEntries.addAll(newMoodEntries)
        notifyDataSetChanged()
    }

    /**
     * Gets mood entries from the last specified days
     * @param days Number of days to look back
     * @return List of mood entries from the specified period
     */
    fun getEntriesFromLastDays(days: Int): List<MoodEntry> {
        val cutoffTime = System.currentTimeMillis() - (days * 24 * 60 * 60 * 1000)
        return moodEntries.filter { it.timestamp >= cutoffTime }
    }

    /**
     * Gets the most recent mood entry
     * @return The most recent mood entry or null if no entries exist
     */
    fun getLatestMoodEntry(): MoodEntry? {
        return moodEntries.firstOrNull()
    }

    /**
     * Filters mood entries by a specific emoji
     * @param emoji The emoji to filter by
     * @return List of mood entries with the specified emoji
     */
    fun filterByEmoji(emoji: String): List<MoodEntry> {
        return moodEntries.filter { it.emoji == emoji }
    }
}