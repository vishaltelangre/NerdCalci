package com.vishaltelangre.nerdcalci.ui.components

import android.text.format.DateFormat
import androidx.compose.animation.animateContentSize
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.Description
import androidx.compose.material.icons.filled.Folder
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import com.vishaltelangre.nerdcalci.data.backup.BackupFileInfo
import com.vishaltelangre.nerdcalci.data.backup.BackupLocationMode
import java.util.Date
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.Checkbox
import androidx.compose.runtime.remember
import androidx.compose.runtime.mutableStateOf
import com.vishaltelangre.nerdcalci.data.backup.ConflictResolution
import java.text.SimpleDateFormat
import androidx.compose.material3.RadioButton
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.ui.draw.clip

@Composable
fun RestoreSourceDialog(
    visible: Boolean,
    hasBackupsInCurrentLocation: Boolean,
    currentLocationText: String,
    onDismiss: () -> Unit,
    onUseCurrentLocation: () -> Unit,
    onChooseDifferentFile: () -> Unit
) {
    if (!visible) return
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Restore from backup") },
        text = {
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Card(
                        onClick = onUseCurrentLocation,
                        enabled = hasBackupsInCurrentLocation,
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(16.dp),
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.7f),
                            disabledContainerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.2f)
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
                                tint = if (hasBackupsInCurrentLocation) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f)
                            )
                            Spacer(modifier = Modifier.width(16.dp))
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = if (hasBackupsInCurrentLocation) "Current location" else "No backups found",
                                    style = MaterialTheme.typography.bodyLarge,
                                    fontWeight = FontWeight.SemiBold,
                                    color = if (hasBackupsInCurrentLocation) MaterialTheme.colorScheme.onPrimaryContainer else MaterialTheme.colorScheme.onSurfaceVariant
                                )
                                Text(
                                    text = currentLocationText,
                                    style = MaterialTheme.typography.bodySmall,
                                    color = if (hasBackupsInCurrentLocation) MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.8f) else MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f)
                                )
                            }
                        }
                    }

                    Card(
                        onClick = onChooseDifferentFile,
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
                                text = "Choose a different backup file",
                                style = MaterialTheme.typography.bodyLarge,
                                fontWeight = FontWeight.SemiBold,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                        }
                    }
                }
        },
        confirmButton = {
            TextButton(onClick = onDismiss) { Text("Close") }
        }
    )
}

@Composable
fun RestoreBackupListDialog(
    visible: Boolean,
    currentLocationText: String,
    backups: List<BackupFileInfo>,
    onDismiss: () -> Unit,
    onBackupSelected: (BackupFileInfo) -> Unit
) {
    if (!visible) return
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Restore from backup") },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState())
            ) {
                Text(
                    text = labelValueText("Location:", currentLocationText),
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Spacer(modifier = Modifier.height(8.dp))
                backups.forEach { backup ->
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { onBackupSelected(backup) },
                        shape = RoundedCornerShape(16.dp),
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f)
                        )
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 16.dp, vertical = 12.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = backup.displayName,
                                    style = MaterialTheme.typography.bodyLarge,
                                    fontWeight = FontWeight.Medium,
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                                Spacer(modifier = Modifier.height(2.dp))
                                Text(
                                    text = "Created at: ${formatFriendlyDateTime(backup.lastModified)}",
                                    style = MaterialTheme.typography.labelMedium,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                            Icon(
                                imageVector = Icons.Default.ChevronRight,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f)
                            )
                        }
                    }
                    Spacer(modifier = Modifier.height(12.dp))
                }
            }
        },
        confirmButton = {
            TextButton(onClick = onDismiss) { Text("Close") }
        }
    )
}

