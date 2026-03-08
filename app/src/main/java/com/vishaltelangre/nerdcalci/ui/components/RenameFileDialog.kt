package com.vishaltelangre.nerdcalci.ui.components

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TextField
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.text.TextRange
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.unit.dp

import com.vishaltelangre.nerdcalci.core.Constants
import com.vishaltelangre.nerdcalci.ui.calculator.CalculatorViewModel

/**
 * Reusable dialog for renaming a file.
 *
 * @param currentName Current file name to show in the text field
 * @param onDismiss Callback when dialog is dismissed without renaming
 * @param onConfirm Callback with new name when rename is confirmed
 */
@Composable
fun RenameFileDialog(
    viewModel: CalculatorViewModel,
    fileId: Long,
    currentName: String,
    onDismiss: () -> Unit,
    onConfirm: (String) -> Unit
) {
    var textFieldValue by remember {
        mutableStateOf(
            TextFieldValue(
                text = currentName,
                selection = TextRange(currentName.length)
            )
        )
    }
    var isNameTaken by remember { mutableStateOf(false) }
    val focusRequester = remember { FocusRequester() }

    LaunchedEffect(textFieldValue.text) {
        val trimmedText = textFieldValue.text.trim()
        if (trimmedText.isNotEmpty() && trimmedText != currentName) {
            isNameTaken = viewModel.doesFileExist(trimmedText, fileId)
        } else {
            isNameTaken = false
        }
    }

    fun confirmRename() {
        if (textFieldValue.text.isNotBlank()) {
            onConfirm(textFieldValue.text.trim())
        }
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Rename File") },
        text = {
            Column {
                TextField(
                    value = textFieldValue,
                    onValueChange = { newValue ->
                        val finalText = newValue.text.replace("\n", "").take(Constants.MAX_FILE_NAME_LENGTH)
                        textFieldValue = newValue.copy(
                            text = finalText,
                            selection = TextRange(newValue.selection.start.coerceAtMost(finalText.length))
                        )
                    },
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(imeAction = ImeAction.Done),
                    keyboardActions = KeyboardActions(
                        onDone = { confirmRename() }
                    ),
                    modifier = Modifier.focusRequester(focusRequester)
                )
                if (isNameTaken) {
                    Text(
                        text = "A file with this name already exists",
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.error,
                        modifier = Modifier.padding(top = 4.dp)
                    )
                }
                Text(
                    text = "${textFieldValue.text.length}/${Constants.MAX_FILE_NAME_LENGTH}",
                    style = MaterialTheme.typography.labelMedium,
                    color = if (isNameTaken) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(top = 4.dp)
                )
            }
        },
        confirmButton = {
            TextButton(
                onClick = { confirmRename() },
                enabled = textFieldValue.text.isNotBlank() && !isNameTaken
            ) {
                Text("Rename")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )

    // Auto-focus the text field when dialog appears
    LaunchedEffect(Unit) {
        focusRequester.requestFocus()
    }
}
