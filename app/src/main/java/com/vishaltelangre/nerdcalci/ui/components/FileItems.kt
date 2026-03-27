package com.vishaltelangre.nerdcalci.ui.components

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyListScope
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.Info
import androidx.compose.material.icons.outlined.PushPin
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.lifecycle.viewModelScope
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.unit.dp
import java.text.SimpleDateFormat
import java.util.Locale
import kotlinx.coroutines.launch
import kotlinx.coroutines.delay
import androidx.compose.ui.text.font.FontWeight

import com.vishaltelangre.nerdcalci.core.Constants
import com.vishaltelangre.nerdcalci.data.local.entities.FileEntity
import com.vishaltelangre.nerdcalci.ui.calculator.CalculatorViewModel

private const val UNDO_TIMEOUT_SECONDS = 5
private val FILE_ITEM_CONTENT_HEIGHT = 56.dp
private val FILE_ITEM_VERTICAL_PADDING = 8.dp
private val FILE_ITEM_INTERNAL_PADDING = 16.dp
private val UNDO_BUTTON_HEIGHT = 36.dp

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
 * Adds a list of dismissible file items to a [LazyListScope].
 * This is used in the HomeScreen to provide swipe-to-delete functionality.
 */
/**
 * Adds a list of dismissible file items to a [LazyListScope].
 * This is used in the HomeScreen to provide swipe-to-delete functionality.
 * Handles both the normal state and the "Deleted [Undo]" state for each item.
 */
fun LazyListScope.addDismissibleFileItems(
    files: List<FileEntity>,
    excludedIds: Set<Long>,
    onFileClick: (Long) -> Unit,
    onRename: (Long, String) -> Unit,
    onDuplicate: (Long) -> Unit,
    onDelete: (Long) -> Unit,
    onTogglePin: (Long) -> Unit,
    onUndo: (Long) -> Unit,
    onDismiss: (Long) -> Unit,
    viewModel: CalculatorViewModel
) {
    items(
        items = files,
        key = { it.id }
    ) { file ->
        val itemModifier = Modifier
            .animateItem()
            .padding(vertical = FILE_ITEM_VERTICAL_PADDING)

        if (excludedIds.contains(file.id)) {
            DeletedUndoItem(
                file = file,
                onUndo = { onUndo(file.id) },
                onTimeout = { onDelete(file.id) },
                modifier = itemModifier
            )
        } else {
            DismissibleFileItem(
                file = file,
                onFileClick = onFileClick,
                onRename = { onRename(file.id, it) },
                onDuplicate = { onDuplicate(file.id) },
                onTogglePin = { onTogglePin(file.id) },
                onDismiss = { onDismiss(file.id) },
                viewModel = viewModel,
                modifier = itemModifier
            )
        }
    }
}

/**
 * A red row that replaces a swiped item, offering an "UNDO" action.
 */