@Composable
fun RestoreProgressDialog(
    visible: Boolean,
    currentFile: String,
    current: Int,
    total: Int,
    conflictFile: String? = null,
    localModified: Long = 0L,
    zipModified: Long = 0L,
    onResolveConflict: (ConflictResolution, Boolean) -> Unit = { _, _ -> },
    onCancel: () -> Unit = {}
) {
    if (!visible) return
    val rememberChoice = remember { mutableStateOf(false) }
    var selectedResolution by remember { mutableStateOf(ConflictResolution.KEEP_LOCAL_FILE) }

    val dateFormat = remember { SimpleDateFormat("MMM d, yyyy h:mm a", java.util.Locale.getDefault()) }

    AlertDialog(
        onDismissRequest = { /* Disallow dismiss during progress */ },
        title = { Text(if (conflictFile != null) "Resolve conflicts" else "Restoring files...") },
        text = {
            Column(modifier = Modifier.fillMaxWidth()) {
                // Progress section
                Text(
                    text = if (currentFile.isNotEmpty()) "Processing: $currentFile" else "Estimating...",
                    style = MaterialTheme.typography.bodyMedium
                )
                Spacer(modifier = Modifier.height(12.dp))
                if (total > 0) {
                    val progress = current.toFloat() / total
                    LinearProgressIndicator(
                        progress = { progress },
                        modifier = Modifier.fillMaxWidth()
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "$current / $total",
                        style = MaterialTheme.typography.labelSmall,
                        modifier = Modifier.align(Alignment.End)
                    )
                } else {
                    LinearProgressIndicator(modifier = Modifier.fillMaxWidth())
                }

                // Conflict section
                if (conflictFile != null) {
                    Spacer(modifier = Modifier.height(16.dp))
                    androidx.compose.material3.HorizontalDivider()
                    Spacer(modifier = Modifier.height(16.dp))

                    Text(
                        buildAnnotatedString {
                            append("The file ")
                            withStyle(SpanStyle(fontWeight = FontWeight.Bold)) {
                                append("\"$conflictFile\"")
                            }
                            append(" already exists.\nChoose which version to keep:")
                        }
                    )
                    Spacer(modifier = Modifier.height(16.dp))

                    Column {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable { selectedResolution = ConflictResolution.KEEP_LOCAL_FILE }
                                .padding(vertical = 4.dp)
                        ) {
                            RadioButton(
                                selected = selectedResolution == ConflictResolution.KEEP_LOCAL_FILE,
                                onClick = { selectedResolution = ConflictResolution.KEEP_LOCAL_FILE }
                            )
                            Column(modifier = Modifier.padding(start = 8.dp)) {
                                Text("Keep existing file", style = MaterialTheme.typography.bodyLarge)
                                if (localModified > 0L) {
                                    Text(
                                        text = "Modified: ${dateFormat.format(java.util.Date(localModified))}",
                                        style = MaterialTheme.typography.labelMedium,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                                        modifier = Modifier.padding(top = 4.dp)
                                    )
                                }
                            }
                        }

                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable { selectedResolution = ConflictResolution.REPLACE_WITH_FILE_FROM_ZIP }
                                .padding(vertical = 4.dp)
                        ) {
                            RadioButton(
                                selected = selectedResolution == ConflictResolution.REPLACE_WITH_FILE_FROM_ZIP,
                                onClick = { selectedResolution = ConflictResolution.REPLACE_WITH_FILE_FROM_ZIP }
                            )
                            Column(modifier = Modifier.padding(start = 8.dp)) {
                                Text("Replace with file from ZIP", style = MaterialTheme.typography.bodyLarge)
                                if (zipModified > 0L) {
                                    Text(
                                        text = "Modified: ${dateFormat.format(java.util.Date(zipModified))}",
                                        style = MaterialTheme.typography.labelMedium,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                                        modifier = Modifier.padding(top = 4.dp)
                                    )
                                }
                            }
                        }

                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable { selectedResolution = ConflictResolution.KEEP_BOTH_FILES }
                                .padding(vertical = 4.dp)
                        ) {
                            RadioButton(
                                selected = selectedResolution == ConflictResolution.KEEP_BOTH_FILES,
                                onClick = { selectedResolution = ConflictResolution.KEEP_BOTH_FILES }
                            )
                            Column(modifier = Modifier.padding(start = 8.dp)) {
                                Text("Keep both files", style = MaterialTheme.typography.bodyLarge)
                                Text(
                                    text = "Rename file imported from ZIP",
                                    style = MaterialTheme.typography.labelMedium,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                    modifier = Modifier.padding(top = 4.dp)
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.clickable { rememberChoice.value = !rememberChoice.value }
                    ) {
                        Checkbox(
                            checked = rememberChoice.value,
                            onCheckedChange = { rememberChoice.value = it }
                        )
                        Text(
                            text = "Apply this choice to all conflicts",
                            style = MaterialTheme.typography.bodyMedium,
                            modifier = Modifier.padding(start = 8.dp)
                        )
                    }
                }
            }
        },
        confirmButton = {
            if (conflictFile != null) {
                TextButton(onClick = { onResolveConflict(selectedResolution, rememberChoice.value) }) {
                    Text("Continue")
                }
            }
        },
        dismissButton = {
            TextButton(onClick = onCancel) {
                Text("Cancel")
            }
        }
    )
}

@Composable
private fun BulletItem(text: String, style: androidx.compose.ui.text.TextStyle) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier.padding(vertical = 2.dp)
    ) {
        Box(
            modifier = Modifier
                .size(6.dp)
                .clip(CircleShape)
                .background(MaterialTheme.colorScheme.onSurfaceVariant)
        )
        Spacer(modifier = Modifier.width(8.dp))
        Text(text = text, style = style)
    }
}

@Composable
fun RestoreCompleteDialog(
    visible: Boolean,
    message: String,
    addedCount: Int = 0,
    overwrittenCount: Int = 0,
    skippedCount: Int = 0,
    isSuccess: Boolean = true,
    onDismiss: () -> Unit
) {
    if (!visible) return
    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                text = if (isSuccess) "Restore finished" else "Restore failed",
                color = if (isSuccess) MaterialTheme.colorScheme.onSurface else MaterialTheme.colorScheme.error
            )
        },
        text = {
            Column(modifier = Modifier.fillMaxWidth()) {
                Text(
                    text = message,
                    style = MaterialTheme.typography.bodyLarge,
                    color = if (isSuccess) MaterialTheme.colorScheme.onSurfaceVariant else MaterialTheme.colorScheme.error
                )
                Spacer(modifier = Modifier.height(12.dp))

                val itemStyle = MaterialTheme.typography.bodyMedium
                
                if (isSuccess) {
                    BulletItem("$addedCount added", itemStyle)
                    BulletItem("$overwrittenCount replaced", itemStyle)
                    BulletItem("$skippedCount skipped", itemStyle)
                }
            }
        },
        confirmButton = {
            TextButton(onClick = onDismiss) { Text("OK") }
        }
    )
}

private fun labelValueText(label: String, value: String) = buildAnnotatedString {
    withStyle(style = SpanStyle(fontWeight = FontWeight.SemiBold)) {
        append(label)
    }
    append(" ")
    append(value)
}

private fun formatFriendlyDateTime(value: Long): String {
    return DateFormat.format("MMM d, yyyy h:mm a", Date(value)).toString()
}

fun formatBackupLocationText(
    mode: BackupLocationMode,
    customFolderSummary: String
): String {
    return if (mode == BackupLocationMode.APP_STORAGE) {
        "Default app storage"
    } else {
        "Custom folder ($customFolderSummary)"
    }
}
