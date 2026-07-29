package com.example.zenwidget

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBars
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.PagerState
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Checkbox
import androidx.compose.material3.CheckboxDefaults
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.FloatingActionButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.example.zenwidget.data.AppDatabase
import com.example.zenwidget.data.RepoItem
import com.example.zenwidget.data.RepoType
import com.example.zenwidget.ui.theme.GlassCard
import com.kyant.backdrop.backdrops.LayerBackdrop
import com.kyant.backdrop.backdrops.layerBackdrop
import com.kyant.backdrop.backdrops.rememberLayerBackdrop
import kotlinx.coroutines.launch

@Composable
fun ZenMainScreen() {
    val context = LocalContext.current
    val database = AppDatabase.getDatabase(context)
    val dao = database.zenDao()

    // 0 for Quotes, 1 for Actions, 2 for Pomodoro
    val pagerState = rememberPagerState(pageCount = { 3 })
    var isAddingItem by remember { mutableStateOf(false) }
    var isSelectionMode by remember { mutableStateOf(false) }
    var selectedItems by remember { mutableStateOf(emptySet<Any>()) }
    val scope = rememberCoroutineScope()
    val backdrop = rememberLayerBackdrop() // For liquid glass API

    val quotes by dao.getItemsForRepo(RepoType.QUOTES).collectAsState(initial = emptyList())
    val actions by dao.getItemsForRepo(RepoType.ACTIONS).collectAsState(initial = emptyList())

    val currentItems = if (pagerState.currentPage == 0) quotes else actions
    var totalCount = currentItems.size
    var selectedCount = selectedItems.size

    Box(modifier = Modifier.fillMaxSize()) {
        // Background Image
        Image(
            painter = painterResource(id = R.drawable.pexels_water_bg),
            contentDescription = null,
            contentScale = ContentScale.Crop,
            modifier = Modifier
                .fillMaxSize()
                .layerBackdrop(backdrop)
        )

        Scaffold(
            containerColor = Color.Transparent,
            topBar = {
                ZenTopBar(
                    backdrop = backdrop,
                    currentPage = pagerState.currentPage,
                    isAddingItem = isAddingItem,
                    isSelectionMode = isSelectionMode,
                    selectedCount = selectedCount,
                    totalCount = totalCount,
                    onToggleSelectAll = {
                        scope.launch {
                            selectedItems = if (selectedCount == totalCount) emptySet() else currentItems.toSet()
                        }
                    })
            },
            bottomBar = {
                ZenBottomBar(
                    isAddingItem = isAddingItem,
                    currentPage = pagerState.currentPage,
                    backdrop = backdrop,
                    isSelectionMode = isSelectionMode,
                    onCancel = {
                        isSelectionMode = false
                        selectedItems = emptySet()
                    },
                    onDelete = {
                        scope.launch {
                            selectedItems.forEach { item -> dao.deleteItem(item as RepoItem) }
                            isSelectionMode = false
                            selectedItems = emptySet()
                        }
                    },
                    onNavigate = { targetPage ->
                        isSelectionMode = false
                        selectedItems = emptySet()
                        scope.launch { pagerState.animateScrollToPage(targetPage) }
                    }
                )
            },
            floatingActionButton = {
                ZenFab(isAddingItem, pagerState.currentPage, isSelectionMode) { isAddingItem = true }
            }
        ) { paddingValues ->
            Box(modifier = Modifier.padding(paddingValues)) {
                if (isAddingItem) {
                    AddItemScreen(
                        backdrop = backdrop,
                        selectedRepo = if (pagerState.currentPage == 0) RepoType.QUOTES else RepoType.ACTIONS,
                        dao = dao,
                        onComplete = { isAddingItem = false }
                    )
                } else {
                    ZenPagerContent(
                        pagerState = pagerState,
                        currentItems = currentItems,
                        backdrop = backdrop,
                        isSelectionMode = isSelectionMode,
                        selectedItems = selectedItems,
                        onToggleSelection = { item ->
                            selectedItems = if (selectedItems.contains(item)) {
                                selectedItems - item
                            } else {
                                selectedItems + item
                            }
                        },
                        onLongPress = { item ->
                            isSelectionMode = true
                            selectedItems = setOf(item)
                        })
                }
            }
        }
    }
}

