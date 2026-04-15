package com.vishaltelangre.nerdcalci.ui.home

import androidx.activity.compose.BackHandler
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.HelpOutline
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.FlashOn
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Description
import androidx.compose.material.icons.filled.RssFeed
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Sync
import androidx.compose.material.icons.filled.UploadFile
import androidx.compose.material3.*
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewModelScope
import com.vishaltelangre.nerdcalci.R
import com.vishaltelangre.nerdcalci.core.Constants
import com.vishaltelangre.nerdcalci.data.local.entities.FileEntity
import com.vishaltelangre.nerdcalci.data.local.entities.FileSortCriteria
import com.vishaltelangre.nerdcalci.data.local.entities.FileSortOption
import com.vishaltelangre.nerdcalci.data.local.entities.FileSortDirection
import com.vishaltelangre.nerdcalci.ui.calculator.CalculatorViewModel
import com.vishaltelangre.nerdcalci.ui.calculator.HomeUiEvent
import com.vishaltelangre.nerdcalci.core.LaunchMode
import com.vishaltelangre.nerdcalci.ui.components.SectionHeader
import com.vishaltelangre.nerdcalci.ui.components.addDismissibleFileItems
import androidx.compose.material.icons.automirrored.filled.Sort
import androidx.compose.material.icons.filled.Check
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Button
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.TextButton
import androidx.compose.material3.IconButton
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    viewModel: CalculatorViewModel,
    onFileClick: (Long) -> Unit,
    onSettingsClick: () -> Unit,
    onHelpClick: () -> Unit,
    onChangelogClick: () -> Unit,
    onRestoreClick: () -> Unit,
    onSearchClick: () -> Unit,
    launchMode: LaunchMode,
    autoOpenFileId: Long?,
    isAutoOpenReady: Boolean,
    suppressAutoOpenScratchpad: Boolean = false,
    showScratchpad: Boolean = true
) {
    val context = LocalContext.current
    val files by viewModel.allFiles.collectAsState(initial = emptyList())
    val fileSortCriteria by viewModel.fileSortCriteria.collectAsState()
    val snackbarHostState = remember { SnackbarHostState() }
    val coroutineScope = rememberCoroutineScope()
    var showSortMenu by remember { mutableStateOf(false) }

    // Get app name from strings.xml
    val appName = context.getString(R.string.app_name)

    fun createFile() {
        viewModel.createNewFile(context) { fileId ->
            onFileClick(fileId)
        }
    }

    var hasAutoOpened by rememberSaveable { mutableStateOf(false) }

    // Handle UI events like Undo Snackbars and other messages
    val excludedFileIds by viewModel.excludedFileIds.collectAsState(initial = emptySet())
    val scratchpadFileId by viewModel.scratchpadFileId.collectAsState()

    val syncEnabled by viewModel.syncEnabled.collectAsState()
    val isSyncing by viewModel.isSyncing.collectAsState()
    val lastSyncAt by viewModel.lastSyncAt.collectAsState()

    // Cleanup "Deleted" items when the user interact with the list or navigates away
    DisposableEffect(Unit) {
        onDispose {
            viewModel.permanentDeleteExclusions(context)
        }
    }

    // Auto-open logic on launch
    LaunchedEffect(isAutoOpenReady, launchMode, autoOpenFileId, suppressAutoOpenScratchpad) {
        if (
            isAutoOpenReady &&
            !suppressAutoOpenScratchpad &&
            launchMode != LaunchMode.NOT_SET &&
            autoOpenFileId != null &&
            !hasAutoOpened
        ) {
            hasAutoOpened = true
            onFileClick(autoOpenFileId)
        }
    }

    LaunchedEffect(Unit) {
        viewModel.uiEvents.collect { event ->
            when (event) {
                is HomeUiEvent.ShowMessage -> {
                    snackbarHostState.showSnackbar(event.message)
                }
            }
        }
    }

    val visiblePinnedFiles = files.filter { it.isPinned }
    val visibleUnpinnedFiles = files.filterNot { it.isPinned }

    Scaffold(
        topBar = {
            if (files.isNotEmpty()) {
                TopAppBar(
                    title = { Text(appName, color = MaterialTheme.colorScheme.onSurface) },
                    navigationIcon = {
                        IconButton(onClick = onChangelogClick) {
                            Icon(
                                Icons.Default.RssFeed,
                                "What's New",
                                tint = MaterialTheme.colorScheme.onSurface
                            )
                        }
                    },
                    actions = {
                        if (syncEnabled) {
                            IconButton(
                                onClick = { viewModel.syncFiles(context) },
                                enabled = !isSyncing
                            ) {
                                Icon(
                                    Icons.Default.Sync,
                                    contentDescription = "Sync files",
                                    tint = MaterialTheme.colorScheme.onSurface
                                )
                            }
                        }
                        IconButton(onClick = onSearchClick) {
                            Icon(
                                Icons.Default.Search,
                                "Search",
                                tint = MaterialTheme.colorScheme.onSurface
                            )
                        }
                        IconButton(onClick = { showSortMenu = true }) {
                            Icon(
                                Icons.AutoMirrored.Filled.Sort,
                                "Sort",
                                tint = MaterialTheme.colorScheme.onSurface
                            )
                            SortMenu(
                                expanded = showSortMenu,
                                onDismissRequest = { showSortMenu = false },
                                currentCriteria = fileSortCriteria,
                                onCriteriaSelected = { criteria: FileSortCriteria ->
                                    viewModel.setFileSortCriteria(criteria)
                                    showSortMenu = false
                                }
                            )
                        }
                        IconButton(onClick = onSettingsClick) {
                            Icon(
                                Icons.Default.Settings,
                                "Settings",
                                tint = MaterialTheme.colorScheme.onSurface
                            )
                        }
                    },
                    colors = TopAppBarDefaults.topAppBarColors(
                        containerColor = MaterialTheme.colorScheme.background
                    )
                )
            } else {
                CenterAlignedTopAppBar(
                    title = { Text(appName, color = MaterialTheme.colorScheme.onSurface) },
                    actions = {
                        if (syncEnabled) {
                            IconButton(
                                onClick = { viewModel.syncFiles(context) },
                                enabled = !isSyncing
                            ) {
                                Icon(
                                    Icons.Default.Sync,
                                    contentDescription = "Sync files",
                                    tint = MaterialTheme.colorScheme.onSurface
                                )
                            }
                        }
                        IconButton(onClick = onSettingsClick) {
                            Icon(
                                Icons.Default.Settings,
                                "Settings",
                                tint = MaterialTheme.colorScheme.onSurface
                            )
                        }
                    },
                    colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
                        containerColor = MaterialTheme.colorScheme.background
                    )
                )
            }
        },
        floatingActionButton = {
            AnimatedVisibility(
                visible = files.isNotEmpty(),
                enter = fadeIn() + slideInVertically { it },
                exit = fadeOut() + slideOutVertically { it }
            ) {
                Column(
                    horizontalAlignment = Alignment.End,
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    if (showScratchpad) {
                        scratchpadFileId?.let { id ->
                            FloatingActionButton(
                                onClick = { onFileClick(id) },
                                containerColor = MaterialTheme.colorScheme.primaryContainer,
                                contentColor = MaterialTheme.colorScheme.onPrimaryContainer
                            ) {
                                Icon(Icons.Default.FlashOn, contentDescription = "Open temporary scratchpad")
                            }
                        }
                    }

                    FloatingActionButton(
                        onClick = { createFile() },
                        containerColor = MaterialTheme.colorScheme.primary
                    ) {
                        Icon(Icons.Default.Add, contentDescription = "New Calculation")
                    }
                }
            }
        },
        snackbarHost = { SnackbarHost(hostState = snackbarHostState) },
        containerColor = MaterialTheme.colorScheme.background
    ) { padding ->
        if (files.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding)
                    .padding(horizontal = 20.dp, vertical = 24.dp),
                contentAlignment = Alignment.Center
            ) {
                Card(
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.surfaceContainerHigh
                    ),
                    elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center,
                        modifier = Modifier.padding(horizontal = 24.dp, vertical = 28.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(72.dp)
                                .clip(MaterialTheme.shapes.extraLarge)
                                .background(MaterialTheme.colorScheme.secondaryContainer),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.Description,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.onSecondaryContainer,
                                modifier = Modifier.size(34.dp)
                            )
                        }
                        Spacer(modifier = Modifier.height(16.dp))
                        Text(
                            text = "No files yet",
                            style = MaterialTheme.typography.headlineSmall,
                            color = MaterialTheme.colorScheme.onSurface,
                            modifier = Modifier.padding(bottom = 8.dp)
                        )
                        Text(
                            text = "Create a file or import a backup.",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            textAlign = TextAlign.Center,
                            modifier = Modifier.padding(bottom = 20.dp)
                        )
                        if (showScratchpad) {
                            scratchpadFileId?.let { id ->
                                OutlinedButton(
                                    onClick = { onFileClick(id) },
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    Icon(Icons.Default.FlashOn, contentDescription = null)
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text("Open temporary scratchpad")
                                }
                                Spacer(modifier = Modifier.height(10.dp))
                            }
                        }
                        Button(
                            onClick = { createFile() },
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Icon(Icons.Default.Add, contentDescription = null)
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("Create file")
                        }
                        Spacer(modifier = Modifier.height(10.dp))
                        OutlinedButton(
                            onClick = onRestoreClick,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Icon(Icons.Default.UploadFile, contentDescription = null)
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("Restore from backup")
                        }
                        Spacer(modifier = Modifier.height(8.dp))
                        TextButton(
                            onClick = onHelpClick,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Icon(Icons.AutoMirrored.Filled.HelpOutline, contentDescription = null)
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("Help")
                        }
                        Spacer(modifier = Modifier.height(8.dp))
                        TextButton(
                            onClick = onChangelogClick,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Icon(Icons.Default.RssFeed, contentDescription = null)
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("What's New")
                        }
                    }
                }
            }
        } else {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding)
            ) {
                if (syncEnabled) {
                    PullToRefreshBox(
                        isRefreshing = isSyncing,
                        onRefresh = { viewModel.syncFiles(context) },
                        modifier = Modifier.fillMaxSize()
                    ) {
                        HomeFileList(
                            visiblePinnedFiles = visiblePinnedFiles,
                            visibleUnpinnedFiles = visibleUnpinnedFiles,
                            excludedFileIds = excludedFileIds,
                            onFileClick = onFileClick,
                            coroutineScope = coroutineScope,
                            snackbarHostState = snackbarHostState,
                            context = context,
                            viewModel = viewModel
                        )
                    }
                } else {
                    HomeFileList(
                        visiblePinnedFiles = visiblePinnedFiles,
                        visibleUnpinnedFiles = visibleUnpinnedFiles,
                        excludedFileIds = excludedFileIds,
                        onFileClick = onFileClick,
                        coroutineScope = coroutineScope,
                        snackbarHostState = snackbarHostState,
                        context = context,
                        viewModel = viewModel
                    )
                }
            }
        }
    }
}

