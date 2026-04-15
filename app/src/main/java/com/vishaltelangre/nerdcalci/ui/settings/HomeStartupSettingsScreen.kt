package com.vishaltelangre.nerdcalci.ui.settings

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Description
import androidx.compose.material.icons.filled.FlashOn
import androidx.compose.material.icons.filled.Restore
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.vishaltelangre.nerdcalci.core.LaunchMode
import com.vishaltelangre.nerdcalci.data.local.entities.FileEntity

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeStartupSettingsScreen(
    launchMode: LaunchMode,
    onLaunchModeChange: (LaunchMode) -> Unit,
    launchFileId: Long?,
    onLaunchFileIdChange: (Long?) -> Unit,
    showScratchpad: Boolean,
    onShowScratchpadChange: (Boolean) -> Unit,
    allFiles: List<FileEntity>,
    onValidateLaunchFile: (Long, (Boolean) -> Unit) -> Unit,
    onBack: () -> Unit
) {
    var showLaunchModeDialog by remember { mutableStateOf(false) }
    var showSelectFileDialog by remember { mutableStateOf(false) }

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = { Text("Home & startup") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, "Back")
                    }
                },
                colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
                    containerColor = MaterialTheme.colorScheme.background
                )
            )
        },
        containerColor = MaterialTheme.colorScheme.background
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .verticalScroll(rememberScrollState())
        ) {
            SettingsSection(title = "Home screen shortcuts")

            SettingsToggleItem(
                icon = Icons.Default.FlashOn,
                title = "Temporary scratchpad shortcut on home",
                subtitle = "Always show a bolt icon on the home screen to quickly open the temporary scratchpad.",
                checked = showScratchpad,
                onCheckedChange = onShowScratchpadChange
            )

            SettingsSection(title = "Startup")

            SettingsDropdownItem(
                icon = when (launchMode) {
                    LaunchMode.NOT_SET -> Icons.Default.Restore
                    LaunchMode.SCRATCHPAD -> Icons.Default.FlashOn
                    LaunchMode.JOURNAL -> Icons.Default.Schedule
                    LaunchMode.SPECIFIC_FILE -> Icons.Default.Description
                },
                title = "Auto-open file on launch",
                value = when (launchMode) {
                    LaunchMode.NOT_SET -> "Not set"
                    LaunchMode.SCRATCHPAD -> "Temporary scratchpad"
                    LaunchMode.JOURNAL -> "Daily journal (YYYY-MM-DD)"
                    LaunchMode.SPECIFIC_FILE -> {
                        val file = allFiles.find { it.id == launchFileId }
                        file?.name ?: "Choose file..."
                    }
                },
                onClick = {
                    if (launchMode == LaunchMode.SPECIFIC_FILE && launchFileId != null) {
                        onValidateLaunchFile(launchFileId) { exists ->
                            if (!exists) {
                                showSelectFileDialog = true
                            } else {
                                showLaunchModeDialog = true
                            }
                        }
                    } else {
                        showLaunchModeDialog = true
                    }
                }
            )
        }
    }

    if (showLaunchModeDialog) {
        LaunchModeDialog(
            currentMode = launchMode,
            onSelect = { mode ->
                showLaunchModeDialog = false
                if (mode == LaunchMode.SPECIFIC_FILE) {
                    showSelectFileDialog = true
                } else {
                    onLaunchModeChange(mode)
                }
            },
            onDismiss = { showLaunchModeDialog = false }
        )
    }

    if (showSelectFileDialog) {
        SelectAutoOpenFileDialog(
            files = allFiles,
            currentFileId = launchFileId,
            onSelect = { fileId ->
                showSelectFileDialog = false
                onLaunchModeChange(LaunchMode.SPECIFIC_FILE)
                onLaunchFileIdChange(fileId)
            },
            onDismiss = { showSelectFileDialog = false }
        )
    }
}

@Composable
private fun LaunchModeDialog(
    currentMode: LaunchMode,
    onSelect: (LaunchMode) -> Unit,
    onDismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Auto-open on launch") },
        text = {
            Column(modifier = Modifier.fillMaxWidth()) {
                val options = listOf(
                    LaunchMode.NOT_SET to "Not set",
                    LaunchMode.SCRATCHPAD to "Temporary scratchpad",
                    LaunchMode.JOURNAL to "Daily journal (YYYY-MM-DD)",
                    LaunchMode.SPECIFIC_FILE to "Specific file"
                )

                options.forEach { (mode, label) ->
                    TextButton(
                        onClick = { onSelect(mode) },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = label,
                                style = MaterialTheme.typography.bodyMedium,
                                color = if (currentMode == mode) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface
                            )
                            Spacer(modifier = Modifier.weight(1f))
                            if (currentMode == mode) {
                                Icon(
                                    imageVector = Icons.Default.Check,
                                    contentDescription = "Selected",
                                    tint = MaterialTheme.colorScheme.primary
                                )
                            }
                        }
                    }
                }
            }
        },
        confirmButton = {
            TextButton(onClick = onDismiss) { Text("Cancel") }
        }
    )
}

@Composable
private fun SelectAutoOpenFileDialog(
    files: List<FileEntity>,
    currentFileId: Long?,
    onSelect: (Long) -> Unit,
    onDismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Select file") },
        text = {
            if (files.isEmpty()) {
                Text("No files available to select.")
            } else {
                val sortedFiles = remember(files) { files.sortedBy { it.name } }
                LazyColumn(
                    modifier = Modifier.fillMaxWidth().height(300.dp)
                ) {
                    items(sortedFiles) { file ->
                        TextButton(
                            onClick = { onSelect(file.id) },
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = file.name,
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = if (currentFileId == file.id) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface
                                )
                                Spacer(modifier = Modifier.weight(1f))
                                if (currentFileId == file.id) {
                                    Icon(
                                        imageVector = Icons.Default.Check,
                                        contentDescription = "Selected",
                                        tint = MaterialTheme.colorScheme.primary
                                    )
                                }
                            }
                        }
                    }
                }
            }
        },
        confirmButton = {
            TextButton(onClick = onDismiss) { Text("Cancel") }
        }
    )
}
