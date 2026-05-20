package com.vishaltelangre.nerdcalci.ui.components

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyListScope
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.Info
import androidx.compose.material.icons.outlined.PushPin
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import java.text.SimpleDateFormat
import android.text.format.DateUtils
import java.util.Calendar
import java.util.Locale
import java.util.Date
import androidx.compose.ui.text.font.FontWeight

import com.vishaltelangre.nerdcalci.core.Constants
import com.vishaltelangre.nerdcalci.data.local.entities.FileEntity
import com.vishaltelangre.nerdcalci.ui.calculator.CalculatorViewModel

private val FILE_ITEM_CONTENT_HEIGHT = 56.dp
private val FILE_ITEM_VERTICAL_PADDING = 8.dp
private val FILE_ITEM_INTERNAL_PADDING = 16.dp

internal fun LazyListScope.addFileItems(
    files: List<FileEntity>,
    onItemClick: (Long) -> Unit,
    onItemRename: (Long, String) -> Unit,
    onItemDuplicate: (Long) -> Unit,
    onItemDelete: (Long) -> Unit,
    onItemTogglePin: (Long) -> Unit,
    viewModel: CalculatorViewModel
) {
    items(
        items = files,
        key = { it.id }
    ) { file ->
        FileItem(
            file = file,
            onClick = { onItemClick(file.id) },
            onRename = { newName -> onItemRename(file.id, newName) },
            onDuplicate = { onItemDuplicate(file.id) },
            onDismiss = { onItemDelete(file.id) },
            onTogglePin = { onItemTogglePin(file.id) },
            viewModel = viewModel
        )
    }
}

/**
 * Adds a list of home screen file items to a [LazyListScope].
 * Supports long-press selection mode.
 */
fun LazyListScope.addHomeFileItems(
    files: List<FileEntity>,
    isSelectionMode: Boolean,
    selectedFileIds: Set<Long>,
    onFileClick: (Long) -> Unit,
    onRename: (Long, String) -> Unit,
    onDuplicate: (Long) -> Unit,
    onDelete: (Long) -> Unit,
    onTogglePin: (Long) -> Unit,
    onLongClick: (Long) -> Unit,
    onToggleSelect: (Long) -> Unit,
    viewModel: CalculatorViewModel
) {
    items(
        items = files,
        key = { it.id }
    ) { file ->
        val itemModifier = Modifier
            .animateItem()
            .padding(vertical = FILE_ITEM_VERTICAL_PADDING)

        val isSelected = selectedFileIds.contains(file.id)

        FileItem(
            file = file,
            isSelectionMode = isSelectionMode,
            isSelected = isSelected,
            onClick = {
                if (isSelectionMode) {
                    if (!file.isLocked) {
                        onToggleSelect(file.id)
                    }
                } else {
                    onFileClick(file.id)
                }
            },
            onLongClick = {
                if (!file.isLocked) {
                    onLongClick(file.id)
                }
            },
            onRename = { onRename(file.id, it) },
            onDuplicate = { onDuplicate(file.id) },
            onDismiss = { onDelete(file.id) },
            onTogglePin = { onTogglePin(file.id) },
            viewModel = viewModel,
            modifier = itemModifier
        )
    }
}

@Composable
internal fun FileItem(
    file: FileEntity,
    onClick: () -> Unit,
    onRename: (String) -> Unit,
    onDuplicate: () -> Unit,
    onDismiss: () -> Unit,
    onTogglePin: () -> Unit,
    viewModel: CalculatorViewModel,
    modifier: Modifier = Modifier,
    isSelectionMode: Boolean = false,
    isSelected: Boolean = false,
    onLongClick: (() -> Unit)? = null
) {
    var showMenu by remember { mutableStateOf(false) }
    var showRenameDialog by remember { mutableStateOf(false) }
    var showInfoDialog by remember { mutableStateOf(false) }
    var showDeleteDialog by remember { mutableStateOf(false) }

    FileRowCard(
        file = file,
        title = AnnotatedString(file.name),
        onClick = onClick,
        onLongClick = onLongClick,
        modifier = modifier,
        leadingContent = if (isSelectionMode) {
            {
                Checkbox(
                    checked = isSelected,
                    onCheckedChange = null,
                    enabled = !file.isLocked,
                    colors = CheckboxDefaults.colors(
                        disabledUncheckedColor = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.38f),
                        disabledCheckedColor = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.38f)
                    )
                )
            }
        } else null,
        trailingContent = if (!isSelectionMode) {
            {
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
                        onDismissRequest = { showMenu = false },
                        modifier = Modifier.navigationBarsPadding()
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
                        if (!file.isTemporary) {
                            DropdownMenuItem(
                                text = { Text(if (file.isLocked) "Unlock" else "Lock") },
                                leadingIcon = {
                                    Icon(
                                        if (file.isLocked) Icons.Default.LockOpen else Icons.Default.Lock,
                                        contentDescription = null
                                    )
                                },
                                onClick = {
                                    showMenu = false
                                    viewModel.toggleLockFile(file.id)
                                }
                            )
                        }
                        DropdownMenuItem(
                            text = { Text("Rename") },
                            leadingIcon = { Icon(Icons.Default.Edit, contentDescription = null) },
                            enabled = !file.isLocked,
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
                                onDuplicate()
                            }
                        )
                        DropdownMenuItem(
                            text = { Text("Delete") },
                            leadingIcon = { Icon(Icons.Default.Delete, contentDescription = null) },
                            enabled = !file.isLocked,
                            onClick = {
                                showMenu = false
                                showDeleteDialog = true
                            }
                        )
                        DropdownMenuItem(
                            text = { Text("File info") },
                            leadingIcon = { Icon(Icons.Outlined.Info, contentDescription = null) },
                            onClick = {
                                showMenu = false
                                showInfoDialog = true
                            }
                        )
                    }
                }
            }
        } else null
    )

    if (showInfoDialog) {
        FileInfoDialog(
            viewModel = viewModel,
            file = file,
            onDismiss = { showInfoDialog = false }
        )
    }

    if (showRenameDialog) {
        RenameFileDialog(
            viewModel = viewModel,
            fileId = file.id,
            currentName = file.name,
            onDismiss = { showRenameDialog = false },
            onConfirm = { newName ->
                onRename(newName.take(Constants.MAX_FILE_NAME_LENGTH))
                true
            }
        )
    }

    if (showDeleteDialog) {
        DeleteFileDialog(
            fileName = file.name,
            onDismiss = { showDeleteDialog = false },
            onConfirm = {
                onDismiss()
                true
            }
        )
    }
}

