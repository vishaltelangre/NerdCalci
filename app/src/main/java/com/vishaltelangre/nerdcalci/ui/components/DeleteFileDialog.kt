package com.vishaltelangre.nerdcalci.ui.components

import androidx.compose.material3.AlertDialog
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.launch

/**
 * Reusable confirmation dialog for deleting a file.
 *
 * @param fileName Name of the file to be deleted (shown in the warning message)
 * @param onDismiss Callback when dialog is dismissed without deleting
 * @param onConfirm Callback when user confirms deletion
 */
@Composable
fun DeleteFileDialog(
    fileName: String,
    onDismiss: () -> Unit,
    onConfirm: suspend () -> Boolean
) {
    var isDeleting by remember { mutableStateOf(false) }
    var deleteError by remember { mutableStateOf<String?>(null) }
    val coroutineScope = rememberCoroutineScope()

    fun confirmDelete() {
        if (isDeleting) return
        isDeleting = true
        coroutineScope.launch {
            try {
                val success = runCatching { onConfirm() }
                    .getOrElse { throwable ->
                        deleteError = throwable.message ?: "Failed to delete file"
                        false
                    }
                if (success) {
                    onDismiss()
                } else if (deleteError == null) {
                    deleteError = "Failed to delete file"
                }
            } finally {
                isDeleting = false
            }
        }
    }

    AlertDialog(
        onDismissRequest = { if (!isDeleting) onDismiss() },
        title = { Text("Delete file?") },
        text = {
            Column {
                Text("This will permanently delete \"$fileName\" and all its contents. This action cannot be undone.")
                if (deleteError != null) {
                    Text(
                        text = deleteError ?: "",
                        color = MaterialTheme.colorScheme.error,
                        style = MaterialTheme.typography.labelMedium,
                        modifier = Modifier.padding(top = 8.dp)
                    )
                }
            }
        },
        confirmButton = {
            TextButton(
                onClick = { confirmDelete() },
                enabled = !isDeleting
            ) {
                Text("Delete", color = if (isDeleting) MaterialTheme.colorScheme.error.copy(alpha = 0.38f) else MaterialTheme.colorScheme.error)
            }
        },
        dismissButton = {
            TextButton(
                onClick = onDismiss,
                enabled = !isDeleting
            ) {
                Text("Cancel")
            }
        }
    )
}

/**
 * Reusable confirmation dialog for deleting multiple files.
 */
@Composable
fun DeleteFilesDialog(
    count: Int,
    onDismiss: () -> Unit,
    onConfirm: suspend () -> Boolean
) {
    var isDeleting by remember { mutableStateOf(false) }
    var deleteError by remember { mutableStateOf<String?>(null) }
    val coroutineScope = rememberCoroutineScope()

    fun confirmDelete() {
        if (isDeleting) return
        isDeleting = true
        coroutineScope.launch {
            try {
                val success = runCatching { onConfirm() }
                    .getOrElse { throwable ->
                        deleteError = throwable.message ?: "Failed to delete files"
                        false
                    }
                if (success) {
                    onDismiss()
                } else if (deleteError == null) {
                    deleteError = "Failed to delete files"
                }
            } finally {
                isDeleting = false
            }
        }
    }

    AlertDialog(
        onDismissRequest = { if (!isDeleting) onDismiss() },
        title = { Text(if (count == 1) "Delete file?" else "Delete $count files?") },
        text = {
            Column {
                Text(
                    if (count == 1)
                        "This will permanently delete the selected file and all its contents. This action cannot be undone."
                    else
                        "This will permanently delete the $count selected files and all their contents. This action cannot be undone."
                )
                if (deleteError != null) {
                    Text(
                        text = deleteError ?: "",
                        color = MaterialTheme.colorScheme.error,
                        style = MaterialTheme.typography.labelMedium,
                        modifier = Modifier.padding(top = 8.dp)
                    )
                }
            }
        },
        confirmButton = {
            TextButton(
                onClick = { confirmDelete() },
                enabled = !isDeleting
            ) {
                Text("Delete", color = if (isDeleting) MaterialTheme.colorScheme.error.copy(alpha = 0.38f) else MaterialTheme.colorScheme.error)
            }
        },
        dismissButton = {
            TextButton(
                onClick = onDismiss,
                enabled = !isDeleting
            ) {
                Text("Cancel")
            }
        }
    )
}