@Composable
private fun HomeFileList(
    visiblePinnedFiles: List<FileEntity>,
    visibleUnpinnedFiles: List<FileEntity>,
    excludedFileIds: Set<Long>,
    onFileClick: (Long) -> Unit,
    coroutineScope: CoroutineScope,
    snackbarHostState: SnackbarHostState,
    context: android.content.Context,
    viewModel: CalculatorViewModel
) {
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(bottom = 96.dp)
    ) {

        if (visiblePinnedFiles.isNotEmpty()) {
            item { SectionHeader(title = "PINNED") }
            addDismissibleFileItems(
                files = visiblePinnedFiles,
                excludedIds = excludedFileIds,
                onFileClick = onFileClick,
                onRename = { id, name ->
                    coroutineScope.launch {
                        if (!viewModel.renameFile(context, id, name)) {
                            snackbarHostState.showSnackbar("Failed to rename file")
                        }
                    }
                },
                onDuplicate = { id -> viewModel.duplicateFile(id) { onFileClick(it) } },
                onDelete = { id ->
                    coroutineScope.launch {
                        if (!viewModel.deleteFile(context, id)) {
                            snackbarHostState.showSnackbar("Failed to delete file")
                        }
                    }
                },
                onTogglePin = { id -> viewModel.togglePinFile(id) },
                onUndo = { id: Long -> viewModel.undoHideFile(id) },
                onDismiss = { id: Long -> viewModel.hideFile(id) },
                viewModel = viewModel
            )
        }

        if (visibleUnpinnedFiles.isNotEmpty()) {
            item {
                SectionHeader(
                    title = if (visiblePinnedFiles.isNotEmpty()) "ALL FILES" else "FILES"
                )
            }
            addDismissibleFileItems(
                files = visibleUnpinnedFiles,
                excludedIds = excludedFileIds,
                onFileClick = onFileClick,
                onRename = { id, name ->
                    coroutineScope.launch {
                        if (!viewModel.renameFile(context, id, name)) {
                            snackbarHostState.showSnackbar("Failed to rename file")
                        }
                    }
                },
                onDuplicate = { id -> viewModel.duplicateFile(id) { onFileClick(it) } },
                onDelete = { id ->
                    coroutineScope.launch {
                        if (!viewModel.deleteFile(context, id)) {
                            snackbarHostState.showSnackbar("Failed to delete file")
                        }
                    }
                },
                onTogglePin = { id -> viewModel.togglePinFile(id) },
                onUndo = { id: Long -> viewModel.undoHideFile(id) },
                onDismiss = { id: Long -> viewModel.hideFile(id) },
                viewModel = viewModel
            )
        }
    }
}

