package com.example.zenwidget

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.FilledIconButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.zenwidget.ui.theme.GlassCard
import com.kyant.backdrop.backdrops.LayerBackdrop
import kotlinx.coroutines.delay
import java.util.concurrent.TimeUnit

@Composable
fun PomodoroScreen(
    backdrop: LayerBackdrop
) {
    // Timer State
    var isBreak by remember { mutableStateOf(false) }
    // 25 minutes in milliseconds
    var timeLeftMs by remember { mutableLongStateOf(TimeUnit.MINUTES.toMillis(25)) }
    var isRunning by remember { mutableStateOf(false) }
    var lap by remember { mutableIntStateOf(1) }
    var isSettingsExpanded by remember { mutableStateOf(false) }

    // Colors matching your screenshot
    val primaryBlue = Color(0xFF64B5F6)

    // Timer Logic: Ticks every second when isRunning is true
    LaunchedEffect(isRunning) {
        while (isRunning && timeLeftMs > 0) {
            timeLeftMs -= 1000L
            if (timeLeftMs <= 0L) {
                isRunning = false
                isBreak = !isBreak
                timeLeftMs = if (isBreak) {
                    TimeUnit.MINUTES.toMillis(5)
                } else {
                    TimeUnit.MINUTES.toMillis(25)
                }

                if (!isBreak) lap++
                // Optional: Trigger a notification or sound here in the future
            }
            delay(1000L)
        }
    }

    // Format time to MM:SS
    val minutes = TimeUnit.MILLISECONDS.toMinutes(timeLeftMs)
    val seconds = TimeUnit.MILLISECONDS.toSeconds(timeLeftMs) - TimeUnit.MINUTES.toSeconds(minutes)
    val timeString = String.format("%02d:%02d", minutes, seconds)

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        GlassCard(
            modifier = Modifier.fillMaxWidth(),
            backdrop = backdrop
        ) {
            Column(
                modifier = Modifier.fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                PomodoroHeader(lap = lap, isBreak = isBreak)

                Spacer(modifier = Modifier.height(8.dp))

                PomodoroTimer(
                    timeString = timeString,
                    textColor = primaryBlue
                )

                PomodoroControls(
                    isRunning = isRunning,
                    primaryColor = primaryBlue,
                    isSettingsExpanded = isSettingsExpanded,
                    onToggleTimer = { isRunning = !isRunning },
                    onSkip = {
                        isRunning = false
                        isBreak = !isBreak
                        timeLeftMs = if (isBreak) {
                            TimeUnit.MINUTES.toMillis(5)
                        } else {
                            TimeUnit.MINUTES.toMillis(25)
                        }
                        if (!isBreak) lap++
                    },
                    onSettingsExpandedChange = { isSettingsExpanded = it },
                    onResetSessions = {
                        lap = 1
                        isBreak = false
                        timeLeftMs = TimeUnit.MINUTES.toMillis(25)
                        isRunning = false
                        isSettingsExpanded = false
                    }
                )
            }
        }
    }
}

@Composable
fun PomodoroHeader(lap: Int, isBreak: Boolean) {
    Text(
        text = if (!isBreak) "Lap $lap" else "Short Break",
        color = Color.White.copy(alpha = 0.7f),
        style = MaterialTheme.typography.titleLarge,
        fontWeight = FontWeight.Bold
    )
}

@Composable
fun PomodoroTimer(timeString: String, textColor: Color) {
    Text(
        text = timeString,
        color = textColor,
        fontSize = 96.sp,
        fontWeight = FontWeight.Light,
        modifier = Modifier.padding(bottom = 32.dp)
    )
}

@Composable
fun PomodoroControls(
    isRunning: Boolean,
    primaryColor: Color,
    isSettingsExpanded: Boolean,
    onToggleTimer: () -> Unit,
    onSkip: () -> Unit,
    onSettingsExpandedChange: (Boolean) -> Unit,
    onResetSessions: () -> Unit
) {
    val darkGray = Color(0xFF424242)

    Row(
        horizontalArrangement = Arrangement.spacedBy(16.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        // 1. Play/Pause Button
        FilledIconButton(
            onClick = onToggleTimer,
            modifier = Modifier.size(72.dp), // Slightly larger main button
            shape = CircleShape,
            colors = IconButtonDefaults.filledIconButtonColors(containerColor = primaryColor)
        ) {
            Icon(
                painter = painterResource (id = if (isRunning) R.drawable.ic_symbol_pause else R.drawable.ic_symbol_play),
                contentDescription = "Play/Pause",
                tint = Color.White,
                modifier = Modifier.size(32.dp)
            )
        }

        // 2. Skip Button
        FilledIconButton(
            onClick = onSkip,
            modifier = Modifier.size(56.dp),
            shape = CircleShape,
            colors = IconButtonDefaults.filledIconButtonColors(containerColor = darkGray)
        ) {
            Icon(
                painter = painterResource(id = R.drawable.ic_symbol_skip),
                contentDescription = "Skip",
                tint = Color.White
            )
        }

        // 3. Settings Dropdown Button
        Box {
            FilledIconButton(
                onClick = { onSettingsExpandedChange(true) },
                modifier = Modifier.size(56.dp),
                shape = CircleShape,
                colors = IconButtonDefaults.filledIconButtonColors(containerColor = darkGray)
            ) {
                Icon(
                    painter = painterResource(id = R.drawable.ic_symbol_menu),
                    contentDescription = "Settings",
                    tint = Color.White
                )
            }

            // Dropdown Menu
            DropdownMenu(
                expanded = isSettingsExpanded,
                onDismissRequest = { onSettingsExpandedChange(false) },
                modifier = Modifier.background(Color(0xFF2C2C2C))
            ) {
                DropdownMenuItem(
                    text = { Text("Reset Sessions", color = Color.White) },
                    onClick = onResetSessions
                )
                DropdownMenuItem(
                    text = { Text("Preferences", color = Color.White) },
                    onClick = {
                        // TODO: Open preferences dialog/screen
                        onSettingsExpandedChange(false)
                    }
                )
            }
        }
    }
}