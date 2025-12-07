package com.example.labexam3

import java.util.*


data class Habit(
    val id: String = UUID.randomUUID().toString(),
    var title: String,
    var description: String = "",
    val createdDate: Long = System.currentTimeMillis(),
    var isActive: Boolean = true,
    var completedDates: MutableSet<String> = mutableSetOf() // Store dates in "yyyy-MM-dd" format
) {


    fun markCompletedToday() {
        val today = getCurrentDateString()
        completedDates.add(today)
    }


    fun unmarkCompletedToday() {
        val today = getCurrentDateString()
        completedDates.remove(today)
    }


    fun isCompletedToday(): Boolean {
        val today = getCurrentDateString()
        return completedDates.contains(today)
    }


    fun getStreak(): Int {
        val today = java.text.SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
        val calendar = Calendar.getInstance()
        var streak = 0

        while (true) {
            val dateString = today.format(calendar.time)
            if (completedDates.contains(dateString)) {
                streak++
                calendar.add(Calendar.DAY_OF_YEAR, -1)
            } else {
                break
            }
        }

        return streak
    }


    private fun getCurrentDateString(): String {
        val formatter = java.text.SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
        return formatter.format(Date())
    }
}