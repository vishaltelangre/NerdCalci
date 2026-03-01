package com.vishaltelangre.nerdcalci.ui.components

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyListScope
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Description
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.FileCopy
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.PushPin
import androidx.compose.material.icons.outlined.PushPin
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.vishaltelangre.nerdcalci.core.Constants
import com.vishaltelangre.nerdcalci.data.local.entities.FileEntity
import java.text.SimpleDateFormat
import java.util.Locale

internal fun LazyListScope.addFileItems(
    files: List<FileEntity>,
    onFileClick: (Long) -> Unit,
    onRename: (Long, String) -> Unit,
    onDuplicate: (Long, String) -> Unit,
    onDelete: (Long) -> Unit,
    onTogglePin: (Long) -> Unit
) {
    items(files, key = { it.id }) { file ->
        FileItem(
            file = file,
            onClick = { onFileClick(file.id) },
            onRename = { newName -> onRename(file.id, newName) },
            onDuplicate = { newName -> onDuplicate(file.id, newName) },
            onDelete = { onDelete(file.id) },
            onTogglePin = { onTogglePin(file.id) }
        )
    }
}

@Composable
internal fun FileItem(
    file: FileEntity,
    onClick: () -> Unit,
    onRename: (String) -> Unit,
    onDuplicate: (String) -> Unit,
    onDelete: () -> Unit,
    onTogglePin: () -> Unit
) {
    var showMenu by remember { mutableStateOf(false) }
    var showRenameDialog by remember { mutableStateOf(false) }
    var showDuplicateDialog by remember { mutableStateOf(false) }
    var showDeleteDialog by remember { mutableStateOf(false) }

    FileRowCard(
        file = file,
        title = AnnotatedString(file.name),
        onClick = onClick,
        modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
    ) {
        Box {
            IconButton(onClick = { showMenu = true }) {
                Icon(
                    Icons.Default.MoreVert,
                    contentDescription = "More options",
                    tint = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            DropdownMenu(
                expanded = showMenu,
                onDismissRequest = { showMenu = false }
            ) {
                DropdownMenuItem(
                    text = { Text(if (file.isPinned) "Unpin" else "Pin") },
                    leadingIcon = {
                        Icon(
                            if (file.isPinned) Icons.Filled.PushPin else Icons.Outlined.PushPin,
                            contentDescription = null
                        )
                    },
                    onClick = {
                        showMenu = false
                        onTogglePin()
                    }
                )
                DropdownMenuItem(
                    text = { Text("Rename") },
                    leadingIcon = { Icon(Icons.Default.Edit, contentDescription = null) },
                    onClick = {
                        showMenu = false
                        showRenameDialog = true
                    }
                )
                DropdownMenuItem(
                    text = { Text("Duplicate") },
                    leadingIcon = { Icon(Icons.Default.FileCopy, contentDescription = null) },
                    onClick = {
                        showMenu = false
                        showDuplicateDialog = true
                    }
                )
                DropdownMenuItem(
                    text = { Text("Delete") },
                    leadingIcon = { Icon(Icons.Default.Delete, contentDescription = null) },
                    onClick = {
                        showMenu = false
                        showDeleteDialog = true
                    }
                )
            }
        }
    }

    if (showRenameDialog) {
        RenameFileDialog(
            currentName = file.name,
            onDismiss = { showRenameDialog = false },
            onConfirm = { newName ->
                onRename(newName.take(Constants.MAX_FILE_NAME_LENGTH))
                showRenameDialog = false
            }
        )
    }

    if (showDuplicateDialog) {
        DuplicateFileDialog(
            originalName = file.name,
            onDismiss = { showDuplicateDialog = false },
            onConfirm = { newName ->
                onDuplicate(newName.take(Constants.MAX_FILE_NAME_LENGTH))
                showDuplicateDialog = false
            }
        )
    }

    if (showDeleteDialog) {
        DeleteFileDialog(
            fileName = file.name,
            onDismiss = { showDeleteDialog = false },
            onConfirm = {
                onDelete()
                showDeleteDialog = false
            }
        )
    }
}

@Composable
internal fun FileRowCard(
    file: FileEntity,
    title: AnnotatedString,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    trailingContent: (@Composable () -> Unit)? = null
) {
    Card(
        modifier = modifier
            .fillMaxWidth()
            .clickable { onClick() },
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                Icons.Default.Description,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Spacer(modifier = Modifier.width(16.dp))
            Column(modifier = Modifier.weight(1f)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = title,
                        style = MaterialTheme.typography.bodyLarge,
                        color = MaterialTheme.colorScheme.onSurface,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        modifier = Modifier.weight(1f, fill = false)
                    )
                    if (file.isPinned) {
                        Spacer(modifier = Modifier.width(8.dp))
                        Icon(
                            Icons.Filled.PushPin,
                            contentDescription = "Pinned",
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.padding(start = 4.dp)
                        )
                    }
                }
                Text(
                    text = "Last edited: ${
                        SimpleDateFormat("MMM dd, yyyy hh:mm a", Locale.getDefault()).format(file.lastModified)
                    }",
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            if (trailingContent != null) {
                trailingContent()
            }
        }
    }
}