@Composable
fun ZenTopBar(
    backdrop: LayerBackdrop,
    currentPage: Int,
    isAddingItem: Boolean,
    isSelectionMode: Boolean,
    selectedCount: Int,
    totalCount: Int,
    onToggleSelectAll: () -> Unit
) {
    if (isSelectionMode) {
        val isAllSelected = totalCount > 0 && selectedCount == totalCount

        Row(
            modifier = Modifier
                .windowInsetsPadding(WindowInsets.statusBars)
                .padding(start = 8.dp, end = 8.dp, bottom = 16.dp)
                .fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            GlassCard(
                modifier = Modifier.clip(RoundedCornerShape(20.dp)),
                backdrop = backdrop
            ) {
                Text(
                    text = if (selectedCount == 0) "Select Item" else "$selectedCount Selected",
                    color = Color.White,
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                )
            }
            GlassCard(
                modifier = Modifier.clip(RoundedCornerShape(20.dp)).clickable { onToggleSelectAll() },
                backdrop = backdrop
            ) {
                Text(
                    text = if (isAllSelected) "Deselect all" else "Select all",
                    color = Color.White,
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold)
            }
        }
    } else if (!isAddingItem) {
        GlassCard(
            modifier = Modifier
                .windowInsetsPadding(WindowInsets.statusBars)
                .padding(start = 8.dp, end = 8.dp, bottom = 16.dp),
            backdrop = backdrop
        ) {
            val titleText = when (currentPage) {
                0 -> "Quotes"
                1 -> "1-min Actions"
                else -> "Focus"
            }

            Text(
                text = titleText,
                color = Color.White,
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold
            )
        }
    }
}

