package com.example.zenwidget

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBars
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
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
import com.kyant.backdrop.backdrops.layerBackdrop
import com.kyant.backdrop.backdrops.rememberLayerBackdrop
import kotlinx.coroutines.launch

@Composable
fun ZenMainScreen() {
    val context = LocalContext.current
    val database = AppDatabase.getDatabase(context)
    val dao = database.zenDao()

    // 0 for Quotes, 1 for Actions
    val pagerState = rememberPagerState(pageCount = { 2 })
    val selectedRepo = if (pagerState.currentPage == 0) RepoType.QUOTES else RepoType.ACTIONS
    var isAddingItem by remember { mutableStateOf(false) }
    val scope = rememberCoroutineScope()

    val backdrop = rememberLayerBackdrop() // For liquid glass API

    Box(modifier = Modifier.fillMaxSize()) {
        Image(
            painter = painterResource(id = R.drawable.pexels_sun_bg),
            contentDescription = null,
            contentScale = ContentScale.Crop,
            modifier = Modifier
                .fillMaxSize()
                .layerBackdrop(backdrop)
        )

        Scaffold(
            containerColor = Color.Transparent,
            topBar = {
                GlassCard(
                    modifier = Modifier
                        .windowInsetsPadding(WindowInsets.statusBars)
                        .padding(start = 16.dp, end = 16.dp, top = 8.dp, bottom = 16.dp),
                    backdrop = backdrop
                ) {
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
                    GlassCard(
                        modifier = Modifier.fillMaxWidth().padding(16.dp),
                        backdrop = backdrop
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            TextButton(onClick = {
                                scope.launch { pagerState.animateScrollToPage(0) }
                            }) {
                                Text(
                                    "Quotes",
                                    color = if (selectedRepo == RepoType.QUOTES) Color.White else Color.White.copy(alpha = 0.5f),
                                    style = MaterialTheme.typography.titleLarge,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                            TextButton(onClick = {
                                scope.launch { pagerState.animateScrollToPage(1) }
                            }) {
                                Text(
                                    "1-min Actions",
                                    color = if (selectedRepo == RepoType.ACTIONS) Color.White else Color.White.copy(alpha = 0.5f),
                                    style = MaterialTheme.typography.titleLarge,
                                    fontWeight = FontWeight.Bold
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
                        backdrop = backdrop,
                        selectedRepo = selectedRepo,
                        dao = dao,
                        onComplete = { isAddingItem = false }
                    )
                } else {
                    HorizontalPager(
                        state = pagerState,
                        modifier = Modifier.fillMaxSize()
                    ) { page ->
                        val currentRepo = if (page == 0) RepoType.QUOTES else RepoType.ACTIONS
                        val currentItems by dao.getItemsForRepo(currentRepo).collectAsState(initial = emptyList())

                        LazyColumn(
                            contentPadding = PaddingValues(16.dp),
                            verticalArrangement = Arrangement.spacedBy(12.dp),
                            modifier = Modifier.fillMaxSize()
                        ) {
                            items(currentItems) { item ->
                                GlassCard(
                                    modifier = Modifier.fillMaxWidth(),
                                    backdrop = backdrop
                                ) {
                                    Column {
                                        Text(
                                            text = item.text,
                                            color = Color.White,
                                            style = MaterialTheme.typography.bodyLarge,
                                            fontWeight = FontWeight.Bold
                                        )
                                        Spacer(modifier = Modifier.height(4.dp))
                                        Text(
                                            text = item.caption,
                                            color = Color.White.copy(alpha = 0.9f),
                                            style = MaterialTheme.typography.labelMedium,
                                            fontWeight = FontWeight.Normal
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
}


