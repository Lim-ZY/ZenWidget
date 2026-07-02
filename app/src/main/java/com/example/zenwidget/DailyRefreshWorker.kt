package com.example.zenwidget

import android.appwidget.AppWidgetManager
import android.content.ComponentName
import android.content.Context
import androidx.glance.appwidget.GlanceAppWidgetManager
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.example.zenwidget.data.Repository

class DailyRefreshWorker(
    private val context: Context,
    workerParams: WorkerParameters
) : CoroutineWorker(context, workerParams) {

    override suspend fun doWork(): Result {
        return try {
            val glanceManager = GlanceAppWidgetManager(context)
            val componentName = ComponentName(context, ZenReceiver::class.java)
            val manager = AppWidgetManager.getInstance(context)
            val appWidgetIds = manager.getAppWidgetIds(componentName)
            for (appWidgetId in appWidgetIds) {
                val glanceId = glanceManager.getGlanceIdBy(appWidgetId)
                val repo = Repository.getRepo(glanceId, context)
                repo.refresh()
                ZenWidget().update(context, glanceId) // Force-triggers provideGlance execution across instances
            }
            return Result.success()
        } catch (e: Exception) {
            Result.retry()
        }
    }
}