@Composable
fun SortMenu(
    expanded: Boolean,
    onDismissRequest: () -> Unit,
    currentCriteria: FileSortCriteria,
    onCriteriaSelected: (FileSortCriteria) -> Unit
) {
    DropdownMenu(
        expanded = expanded,
        onDismissRequest = onDismissRequest
    ) {
        Text(
            text = "Sort by",
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
            style = MaterialTheme.typography.labelLarge,
            color = MaterialTheme.colorScheme.primary
        )

        FileSortOption.entries.forEach { option ->
            DropdownMenuItem(
                text = {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        RadioButton(
                            selected = currentCriteria.option == option,
                            onClick = null
                        )
                        Text(
                            text = when (option) {
                                FileSortOption.NAME -> "Name"
                                FileSortOption.CREATED_AT -> "Date created"
                                FileSortOption.MODIFIED_AT -> "Date modified"
                            },
                            modifier = Modifier.padding(start = 8.dp)
                        )
                    }
                },
                onClick = {
                    onCriteriaSelected(currentCriteria.copy(option = option))
                }
            )
        }

        HorizontalDivider()

        Text(
            text = "Order",
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
            style = MaterialTheme.typography.labelLarge,
            color = MaterialTheme.colorScheme.primary
        )

        FileSortDirection.entries.forEach { direction ->
            DropdownMenuItem(
                text = {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        RadioButton(
                            selected = currentCriteria.direction == direction,
                            onClick = null
                        )
                        Text(
                            text = when (direction) {
                                FileSortDirection.ASCENDING -> "Ascending"
                                FileSortDirection.DESCENDING -> "Descending"
                            },
                            modifier = Modifier.padding(start = 8.dp)
                        )
                    }
                },
                onClick = {
                    onCriteriaSelected(currentCriteria.copy(direction = direction))
                }
            )
        }
    }
}
