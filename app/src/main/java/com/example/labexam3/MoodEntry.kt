package com.example.labexam3

import java.text.SimpleDateFormat
import java.util.*

/**
 * Data class representing a mood journal entry
 * Contains mood information, emoji, note, and timestamp
 */
data class MoodEntry(
    val id: String = UUID.randomUUID().toString(),
    val emoji: String,
    val moodName: String,
    val note: String = "",
    val timestamp: Long = System.currentTimeMillis()
) {

    /**
     * Gets formatted date string for display
     * @return formatted date (MMM dd, yyyy)
     */
    fun getFormattedDate(): String {
        val formatter = SimpleDateFormat("MMM dd, yyyy", Locale.getDefault())
        return formatter.format(Date(timestamp))
    }

    /**
     * Gets formatted time string for display
     * @return formatted time (HH:mm)
     */
    fun getFormattedTime(): String {
        val formatter = SimpleDateFormat("HH:mm", Locale.getDefault())
        return formatter.format(Date(timestamp))
    }

    /**
     * Gets formatted date and time for display
     * @return formatted datetime string
     */
    fun getFormattedDateTime(): String {
        val formatter = SimpleDateFormat("MMM dd, yyyy 'at' HH:mm", Locale.getDefault())
        return formatter.format(Date(timestamp))
    }

    /**
     * Creates a shareable text representation of the mood entry
     * @return formatted string for sharing
     */
    fun toShareableText(): String {
        return buildString {
            append("My mood on ${getFormattedDate()}: $emoji $moodName")
            if (note.isNotEmpty()) {
                append("\n\nNote: $note")
            }
            append("\n\nLogged at ${getFormattedTime()}")
            append("\n\n#PersonalWellness #MoodTracking")
        }
    }

    companion object {
        /**
         * Available mood options with their emojis
         */
        val MOOD_OPTIONS = listOf(
            "😄" to "Very Happy",
            "😊" to "Happy",
            "😐" to "Neutral",
            "😔" to "Sad",
            "😢" to "Very Sad",
            "😴" to "Tired",
            "😤" to "Frustrated",
            "🤗" to "Grateful",
            "😰" to "Anxious",
            "💪" to "Energetic"
        )

        /**
         * Gets mood name by emoji
         * @param emoji the emoji to search for
         * @return the mood name or "Unknown" if not found
         */
        fun getMoodName(emoji: String): String {
            return MOOD_OPTIONS.find { it.first == emoji }?.second ?: "Unknown"
        }
    }
}