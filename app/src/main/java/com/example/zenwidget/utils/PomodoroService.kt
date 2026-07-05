package com.example.zenwidget.utils

import android.Manifest
import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.Service
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.content.pm.ServiceInfo
import android.os.IBinder
import androidx.core.app.NotificationCompat
import androidx.core.content.ContextCompat
import com.example.zenwidget.DndManager
import com.example.zenwidget.R
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import java.util.concurrent.TimeUnit

class PomodoroService : Service() {
    private val serviceScope = CoroutineScope(Dispatchers.Main + SupervisorJob())
    private var timerJob: Job? = null

    companion object {
        private val _timeLeftMs = MutableStateFlow(TimeUnit.MINUTES.toMillis(25))
        val timeLeftMs: StateFlow<Long> = _timeLeftMs

        private val _isRunning = MutableStateFlow(false)
        val isRunning: StateFlow<Boolean> = _isRunning

        private val _isBreak = MutableStateFlow(false)
        val isBreak: StateFlow<Boolean> = _isBreak

        private val _lap = MutableStateFlow(1)
        val lap: StateFlow<Int> = _lap

        const val CHANNEL_ID = "pomodoro_channel"
        const val NOTIFICATION_ID = 1001
    }

    override fun onCreate() {
        super.onCreate()
        createNotificationChannel()
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        when (intent?.action) {
            "START" -> startTimer()
            "PAUSE" -> pauseTimer()
            "SKIP" -> advanceToNextPhase()
            "RESET" -> resetSessions()
        }
        return START_NOT_STICKY
    }

    private fun startTimer() {
        if (_isRunning.value) return
        _isRunning.value = true

        if (!_isBreak.value && DndManager.hasPermission(this)) {
            DndManager.setDoNotDisturb(this, enable = true)
        }

        startForeground(
            NOTIFICATION_ID,
            buildNotification("Timer running..."),
            ServiceInfo.FOREGROUND_SERVICE_TYPE_SPECIAL_USE
        )

        timerJob = serviceScope.launch {
            while (_timeLeftMs.value > 0) {
                delay(1000L)
                _timeLeftMs.value -= 1000L
                updateNotification("${formatTime(_timeLeftMs.value)} remaining")

                if (_timeLeftMs.value <= 0L) {
                    advanceToNextPhase()
                }
            }
        }
    }

    private fun pauseTimer() {
        _isRunning.value = false
        timerJob?.cancel()
        if (DndManager.hasPermission(this)) {
            DndManager.setDoNotDisturb(this, enable = false)
        }
        stopForeground(STOP_FOREGROUND_DETACH)
    }

    private fun advanceToNextPhase() {
        _isRunning.value = false
        timerJob?.cancel()
        _isBreak.value = !_isBreak.value

        _timeLeftMs.value = if (_isBreak.value) {
            if (_lap.value == 4) TimeUnit.MINUTES.toMillis(15) else TimeUnit.MINUTES.toMillis(5)
        } else {
            TimeUnit.MINUTES.toMillis(25)
        }

        if (!_isBreak.value) {
            _lap.value = if (_lap.value == 4) 1 else _lap.value + 1
        }

        if (DndManager.hasPermission(this)) {
            DndManager.setDoNotDisturb(this, enable = false)
        }
        stopForeground(STOP_FOREGROUND_DETACH)
    }

    private fun resetSessions() {
        _isRunning.value = false
        timerJob?.cancel()
        _lap.value = 1
        _isBreak.value = false
        _timeLeftMs.value = TimeUnit.MINUTES.toMillis(25)
        if (DndManager.hasPermission(this)) {
            DndManager.setDoNotDisturb(this, enable = false)
        }
        stopSelf()
    }

    // --- Helper UI Utilities ---
    private fun formatTime(ms: Long): String {
        val minutes = TimeUnit.MILLISECONDS.toMinutes(ms)
        val seconds = TimeUnit.MILLISECONDS.toSeconds(ms) - TimeUnit.MINUTES.toSeconds(minutes)
        return String.format("%02d:%02d", minutes, seconds)
    }

    private fun buildNotification(content: String): Notification {
        return NotificationCompat.Builder(this, CHANNEL_ID)
            .setContentTitle("Zen Pomodoro")
            .setContentText(content)
            .setSmallIcon(R.drawable.ic_symbol_hourglass)
            .setOngoing(true)
            .build()
    }

    private fun updateNotification(content: String) {
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.TIRAMISU) {
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.POST_NOTIFICATIONS)
                != PackageManager.PERMISSION_GRANTED
            ) {
                return
            }
        }

        val notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        notificationManager.notify(NOTIFICATION_ID, buildNotification(content))
    }

    private fun createNotificationChannel() {
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
            val channel = NotificationChannel(CHANNEL_ID, "Pomodoro Timer", NotificationManager.IMPORTANCE_LOW)
            val manager = getSystemService(NotificationManager::class.java)
            manager.createNotificationChannel(channel)
        }
    }

    override fun onDestroy() {
        serviceScope.cancel()
        super.onDestroy()
    }

    override fun onBind(intent: Intent?): IBinder? = null
}