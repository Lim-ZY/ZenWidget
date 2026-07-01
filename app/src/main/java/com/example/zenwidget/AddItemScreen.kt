package com.example.zenwidget

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.glance.appwidget.updateAll
import com.example.zenwidget.data.RepoItem
import com.example.zenwidget.data.RepoType
import com.example.zenwidget.data.ZenDao
import com.example.zenwidget.ui.theme.GlassCard
import com.kyant.backdrop.backdrops.LayerBackdrop
import kotlinx.coroutines.launch

@Composable
fun AddItemScreen(
    backdrop: LayerBackdrop,
    selectedRepo: RepoType,
    dao: ZenDao,
    onComplete: () -> Unit
) {
    BackHandler(enabled = true) {
        onComplete()
    }

    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()

    var targetRepo by remember { mutableStateOf(selectedRepo) }
    var inputText by remember { mutableStateOf("") }
    var captionText by remember { mutableStateOf("") }

    val scrollState = rememberScrollState()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .imePadding()
            .verticalScroll(scrollState)
            .padding(16.dp),
        verticalArrangement = Arrangement.Center
    ) {
        GlassCard(
            modifier = Modifier.fillMaxWidth(),
            backdrop = backdrop) {
            Column {
                Text(
                    "Add to Widget",
                    color = Color.White,
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold
                )

                Spacer(modifier = Modifier.height(24.dp))

                Text(
                    text = "Target Repository",
                    color = Color.White,
                    style = MaterialTheme.typography.labelMedium
                )
                Spacer(modifier = Modifier.height(8.dp))

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(12.dp))
                        .background(Color.White.copy(alpha = 0.1f))
                        .border(1.dp, Color.White.copy(alpha = 0.65f), RoundedCornerShape(12.dp)),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Option 1
                    TextButton(
                        onClick = { targetRepo = RepoType.QUOTES },
                        modifier = Modifier
                            .weight(1f)
                            .background(if (targetRepo == RepoType.QUOTES) Color.White.copy(alpha = 0.4f) else Color.Transparent)
                    ) {
                        Text(
                            text = "Quotes",
                            color = Color.White
                        )
                    }

                    // Divider Line
                    Box(
                        modifier = Modifier
                            .width(1.dp)
                            .height(40.dp)
                            .background(Color.White.copy(alpha = 0.3f))
                    )

                    // Option 2
                    TextButton(
                        onClick = { targetRepo = RepoType.ACTIONS },
                        modifier = Modifier
                            .weight(1f)
                            .background(if (targetRepo == RepoType.ACTIONS) Color.White.copy(alpha = 0.4f) else Color.Transparent)
                    ) {
                        Text(
                            text = "1-min Actions",
                            color = Color.White
                        )
                    }
                }

                Spacer(modifier = Modifier.height(20.dp))

                OutlinedTextField(
                    value = inputText,
                    onValueChange = { inputText = it },
                    label = { Text("Main Text", color = Color.White) },
                    modifier = Modifier.fillMaxWidth(),
                    colors = TextFieldDefaults.colors(
                        unfocusedContainerColor = Color.Transparent,
                        focusedContainerColor = Color.Transparent,
                        unfocusedTextColor = Color.White,
                        focusedTextColor = Color.White
                    )
                )

                Spacer(modifier = Modifier.height(8.dp))

                OutlinedTextField(
                    value = captionText,
                    onValueChange = { captionText = it },
                    label = { Text("Caption", color = Color.White) },
                    modifier = Modifier.fillMaxWidth(),
                    colors = TextFieldDefaults.colors(
                        unfocusedContainerColor = Color.Transparent,
                        focusedContainerColor = Color.Transparent,
                        unfocusedTextColor = Color.White,
                        focusedTextColor = Color.White
                    )
                )

                Spacer(modifier = Modifier.height(24.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    TextButton(onClick = onComplete) {
                        Text("Cancel", color = Color.White)
                    }
                    Button(
                        onClick = {
                            if (inputText.isNotBlank()) {
                                coroutineScope.launch {
                                    // 1. Write safely to disk
                                    dao.insertItem(
                                        RepoItem(
                                            repoType = targetRepo,
                                            text = inputText,
                                            caption = captionText
                                        )
                                    )
                                    // Update widget and close screen
                                    ZenWidget().updateAll(context)
                                    onComplete()
                                }
                            }
                        }
                    ) {
                        Text("Save")
                    }
                }
            }
        }
    }
}
