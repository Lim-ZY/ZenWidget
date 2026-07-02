package com.example.zenwidget

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Scaffold
import androidx.compose.ui.Modifier
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import com.example.zenwidget.ui.theme.ZenWidgetTheme
import java.util.concurrent.TimeUnit

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        scheduleDailyWidgetRefresh()
        setContent {
            ZenWidgetTheme {
                Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
                    ZenMainScreen()
                }
            }
        }
    }

    private fun scheduleDailyWidgetRefresh() {
        val dailyRefreshRequest = PeriodicWorkRequestBuilder<DailyRefreshWorker>(
            24, TimeUnit.HOURS
        ).build()

        WorkManager.getInstance(applicationContext).enqueueUniquePeriodicWork(
            "DailyZenWidgetRefresh",
            ExistingPeriodicWorkPolicy.KEEP, // Do not reset the 24h timer if already scheduled
            dailyRefreshRequest
        )
    }
}