@Composable
fun DeletedUndoItem(
    file: FileEntity,
    onUndo: () -> Unit,
    onTimeout: () -> Unit,
    modifier: Modifier = Modifier
) {
    var secondsLeft by remember(file.id) { mutableIntStateOf(UNDO_TIMEOUT_SECONDS) }

    LaunchedEffect(file.id) {
        while (secondsLeft > 0) {
            delay(1000)
            secondsLeft--
        }
        onTimeout()
    }

    Card(
        modifier = modifier
            .fillMaxWidth(),
        shape = androidx.compose.ui.graphics.RectangleShape,
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.errorContainer)
    ) {
        Row(
            modifier = Modifier
                .padding(FILE_ITEM_INTERNAL_PADDING)
                .fillMaxWidth()
                .height(FILE_ITEM_CONTENT_HEIGHT),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(
                text = "Deleted \"${file.name}\"",
                color = MaterialTheme.colorScheme.onErrorContainer,
                style = MaterialTheme.typography.bodyLarge,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                modifier = Modifier.weight(1f)
            )
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                TextButton(
                    onClick = onUndo,
                    contentPadding = PaddingValues(horizontal = 12.dp, vertical = 0.dp),
                    modifier = Modifier.height(UNDO_BUTTON_HEIGHT)
                ) {
                    Text(
                        text = "UNDO",
                        color = MaterialTheme.colorScheme.error,
                        style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.Bold)
                    )
                }
                Text(
                    text = "${secondsLeft}s left",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onErrorContainer.copy(alpha = 0.7f)
                )
            }
        }
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
    modifier: Modifier = Modifier
) {
    var showMenu by remember { mutableStateOf(false) }
    var showRenameDialog by remember { mutableStateOf(false) }
    var showInfoDialog by remember { mutableStateOf(false) }

    FileRowCard(
        file = file,
        title = AnnotatedString(file.name),
        onClick = onClick,
        modifier = modifier
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
                        onDuplicate()
                    }
                )
                DropdownMenuItem(
                    text = { Text("Delete") },
                    leadingIcon = { Icon(Icons.Default.Delete, contentDescription = null) },
                    onClick = {
                        showMenu = false
                        onDismiss()
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
        shape = androidx.compose.ui.graphics.RectangleShape,
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
    ) {
        Row(
            modifier = Modifier
                .padding(FILE_ITEM_INTERNAL_PADDING)
                .height(FILE_ITEM_CONTENT_HEIGHT),
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

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DismissibleFileItem(
    file: FileEntity,
    onFileClick: (Long) -> Unit,
    onRename: (String) -> Unit,
    onDuplicate: () -> Unit,
    onTogglePin: () -> Unit,
    onDismiss: () -> Unit,
    viewModel: CalculatorViewModel,
    modifier: Modifier = Modifier
) {
    val haptic = LocalHapticFeedback.current
    val dismissState = rememberSwipeToDismissBoxState(
        confirmValueChange = { value ->
            value == SwipeToDismissBoxValue.EndToStart
        }
    )

    // Handle dismissal when animation finishes
    LaunchedEffect(dismissState.currentValue) {
        if (dismissState.currentValue == SwipeToDismissBoxValue.EndToStart) {
            onDismiss()
            // Reset state so if/when the item is restored, it's not still "dismissed"
            dismissState.snapTo(SwipeToDismissBoxValue.Settled)
        }
    }

    // Trigger haptic feedback when the threshold is crossed
    LaunchedEffect(dismissState.targetValue) {
        if (dismissState.targetValue == SwipeToDismissBoxValue.EndToStart) {
            haptic.performHapticFeedback(HapticFeedbackType.LongPress)
        }
    }

    SwipeToDismissBox(
        state = dismissState,
        enableDismissFromStartToEnd = false,
        backgroundContent = { SwipeToDismissBackground(dismissState) },
        modifier = modifier
            .fillMaxWidth()
    ) {
        FileItem(
            file = file,
            onClick = { onFileClick(file.id) },
            onRename = onRename,
            onDuplicate = onDuplicate,
            onDismiss = onDismiss,
            onTogglePin = onTogglePin,
            viewModel = viewModel,
            modifier = Modifier
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SwipeToDismissBackground(dismissState: SwipeToDismissBoxState) {
    val color by animateColorAsState(
        when (dismissState.targetValue) {
            SwipeToDismissBoxValue.Settled -> Color.Transparent
            SwipeToDismissBoxValue.EndToStart -> MaterialTheme.colorScheme.errorContainer
            SwipeToDismissBoxValue.StartToEnd -> Color.Transparent
        }, label = "dismiss_background_color"
    )

    val alignment = Alignment.CenterEnd
    val icon = Icons.Default.Delete

    val scale by animateFloatAsState(
        if (dismissState.targetValue == SwipeToDismissBoxValue.Settled) 0.75f else 1f,
        label = "dismiss_background_scale"
    )

    Box(
        Modifier
            .fillMaxSize()
            .background(color)
            .padding(horizontal = 16.dp),
        contentAlignment = alignment
    ) {
        Icon(
            icon,
            contentDescription = "Delete",
            modifier = Modifier.scale(scale),
            tint = MaterialTheme.colorScheme.onErrorContainer
        )
    }
}

@Composable
fun SectionHeader(title: String) {
    Text(
        text = title,
        style = MaterialTheme.typography.titleSmall,
        color = MaterialTheme.colorScheme.onSurfaceVariant,
        modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
    )
}
