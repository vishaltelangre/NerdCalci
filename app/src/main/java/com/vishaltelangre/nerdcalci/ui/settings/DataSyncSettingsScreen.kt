package com.vishaltelangre.nerdcalci.ui.settings

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.AccessTimeFilled
import androidx.compose.material.icons.filled.Backup
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Description
import androidx.compose.material.icons.filled.Folder
import androidx.compose.material.icons.filled.Restore
import androidx.compose.material.icons.filled.Sync
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.vishaltelangre.nerdcalci.data.backup.BackupFileInfo
import com.vishaltelangre.nerdcalci.data.backup.BackupFrequency
import com.vishaltelangre.nerdcalci.data.backup.BackupLocationMode
import com.vishaltelangre.nerdcalci.ui.components.RestoreBackupListDialog
import com.vishaltelangre.nerdcalci.ui.components.RestoreSourceDialog

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DataSyncSettingsScreen(
    autoBackupEnabled: Boolean,
    onAutoBackupEnabledChange: (Boolean) -> Unit,
    backupFrequency: BackupFrequency,
    onBackupFrequencyChange: (BackupFrequency) -> Unit,
    backupLocationMode: BackupLocationMode,
    backupLocationSummary: String,
    onChooseBackupFolder: () -> Unit,
    onUseAppStorageLocation: () -> Unit,
    onBackupNow: () -> Unit,
    onBackupNowAtDifferentLocation: () -> Unit,
    lastBackupAt: Long?,
    availableBackups: List<BackupFileInfo>,
    onRestoreBackup: (BackupFileInfo) -> Unit,
    onRestoreFromDifferentLocation: () -> Unit,
    syncEnabled: Boolean,
    onSyncEnabledChange: (Boolean) -> Unit,
    syncFolderUri: String?,
    onChooseSyncFolder: () -> Unit,
    lastSyncAt: Long?,
    onBack: () -> Unit
) {
    var showRestoreDialog by remember { mutableStateOf(false) }
    var showRestoreActionDialog by remember { mutableStateOf(false) }
    var showBackupNowActionDialog by remember { mutableStateOf(false) }
    var showLocationDialog by remember { mutableStateOf(false) }
    var showFrequencyDialog by remember { mutableStateOf(false) }

    val currentLocationText = com.vishaltelangre.nerdcalci.ui.components.formatBackupLocationText(
        mode = backupLocationMode,
        customFolderSummary = backupLocationSummary
    )

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = { Text("Data & sync") },
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
            SettingsSection(title = "Backup")

            SettingsToggleItem(
                icon = Icons.Default.Backup,
                title = "Automatic backup",
                subtitle = "Keep your data safe by enabling scheduled backups.",
                checked = autoBackupEnabled,
                onCheckedChange = onAutoBackupEnabledChange
            )

            if (autoBackupEnabled) {
                SettingsDropdownItem(
                    icon = Icons.Default.AccessTimeFilled,
                    title = "Backup frequency",
                    value = if (backupFrequency == BackupFrequency.DAILY) "Daily" else "Weekly",
                    onClick = { showFrequencyDialog = true }
                )

                SettingsItem(
                    icon = Icons.Default.Folder,
                    title = "Backup location",
                    subtitle = currentLocationText,
                    onClick = { showLocationDialog = true }
                )
            }

            SettingsItem(
                icon = Icons.Default.Backup,
                title = "Back up now",
                subtitle = if (lastBackupAt != null) {
                    "Last backup ${formatRelativeTime(lastBackupAt)}"
                } else {
                    "Create a backup immediately"
                },
                onClick = { showBackupNowActionDialog = true }
            )

            SettingsItem(
                icon = Icons.Default.Restore,
                title = "Restore from backup",
                subtitle = if (availableBackups.isEmpty()) {
                    "Use current location or choose a different file"
                } else {
                    "Use current location (contains ${availableBackups.size} backups) or choose a different file"
                },
                onClick = { showRestoreActionDialog = true }
            )

            SettingsSection(title = "Sync (Experimental)")

            SettingsToggleItem(
                icon = Icons.Default.Sync,
                title = "Sync files",
                subtitle = "Sync files across devices using your preferred sync method (e.g., Syncthing, WebDAV, etc.)",
                checked = syncEnabled,
                onCheckedChange = { confirmed ->
                    if (confirmed && syncFolderUri == null) {
                        onChooseSyncFolder()
                    } else {
                        onSyncEnabledChange(confirmed)
                    }
                }
            )

            if (syncEnabled) {
                SettingsItem(
                    icon = Icons.Default.Folder,
                    title = "Sync location",
                    subtitle = com.vishaltelangre.nerdcalci.ui.settings.formatSyncFolderSubtitle(syncFolderUri) ?: "Choose folder",
                    onClick = onChooseSyncFolder
                )

                if (lastSyncAt != null) {
                    Text(
                        text = "Last synced ${formatRelativeTime(lastSyncAt)}",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.padding(horizontal = 16.dp, vertical = 4.dp)
                    )
                }
            }
        }
    }

    // Dialogs
    if (showFrequencyDialog) {
        AlertDialog(
            onDismissRequest = { showFrequencyDialog = false },
            title = { Text("Backup frequency") },
            text = {
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    val frequencies = listOf(BackupFrequency.DAILY to "Daily", BackupFrequency.WEEKLY to "Weekly")
                    frequencies.forEach { (freq, label) ->
                        Card(
                            onClick = {
                                showFrequencyDialog = false
                                onBackupFrequencyChange(freq)
                            },
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(12.dp),
                            colors = CardDefaults.cardColors(
                                containerColor = if (backupFrequency == freq)
                                    MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.5f)
                                else
                                    MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f)
                            )
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(16.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = label,
                                    style = MaterialTheme.typography.bodyLarge,
                                    fontWeight = if (backupFrequency == freq) FontWeight.Bold else FontWeight.Normal,
                                    color = if (backupFrequency == freq) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface
                                )
                                Spacer(modifier = Modifier.weight(1f))
                                if (backupFrequency == freq) {
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
                TextButton(onClick = { showFrequencyDialog = false }) { Text("Cancel") }
            }
        )
    }

    if (showLocationDialog) {
        AlertDialog(
            onDismissRequest = { showLocationDialog = false },
            title = { Text("Backup location") },
            text = {
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Card(
                        onClick = {
                            showLocationDialog = false
                            onUseAppStorageLocation()
                        },
                        enabled = backupLocationMode != BackupLocationMode.APP_STORAGE,
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(16.dp),
                        colors = CardDefaults.cardColors(
                            containerColor = if (backupLocationMode == BackupLocationMode.APP_STORAGE)
                                MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.7f)
                            else
                                MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f),
                            disabledContainerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.7f)
                        )
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(16.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = Icons.Default.Folder,
                                contentDescription = null,
                                tint = if (backupLocationMode == BackupLocationMode.APP_STORAGE) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            Spacer(modifier = Modifier.width(16.dp))
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = "App storage",
                                    style = MaterialTheme.typography.bodyLarge,
                                    fontWeight = FontWeight.SemiBold,
                                    color = if (backupLocationMode == BackupLocationMode.APP_STORAGE) MaterialTheme.colorScheme.onPrimaryContainer else MaterialTheme.colorScheme.onSurface
                                )
                                if (backupLocationMode == BackupLocationMode.APP_STORAGE) {
                                    Text(
                                        text = "Current location",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.8f)
                                    )
                                }
                            }
                        }
                    }

                    Card(
                        onClick = {
                            showLocationDialog = false
                            onChooseBackupFolder()
                        },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(16.dp),
                        colors = CardDefaults.cardColors(
                            containerColor = if (backupLocationMode == BackupLocationMode.CUSTOM_FOLDER)
                                MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.7f)
                            else
                                MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f)
                        )
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(16.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = Icons.Default.Folder,
                                contentDescription = null,
                                tint = if (backupLocationMode == BackupLocationMode.CUSTOM_FOLDER) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            Spacer(modifier = Modifier.width(16.dp))
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = "Choose custom folder",
                                    style = MaterialTheme.typography.bodyLarge,
                                    fontWeight = FontWeight.SemiBold,
                                    color = if (backupLocationMode == BackupLocationMode.CUSTOM_FOLDER) MaterialTheme.colorScheme.onPrimaryContainer else MaterialTheme.colorScheme.onSurface
                                )
                                if (backupLocationMode == BackupLocationMode.CUSTOM_FOLDER) {
                                    Text(
                                        text = backupLocationSummary,
                                        style = MaterialTheme.typography.bodySmall,
                                        color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.8f)
                                    )
                                }
                            }
                        }
                    }
                }
            },
            confirmButton = {
                TextButton(onClick = { showLocationDialog = false }) { Text("Cancel") }
            }
        )
    }

    if (showBackupNowActionDialog) {
        AlertDialog(
            onDismissRequest = { showBackupNowActionDialog = false },
            title = { Text("Back up now") },
            text = {
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Card(
                        onClick = {
                            showBackupNowActionDialog = false
                            onBackupNow()
                        },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(16.dp),
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.7f)
                        )
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(16.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = Icons.Default.Folder,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.primary
                            )
                            Spacer(modifier = Modifier.width(16.dp))
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = "Current location",
                                    style = MaterialTheme.typography.bodyLarge,
                                    fontWeight = FontWeight.SemiBold,
                                    color = MaterialTheme.colorScheme.onPrimaryContainer
                                )
                                Text(
                                    text = currentLocationText,
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.8f)
                                )
                            }
                        }
                    }

                    Card(
                        onClick = {
                            showBackupNowActionDialog = false
                            onBackupNowAtDifferentLocation()
                        },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(16.dp),
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f)
                        )
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(16.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = Icons.Default.Description,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            Spacer(modifier = Modifier.width(16.dp))
                            Text(
                                text = "Choose different location",
                                style = MaterialTheme.typography.bodyLarge,
                                fontWeight = FontWeight.SemiBold,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                        }
                    }
                }
            },
            confirmButton = {
                TextButton(onClick = { showBackupNowActionDialog = false }) { Text("Cancel") }
            }
        )
    }

    RestoreSourceDialog(
        visible = showRestoreActionDialog,
        hasBackupsInCurrentLocation = availableBackups.isNotEmpty(),
        currentLocationText = currentLocationText,
        onDismiss = { showRestoreActionDialog = false },
        onUseCurrentLocation = {
            showRestoreActionDialog = false
            showRestoreDialog = true
        },
        onChooseDifferentFile = {
            showRestoreActionDialog = false
            onRestoreFromDifferentLocation()
        }
    )

    RestoreBackupListDialog(
        visible = showRestoreDialog,
        currentLocationText = currentLocationText,
        backups = availableBackups,
        onDismiss = { showRestoreDialog = false },
        onBackupSelected = { backup ->
            showRestoreDialog = false
            onRestoreBackup(backup)
        }
    )
}

private fun formatSyncFolderSubtitle(syncFolderUri: String?): String? {
    if (syncFolderUri.isNullOrBlank()) return null
    return try {
        val decoded = android.net.Uri.decode(android.net.Uri.parse(syncFolderUri).lastPathSegment ?: "")
        if (decoded.isBlank()) {
            com.vishaltelangre.nerdcalci.utils.FileUtils.formatPathForDisplay(syncFolderUri)
        } else {
            com.vishaltelangre.nerdcalci.utils.FileUtils.formatPathForDisplay(decoded)
        }
    } catch (_: Exception) {
        com.vishaltelangre.nerdcalci.utils.FileUtils.formatPathForDisplay(syncFolderUri)
    }
}