@Composable
fun ZenBottomBar(
    isAddingItem: Boolean,
    currentPage: Int,
    backdrop: LayerBackdrop,
    isSelectionMode: Boolean,
    onCancel: () -> Unit,
    onDelete: () -> Unit,
    onNavigate: (Int) -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .windowInsetsPadding(WindowInsets.navigationBars)
            .padding(start = 8.dp, end = 8.dp)
    ) {
        if (isSelectionMode) {
            Row(
                modifier = Modifier
                    .fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                GlassCard(
                    modifier = Modifier
                        .weight(1f)
                        .clip(RoundedCornerShape(20.dp))
                        .clickable { onCancel() },
                    backdrop = backdrop
                ) {
                    Text(
                        text = "Cancel",
                        color = Color.White,
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
                        textAlign = TextAlign.Center
                    )
                }

                GlassCard(
                    modifier = Modifier
                        .weight(1f)
                        .clip(RoundedCornerShape(20.dp))
                        .clickable { onDelete() },
                    backdrop = backdrop
                ) {
                    Text(
                        text = "Delete",
                        color = Color(0xFFFF5252),
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
                        textAlign = TextAlign.Center
                    )
                }
            }
        } else if (!isAddingItem) {
            GlassCard(
                modifier = Modifier.fillMaxWidth(),
                backdrop = backdrop
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(start = 4.dp, end = 4.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    IconButton(
                        onClick = { onNavigate(0) },
                        modifier = Modifier.size(36.dp)
                    ) {
                        Icon(
                            painter = painterResource(id = R.drawable.ic_symbol_chat_bubble),
                            contentDescription = "Quotes",
                            modifier = Modifier.size(28.dp),
                            tint = if (currentPage == 0) Color.White else Color.White.copy(alpha = 0.5f),
                        )
                    }
                    IconButton(
                        onClick = { onNavigate(1) },
                        modifier = Modifier.size(36.dp)
                    ) {
                        OneMinActionLogo(currentPage)
                    }
                    IconButton(
                        onClick = { onNavigate(2) },
                        modifier = Modifier.size(36.dp)
                    ) {
                        Icon(
                            painter = painterResource(id = R.drawable.ic_symbol_hourglass),
                            contentDescription = "Pomodoro Timer",
                            modifier = Modifier.size(28.dp),
                            tint = if (currentPage == 2) Color.White else Color.White.copy(alpha = 0.5f),
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun OneMinActionLogo(currentPage: Int) {
    Box(modifier = Modifier.size(28.dp)) {
        Icon(
            painter = painterResource(id = R.drawable.ic_sentiment_calm),
            contentDescription = "1-min Actions",
            modifier = Modifier.fillMaxSize(),
            tint = if (currentPage == 1) Color.White else Color.White.copy(alpha = 0.5f)
        )

        Icon(
            painter = painterResource(id = R.drawable.ic_symbol_one),
            contentDescription = null,
            modifier = Modifier
                .size(7.dp)
                .align(Alignment.TopEnd),
            tint = if (currentPage == 1) Color.White else Color.White.copy(alpha = 0.5f)
        )
    }
}

@Composable
fun ZenFab(
    isAddingItem: Boolean,
    currentPage: Int,
    isSelectionMode: Boolean,
    onClick: () -> Unit
) {
    if (!isAddingItem && currentPage != 2 && !isSelectionMode) {
        FloatingActionButton(
            onClick = onClick,
            containerColor = Color.White.copy(alpha = 0.2f),
            contentColor = Color.White,
            shape = RoundedCornerShape(16.dp),
            elevation = FloatingActionButtonDefaults.elevation(
                defaultElevation = 0.dp,
                pressedElevation = 0.dp
            )
        ) {
            Icon(
                painter = painterResource(id = R.drawable.ic_symbol_add),
                contentDescription = "Add",
                modifier = Modifier.size(24.dp))
        }
    }
}

@Composable
fun ZenPagerContent(
    pagerState: PagerState,
    currentItems: List<RepoItem>,
    backdrop: LayerBackdrop,
    isSelectionMode: Boolean,
    selectedItems: Set<Any>,
    onToggleSelection: (Any) -> Unit,
    onLongPress: (Any) -> Unit
) {
    HorizontalPager(
        state = pagerState,
        userScrollEnabled = !isSelectionMode,
        modifier = Modifier.fillMaxSize()
    ) { page ->
        when (page) {
            0, 1 -> {
                LazyColumn(
                    contentPadding = PaddingValues(8.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier.fillMaxSize(),
                ) {
                    val reversedItems = currentItems.asReversed()

                    items(reversedItems.size) { index ->
                        val item = reversedItems[index]
                        val isSelected = selectedItems.contains(item as Any)

                        GlassCard(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(20.dp))
                                .combinedClickable(
                                    onClick = {
                                        if (isSelectionMode) onToggleSelection(item as Any)
                                    },
                                    onLongClick = {
                                        if (!isSelectionMode) onLongPress(item as Any)
                                    }
                                ),
                            backdrop = backdrop
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(IntrinsicSize.Min),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column(
                                    modifier = Modifier.weight(1f).padding(end = 8.dp)
                                ) {
                                    Text(
                                        text = item.text,
                                        color = Color.White,
                                        style = MaterialTheme.typography.bodyLarge,
                                        fontWeight = FontWeight.Bold
                                    )
                                    if (item.caption != "" ) {
                                        Spacer(modifier = Modifier.height(4.dp))
                                        Text(
                                            text = item.caption,
                                            color = Color.White.copy(alpha = 0.9f),
                                            style = MaterialTheme.typography.labelMedium,
                                            fontWeight = FontWeight.Normal
                                        )
                                    }
                                }

                                AnimatedVisibility(
                                    visible = isSelectionMode,
                                    enter =
                                        fadeIn(
                                            animationSpec = tween(durationMillis = 200)
                                        ) + slideInHorizontally(
                                            animationSpec = tween(durationMillis = 200)) { fullWidth ->
                                            fullWidth
                                        },
                                    exit =
                                        fadeOut(
                                            animationSpec = tween(durationMillis = 200)
                                        ) + slideOutHorizontally(
                                            animationSpec = tween(durationMillis = 200)) { fullWidth ->
                                            200
                                        },
                                ) {
                                    Checkbox(
                                        checked = isSelected,
                                        onCheckedChange = { onToggleSelection(item as Any) },
                                        colors = CheckboxDefaults.colors(
                                            checkedColor = Color.White.copy(alpha = 0.1f),
                                            uncheckedColor = Color.White.copy(alpha = 0.5f),
                                            checkmarkColor = Color.White
                                        )
                                    )
                                }
                            }
                        }
                    }
                }
            }
            2 -> {
                PomodoroScreen(backdrop)
            }
        }
    }
}
