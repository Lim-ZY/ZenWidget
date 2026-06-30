package com.example.zenwidget

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.zenwidget.data.AppDatabase
import com.example.zenwidget.data.RepoType
import com.example.zenwidget.ui.theme.GlassCard

@Composable
fun ZenMainScreen() {
    val context = LocalContext.current
    val database = AppDatabase.getDatabase(context)
    val dao = database.zenDao()

    var selectedRepo by remember { mutableStateOf(RepoType.QUOTES) }
    var isAddingItem by remember { mutableStateOf(false) }

    // Room's Flow automatically updates this list whenever the DB changes
    val currentItems by dao.getItemsForRepo(selectedRepo).collectAsState(initial = emptyList())

    Box(modifier = Modifier.fillMaxSize()) {
        Image(
            painter = painterResource(id = R.drawable.pexels_tree_bg), // Set your background here
            contentDescription = null,
            contentScale = ContentScale.Crop,
            modifier = Modifier.fillMaxSize()
        )

        Scaffold(
            containerColor = Color.Transparent,
            topBar = {
                GlassCard(modifier = Modifier.fillMaxWidth().padding(16.dp)) {
                    Text(
                        text = if (selectedRepo == RepoType.QUOTES) "Quotes" else "1-min Actions",
                        color = Color.White,
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold
                    )
                }
            },
            bottomBar = {
                if (!isAddingItem) {
                    GlassCard(modifier = Modifier.fillMaxWidth().padding(16.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceEvenly
                        ) {
                            TextButton(onClick = { selectedRepo = RepoType.QUOTES }) {
                                Text(
                                    "Quotes",
                                    color = if (selectedRepo == RepoType.QUOTES) Color.White else Color.White.copy(alpha = 0.5f)
                                )
                            }
                            TextButton(onClick = { selectedRepo = RepoType.ACTIONS }) {
                                Text(
                                    "1-min Actions",
                                    color = if (selectedRepo == RepoType.ACTIONS) Color.White else Color.White.copy(alpha = 0.5f)
                                )
                            }
                        }
                    }
                }
            },
            floatingActionButton = {
                if (!isAddingItem) {
                    FloatingActionButton(
                        onClick = { isAddingItem = true },
                        containerColor = Color.White.copy(alpha = 0.2f),
                        contentColor = Color.White,
                        shape = RoundedCornerShape(16.dp)
                    ) {
                        Icon(Icons.Default.Add, contentDescription = "Add")
                    }
                }
            }
        ) { paddingValues ->
            Box(modifier = Modifier.padding(paddingValues)) {
                if (isAddingItem) {
                    AddItemScreen(
                        selectedRepo = selectedRepo,
                        dao = dao,
                        onComplete = { isAddingItem = false }
                    )
                } else {
                    LazyColumn(
                        contentPadding = PaddingValues(16.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp),
                        modifier = Modifier.fillMaxSize()
                    ) {
                        items(currentItems) { item ->
                            GlassCard(modifier = Modifier.fillMaxWidth()) {
                                Column {
                                    Text(
                                        text = item.caption,
                                        color = Color.White.copy(alpha = 0.7f),
                                        style = MaterialTheme.typography.labelMedium
                                    )
                                    Spacer(modifier = Modifier.height(4.dp))
                                    Text(
                                        text = item.text,
                                        color = Color.White,
                                        style = MaterialTheme.typography.bodyLarge
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}