private fun formatFileDate(timestamp: Long): String {
    val date = Date(timestamp)
    val timeFormat = SimpleDateFormat("hh:mm a", Locale.getDefault())
    val dateFormat = SimpleDateFormat("MMM dd, yyyy", Locale.getDefault())

    return when {
        DateUtils.isToday(timestamp) -> timeFormat.format(date)
        isYesterday(timestamp) -> "YESTERDAY ${timeFormat.format(date)}"
        else -> dateFormat.format(date)
    }
}

private fun isYesterday(timestamp: Long): Boolean {
    val yesterday = Calendar.getInstance().apply { add(Calendar.DAY_OF_YEAR, -1) }
    val time = Calendar.getInstance().apply { timeInMillis = timestamp }
    return yesterday.get(Calendar.YEAR) == time.get(Calendar.YEAR) &&
            yesterday.get(Calendar.DAY_OF_YEAR) == time.get(Calendar.DAY_OF_YEAR)
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
internal fun FileRowCard(
    file: FileEntity,
    title: AnnotatedString,
    onClick: () -> Unit,
    onLongClick: (() -> Unit)? = null,
    modifier: Modifier = Modifier,
    leadingContent: (@Composable () -> Unit)? = null,
    trailingContent: (@Composable () -> Unit)? = null
) {
    Card(
        modifier = modifier
            .fillMaxWidth()
            .combinedClickable(
                onClick = onClick,
                onLongClick = onLongClick
            ),
        shape = androidx.compose.ui.graphics.RectangleShape,
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainer)
    ) {
        Row(
            modifier = Modifier
                .padding(FILE_ITEM_INTERNAL_PADDING)
                .height(FILE_ITEM_CONTENT_HEIGHT),
            verticalAlignment = Alignment.CenterVertically
        ) {
            if (leadingContent != null) {
                leadingContent()
                Spacer(modifier = Modifier.width(16.dp))
            }
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
                            tint = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier
                                .padding(start = 4.dp)
                                .size(16.dp)
                        )
                    }
                    if (file.isLocked) {
                        Spacer(modifier = Modifier.width(8.dp))
                        Icon(
                            Icons.Default.Lock,
                            contentDescription = "Locked",
                            tint = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.size(16.dp)
                        )
                    }
                }
                val onSurfaceVariant = MaterialTheme.colorScheme.onSurfaceVariant
                val labelStyle = SpanStyle(color = onSurfaceVariant.copy(alpha = 0.6f))
                val valueStyle = SpanStyle(
                    color = onSurfaceVariant.copy(alpha = 0.6f),
                    fontWeight = FontWeight.Bold
                )

                Text(
                    text = buildAnnotatedString {
                        withStyle(labelStyle) { append("MODIFIED: ") }
                        withStyle(valueStyle) { append(formatFileDate(file.lastModified).uppercase()) }
                        withStyle(labelStyle) { append(" • ") }
                        withStyle(labelStyle) { append("CREATED: ") }
                        withStyle(valueStyle) { append(formatFileDate(file.createdAt).uppercase()) }
                    },
                    style = MaterialTheme.typography.labelSmall.copy(
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Normal
                    ),
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier.padding(top = 6.dp)
                )
            }
            if (trailingContent != null) {
                trailingContent()
            }
        }
    }
}

@Composable
fun SectionHeader(title: String) {
    Text(
        text = title,
        style = MaterialTheme.typography.titleSmall,
        fontSize = 12.sp,
        color = MaterialTheme.colorScheme.onSurfaceVariant,
        modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
    )
}
