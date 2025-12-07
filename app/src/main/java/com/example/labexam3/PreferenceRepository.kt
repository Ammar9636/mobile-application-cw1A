package com.example.labexam3

import android.content.Context
import android.content.SharedPreferences
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken

/**
 * Repository class to handle all SharedPreferences operations
 * Uses Gson to serialize/deserialize objects to JSON for storage
 */
class PreferenceRepository(context: Context) {

    private val preferences: SharedPreferences = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
    private val gson = Gson()

    companion object {
        private const val PREFS_NAME = "wellness_app_prefs"
        private const val KEY_HABITS = "habits"
        private const val KEY_MOOD_ENTRIES = "mood_entries"
        private const val KEY_HYDRATION_INTERVAL = "hydration_interval"
        private const val KEY_HYDRATION_ENABLED = "hydration_enabled"
        private const val KEY_FIRST_LAUNCH = "first_launch"

        // Default hydration reminder interval in minutes
        const val DEFAULT_HYDRATION_INTERVAL = 120 // 2 hours
    }

    /**
     * Saves a list of habits to SharedPreferences
     * @param habits List of habits to save
     */
    fun saveHabits(habits: List<Habit>) {
        val json = gson.toJson(habits)
        preferences.edit().putString(KEY_HABITS, json).apply()
    }

    /**
     * Loads the list of habits from SharedPreferences
     * @return List of habits, or empty list if none found
     */
    fun loadHabits(): MutableList<Habit> {
        val json = preferences.getString(KEY_HABITS, null) ?: return mutableListOf()
        val type = object : TypeToken<List<Habit>>() {}.type
        val habits = gson.fromJson<List<Habit>>(json, type) ?: return mutableListOf()
        return habits.toMutableList()
    }

    /**
     * Saves a single habit (updates existing or adds new)
     * @param habit The habit to save
     */
    fun saveHabit(habit: Habit) {
        val habits = loadHabits()
        val existingIndex = habits.indexOfFirst { it.id == habit.id }

        if (existingIndex >= 0) {
            habits[existingIndex] = habit
        } else {
            habits.add(habit)
        }

        saveHabits(habits)
    }

    /**
     * Deletes a habit by ID
     * @param habitId The ID of the habit to delete
     */
    fun deleteHabit(habitId: String) {
        val habits = loadHabits()
        habits.removeAll { it.id == habitId }
        saveHabits(habits)
    }

    /**
     * Saves a list of mood entries to SharedPreferences
     * @param entries List of mood entries to save
     */
    fun saveMoodEntries(entries: List<MoodEntry>) {
        val json = gson.toJson(entries)
        preferences.edit().putString(KEY_MOOD_ENTRIES, json).apply()
    }

    /**
     * Loads the list of mood entries from SharedPreferences
     * @return List of mood entries sorted by timestamp (newest first)
     */
    fun loadMoodEntries(): MutableList<MoodEntry> {
        val json = preferences.getString(KEY_MOOD_ENTRIES, null) ?: return mutableListOf()
        val type = object : TypeToken<List<MoodEntry>>() {}.type
        val entries = gson.fromJson<List<MoodEntry>>(json, type) ?: return mutableListOf()
        return entries.sortedByDescending { it.timestamp }.toMutableList()
    }

    /**
     * Adds a new mood entry
     * @param entry The mood entry to add
     */
    fun addMoodEntry(entry: MoodEntry) {
        val entries = loadMoodEntries()
        entries.add(0, entry) // Add at the beginning (newest first)
        saveMoodEntries(entries)
    }

    /**
     * Deletes a mood entry by ID
     * @param entryId The ID of the mood entry to delete
     */
    fun deleteMoodEntry(entryId: String) {
        val entries = loadMoodEntries()
        entries.removeAll { it.id == entryId }
        saveMoodEntries(entries)
    }

    /**
     * Gets the hydration reminder interval in minutes
     * @return interval in minutes
     */
    fun getHydrationInterval(): Int {
        return preferences.getInt(KEY_HYDRATION_INTERVAL, DEFAULT_HYDRATION_INTERVAL)
    }

    /**
     * Sets the hydration reminder interval
     * @param intervalMinutes interval in minutes
     */
    fun setHydrationInterval(intervalMinutes: Int) {
        preferences.edit().putInt(KEY_HYDRATION_INTERVAL, intervalMinutes).apply()
    }

    /**
     * Checks if hydration reminders are enabled
     * @return true if enabled, false otherwise
     */
    fun isHydrationEnabled(): Boolean {
        return preferences.getBoolean(KEY_HYDRATION_ENABLED, true)
    }

    /**
     * Enables or disables hydration reminders
     * @param enabled true to enable, false to disable
     */
    fun setHydrationEnabled(enabled: Boolean) {
        preferences.edit().putBoolean(KEY_HYDRATION_ENABLED, enabled).apply()
    }

    /**
     * Checks if this is the first app launch
     * @return true if first launch, false otherwise
     */
    fun isFirstLaunch(): Boolean {
        return preferences.getBoolean(KEY_FIRST_LAUNCH, true)
    }

    /**
     * Marks the first launch as completed
     */
    fun setFirstLaunchCompleted() {
        preferences.edit().putBoolean(KEY_FIRST_LAUNCH, false).apply()
    }

    /**
     * Initializes the app with default data on first launch
     */
    fun initializeDefaultData() {
        if (isFirstLaunch()) {
            // Add some default habits
            val defaultHabits = listOf(
                Habit(title = "Drink 8 glasses of water", description = "Stay hydrated throughout the day"),
                Habit(title = "Take 10,000 steps", description = "Walk for better health"),
                Habit(title = "Meditate for 10 minutes", description = "Practice mindfulness daily"),
                Habit(title = "Read for 30 minutes", description = "Expand knowledge and relax"),
                Habit(title = "Get 8 hours of sleep", description = "Ensure proper rest")
            )
            saveHabits(defaultHabits)

            setFirstLaunchCompleted()
        }
    }

    /**
     * Gets today's habit completion percentage
     * @return completion percentage as integer (0-100)
     */
    fun getTodayCompletionPercentage(): Int {
        val habits = loadHabits().filter { it.isActive }
        if (habits.isEmpty()) return 0

        val completedCount = habits.count { it.isCompletedToday() }
        return (completedCount * 100) / habits.size
    }

    /**
     * Gets today's completion stats
     * @return Pair of (completed count, total count)
     */
    fun getTodayCompletionStats(): Pair<Int, Int> {
        val habits = loadHabits().filter { it.isActive }
        val completedCount = habits.count { it.isCompletedToday() }
        return Pair(completedCount, habits.size)
    }
}