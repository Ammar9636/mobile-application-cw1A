package com.example.labexam3

import android.app.PendingIntent
import android.appwidget.AppWidgetManager
import android.appwidget.AppWidgetProvider
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.widget.RemoteViews

/**
 * App Widget Provider for displaying habit completion progress on home screen
 * Shows today's habit completion percentage and statistics
 */
class HabitWidget : AppWidgetProvider() {

    companion object {
        private const val ACTION_WIDGET_UPDATE = "com.example.labexam3.WIDGET_UPDATE"
        private const val ACTION_WIDGET_CLICK = "com.example.labexam3.WIDGET_CLICK"
    }

    override fun onUpdate(
        context: Context,
        appWidgetManager: AppWidgetManager,
        appWidgetIds: IntArray
    ) {
        // Update each widget instance
        for (appWidgetId in appWidgetIds) {
            updateAppWidget(context, appWidgetManager, appWidgetId)
        }
    }

    override fun onReceive(context: Context?, intent: Intent?) {
        super.onReceive(context, intent)

        context ?: return
        intent ?: return

        when (intent.action) {
            ACTION_WIDGET_UPDATE -> {
                // Manual update request
                val appWidgetManager = AppWidgetManager.getInstance(context)
                val widgetComponent = ComponentName(context, HabitWidget::class.java)
                val widgetIds = appWidgetManager.getAppWidgetIds(widgetComponent)
                onUpdate(context, appWidgetManager, widgetIds)
            }
            ACTION_WIDGET_CLICK -> {
                // Widget was clicked, open the app
                val launchIntent = Intent(context, MainActivity::class.java).apply {
                    flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                }
                context.startActivity(launchIntent)
            }
            AppWidgetManager.ACTION_APPWIDGET_UPDATE -> {
                // System update
                val appWidgetManager = AppWidgetManager.getInstance(context)
                val widgetIds = intent.getIntArrayExtra(AppWidgetManager.EXTRA_APPWIDGET_IDS)
                if (widgetIds != null) {
                    onUpdate(context, appWidgetManager, widgetIds)
                }
            }
        }
    }

    override fun onEnabled(context: Context) {
        // Enter relevant functionality when the first widget is created
        super.onEnabled(context)
    }

    override fun onDisabled(context: Context) {
        // Enter relevant functionality when the last widget is disabled
        super.onDisabled(context)
    }

    /**
     * Updates a single app widget instance
     * @param context Application context
     * @param appWidgetManager AppWidgetManager instance
     * @param appWidgetId ID of the widget to update
     */
    private fun updateAppWidget(
        context: Context,
        appWidgetManager: AppWidgetManager,
        appWidgetId: Int
    ) {
        val preferenceRepository = PreferenceRepository(context)
        val (completed, total) = preferenceRepository.getTodayCompletionStats()
        val percentage = if (total > 0) (completed * 100) / total else 0

        // Create RemoteViews for the widget layout
        val views = RemoteViews(context.packageName, R.layout.widget_habit_progress)

        // Update the widget content
        updateWidgetContent(context, views, completed, total, percentage)

        // Set up click listener to open the app
        val clickIntent = Intent(context, HabitWidget::class.java).apply {
            action = ACTION_WIDGET_CLICK
        }
        val clickPendingIntent = PendingIntent.getBroadcast(
            context, 0, clickIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
        views.setOnClickPendingIntent(R.id.widget_container, clickPendingIntent)

        // Tell the AppWidgetManager to perform an update on the current app widget
        appWidgetManager.updateAppWidget(appWidgetId, views)
    }

    /**
     * Updates the widget content with current habit data
     * @param context Application context
     * @param views RemoteViews for the widget
     * @param completed Number of completed habits
     * @param total Total number of habits
     * @param percentage Completion percentage
     */
    private fun updateWidgetContent(
        context: Context,
        views: RemoteViews,
        completed: Int,
        total: Int,
        percentage: Int
    ) {
        // Update main text
        if (total > 0) {
            views.setTextViewText(
                R.id.text_habit_progress,
                "Habits: $completed/$total"
            )
            views.setTextViewText(
                R.id.text_percentage,
                "$percentage%"
            )
        } else {
            views.setTextViewText(
                R.id.text_habit_progress,
                "No habits yet"
            )
            views.setTextViewText(
                R.id.text_percentage,
                "0%"
            )
        }

        // Update progress bar
        views.setProgressBar(R.id.progress_bar, 100, percentage, false)

        // Update emoji based on progress
        val emoji = when {
            percentage == 100 -> "🎉"
            percentage >= 80 -> "🌟"
            percentage >= 60 -> "💪"
            percentage >= 40 -> "🚀"
            percentage > 0 -> "🌱"
            else -> "📝"
        }
        views.setTextViewText(R.id.text_emoji, emoji)

        // Update motivational message
        val message = when {
            percentage == 100 -> "Complete!"
            percentage >= 80 -> "Almost there!"
            percentage >= 60 -> "Great progress!"
            percentage >= 40 -> "Keep going!"
            percentage > 0 -> "Good start!"
            else -> "Start today!"
        }
        views.setTextViewText(R.id.text_motivation, message)

        // Update last updated time
        val currentTime = java.text.SimpleDateFormat("HH:mm", java.util.Locale.getDefault())
            .format(java.util.Date())
        views.setTextViewText(R.id.text_last_update, "Updated: $currentTime")
    }

    /**
     * Requests an update of all widget instances
     * @param context Application context
     */
    fun requestWidgetUpdate(context: Context) {
        val intent = Intent(context, HabitWidget::class.java).apply {
            action = ACTION_WIDGET_UPDATE
        }
        context.sendBroadcast(intent)
    }
}

/**
 * Companion object with utility methods for widget management
 */
object HabitWidgetUtils {

    /**
     * Updates all widget instances with fresh data
     * @param context Application context
     */
    fun updateAllWidgets(context: Context) {
        val appWidgetManager = AppWidgetManager.getInstance(context)
        val widgetComponent = ComponentName(context, HabitWidget::class.java)
        val widgetIds = appWidgetManager.getAppWidgetIds(widgetComponent)

        val intent = Intent(context, HabitWidget::class.java).apply {
            action = AppWidgetManager.ACTION_APPWIDGET_UPDATE
            putExtra(AppWidgetManager.EXTRA_APPWIDGET_IDS, widgetIds)
        }
        context.sendBroadcast(intent)
    }

    /**
     * Checks if any widget instances are active
     * @param context Application context
     * @return true if widgets are active, false otherwise
     */
    fun hasActiveWidgets(context: Context): Boolean {
        val appWidgetManager = AppWidgetManager.getInstance(context)
        val widgetComponent = ComponentName(context, HabitWidget::class.java)
        val widgetIds = appWidgetManager.getAppWidgetIds(widgetComponent)
        return widgetIds.isNotEmpty()
    }

    /**
     * Forces a manual update of all widgets
     * This method can be called when habit data changes
     * @param context Application context
     */
    fun forceWidgetUpdate(context: Context) {
        val widget = HabitWidget()
        widget.requestWidgetUpdate(context)
    }
}