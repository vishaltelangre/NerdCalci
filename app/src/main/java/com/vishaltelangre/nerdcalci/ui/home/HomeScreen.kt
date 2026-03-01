package com.vishaltelangre.nerdcalci.ui.home

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.ui.platform.LocalContext
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Description
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.UploadFile
import androidx.compose.material.icons.automirrored.filled.HelpOutline
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TextField
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.vishaltelangre.nerdcalci.R
import com.vishaltelangre.nerdcalci.core.Constants
import com.vishaltelangre.nerdcalci.ui.calculator.CalculatorViewModel
import com.vishaltelangre.nerdcalci.ui.components.addFileItems
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    viewModel: CalculatorViewModel,
    onFileClick: (Long) -> Unit,
    onSettingsClick: () -> Unit,
    onHelpClick: () -> Unit,
    onRestoreClick: () -> Unit
) {
    val context = LocalContext.current
    val files by viewModel.allFiles.collectAsState(initial = emptyList())
    var showDialog by remember { mutableStateOf(false) }
    var newFileName by remember { mutableStateOf("") }
    var searchQuery by rememberSaveable { mutableStateOf("") }
    var searchBarVisible by rememberSaveable { mutableStateOf(false) }
    var searchBarActive by rememberSaveable { mutableStateOf(false) }
    val snackbarHostState = remember { SnackbarHostState() }
    val coroutineScope = rememberCoroutineScope()

    // Get app name from strings.xml
    val appName = context.getString(R.string.app_name)

    fun createFile() {
        if (newFileName.isNotBlank()) {
            val trimmedName = newFileName.trim().take(Constants.MAX_FILE_NAME_LENGTH)
            viewModel.createNewFile(trimmedName) { fileId ->
                onFileClick(fileId)
            }
            newFileName = ""
            showDialog = false
        }
    }

    val hasQuery = searchQuery.trim().isNotEmpty()
    val isSearchViewActive = searchBarActive || hasQuery
    val isSearchUiVisible = searchBarVisible || isSearchViewActive
    val visibleFiles = files
    val visiblePinnedFiles = visibleFiles.filter { it.isPinned }
    val visibleUnpinnedFiles = visibleFiles.filterNot { it.isPinned }

    fun dismissSearch(clearQuery: Boolean = true) {
        if (clearQuery) searchQuery = ""
        searchBarActive = false
        searchBarVisible = false
    }

    BackHandler(enabled = isSearchUiVisible) {
        dismissSearch(clearQuery = true)
    }

    Scaffold(
        topBar = {
            if (!isSearchViewActive) {
                CenterAlignedTopAppBar(
                    title = { Text(appName, color = MaterialTheme.colorScheme.onSurface) },
                    actions = {
                        IconButton(
                            onClick = {
                                searchBarVisible = true
                                searchBarActive = true
                            },
                            enabled = files.isNotEmpty()
                        ) {
                            Icon(
                                Icons.Default.Search,
                                "Search",
                        tint = if (isSearchUiVisible) {
                                    MaterialTheme.colorScheme.primary
                                } else {
                                    MaterialTheme.colorScheme.onSurface
                                }
                            )
                        }
                        IconButton(onClick = onSettingsClick) {
                            Icon(Icons.Default.Settings, "Settings", tint = MaterialTheme.colorScheme.onSurface)
                        }
                    },
                    colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
                        containerColor = MaterialTheme.colorScheme.background
                    )
                )
            }
        },
        floatingActionButton = {
            if (!isSearchViewActive) {
                FloatingActionButton(
                    onClick = { showDialog = true },
                    containerColor = MaterialTheme.colorScheme.primary
                ) {
                    Icon(Icons.Default.Add, contentDescription = "New Calculation")
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
                        Button(
                            onClick = { showDialog = true },
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
                    }
                }
            }
        } else {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding)
            ) {
                if (isSearchUiVisible) {
                    HomeSearchBar(
                        query = searchQuery,
                        files = files,
                        active = isSearchViewActive,
                        onQueryChange = { searchQuery = it.replace("\n", "") },
                        onActiveChange = { active ->
                            searchBarActive = active
                            if (!active && searchQuery.isBlank()) {
                                searchBarVisible = false
                            }
                        },
                        onClearQuery = { searchQuery = "" },
                        onDismissSearch = { dismissSearch(clearQuery = true) },
                        onFileClick = onFileClick
                    )
                }

                if (isSearchViewActive) {
                    return@Column
                }
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = androidx.compose.foundation.layout.PaddingValues(bottom = 96.dp)
                ) {
                    if (visiblePinnedFiles.isNotEmpty()) {
                        item { SectionHeader(title = "Pinned") }
                        addFileItems(
                            files = visiblePinnedFiles,
                            onFileClick = onFileClick,
                            onRename = { fileId, newName ->
                                viewModel.renameFile(fileId, newName.take(Constants.MAX_FILE_NAME_LENGTH))
                            },
                            onDuplicate = { fileId, newName ->
                                viewModel.duplicateFile(fileId, newName.take(Constants.MAX_FILE_NAME_LENGTH)) { newFileId ->
                                    onFileClick(newFileId)
                                }
                            },
                            onDelete = viewModel::deleteFile,
                            onTogglePin = { fileId ->
                                viewModel.togglePinFile(fileId) {
                                    coroutineScope.launch {
                                        snackbarHostState.showSnackbar(
                                            "Maximum ${Constants.MAX_PINNED_FILES} files can be pinned"
                                        )
                                    }
                                }
                            }
                        )
                    }

                    if (visibleUnpinnedFiles.isNotEmpty()) {
                        item {
                            SectionHeader(
                                title = if (visiblePinnedFiles.isNotEmpty()) "All files" else "Files"
                            )
                        }
                        addFileItems(
                            files = visibleUnpinnedFiles,
                            onFileClick = onFileClick,
                            onRename = { fileId, newName ->
                                viewModel.renameFile(fileId, newName.take(Constants.MAX_FILE_NAME_LENGTH))
                            },
                            onDuplicate = { fileId, newName ->
                                viewModel.duplicateFile(fileId, newName.take(Constants.MAX_FILE_NAME_LENGTH)) { newFileId ->
                                    onFileClick(newFileId)
                                }
                            },
                            onDelete = viewModel::deleteFile,
                            onTogglePin = { fileId ->
                                viewModel.togglePinFile(fileId) {
                                    coroutineScope.launch {
                                        snackbarHostState.showSnackbar(
                                            "Maximum ${Constants.MAX_PINNED_FILES} files can be pinned"
                                        )
                                    }
                                }
                            }
                        )
                    }
                }
            }
        }
    }

    // Dialog to name the new file
    if (showDialog) {
        AlertDialog(
            onDismissRequest = { showDialog = false },
            title = { Text("New File") },
            text = {
                Column {
                    TextField(
                        value = newFileName,
                        onValueChange = { newValue ->
                            // Filter out newlines and limit length
                            val filtered = newValue.replace("\n", "")
                            newFileName = if (filtered.length <= Constants.MAX_FILE_NAME_LENGTH) {
                                filtered
                            } else {
                                filtered.take(Constants.MAX_FILE_NAME_LENGTH)
                            }
                        },
                        placeholder = { Text("Enter a file name") },
                        singleLine = true,
                        keyboardOptions = KeyboardOptions(imeAction = ImeAction.Done),
                        keyboardActions = KeyboardActions(
                            onDone = { createFile() }
                        )
                    )
                    Text(
                        text = "${newFileName.length}/${Constants.MAX_FILE_NAME_LENGTH}",
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.padding(top = 4.dp)
                    )
                }
            },
            confirmButton = {
                TextButton(onClick = { createFile() }) {
                    Text("Create")
                }
            },
            dismissButton = {
                TextButton(onClick = {
                    showDialog = false
                    newFileName = ""
                }) {
                    Text("Cancel")
                }
            }
        )
    }
}

@Composable
private fun SectionHeader(title: String) {
    Text(
        text = title,
        style = MaterialTheme.typography.titleSmall,
        color = MaterialTheme.colorScheme.onSurfaceVariant,
        modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
    )
}
