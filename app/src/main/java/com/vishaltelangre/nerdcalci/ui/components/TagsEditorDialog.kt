package com.vishaltelangre.nerdcalci.ui.components

import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import com.vishaltelangre.nerdcalci.data.local.entities.normalizeTag
import com.vishaltelangre.nerdcalci.ui.calculator.CalculatorViewModel

@OptIn(ExperimentalLayoutApi::class, ExperimentalMaterial3Api::class)
@Composable
fun TagsEditorDialog(
    initialTags: List<String>,
    viewModel: CalculatorViewModel,
    onDismiss: () -> Unit,
    onConfirm: (List<String>) -> Unit
) {
    val allTags by viewModel.allTags.collectAsState(initial = emptyList())
    var currentTags by remember { mutableStateOf(initialTags) }
    var inputText by remember { mutableStateOf("") }
    val focusRequester = remember { FocusRequester() }
    val interactionSource = remember { MutableInteractionSource() }

    val unaddedTags = allTags.filter { !currentTags.contains(it) }
    val suggestions = if (inputText.isNotBlank()) {
        unaddedTags.filter { it.contains(inputText.trim(), ignoreCase = true) }
    } else {
        emptyList()
    }

    fun addTag(tag: String) {
        val normalized = tag.normalizeTag()
        if (normalized.isNotBlank() && !currentTags.contains(normalized)) {
            currentTags = currentTags + normalized
        }
        inputText = ""
    }

    fun removeTag(tag: String) {
        currentTags = currentTags.filter { it != tag }
    }

    fun confirmTags() {
        if (inputText.isNotBlank()) {
            addTag(inputText)
        }
        onConfirm(currentTags)
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Edit Tags") },
        text = {
            Column {
                BasicTextField(
                    value = inputText,
                    onValueChange = { newValue ->
                        if (newValue.endsWith(",")) {
                            addTag(newValue.dropLast(1))
                        } else {
                            inputText = newValue
                        }
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .focusRequester(focusRequester),
                    textStyle = MaterialTheme.typography.bodyLarge.copy(color = MaterialTheme.colorScheme.onSurface),
                    cursorBrush = SolidColor(MaterialTheme.colorScheme.primary),
                    keyboardOptions = KeyboardOptions(imeAction = ImeAction.Done),
                    keyboardActions = KeyboardActions(onDone = { confirmTags() }),
                    interactionSource = interactionSource,
                    decorationBox = { innerTextField ->
                        OutlinedTextFieldDefaults.DecorationBox(
                            value = inputText,
                            visualTransformation = VisualTransformation.None,
                            innerTextField = {
                                FlowRow(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                                    verticalArrangement = Arrangement.spacedBy(4.dp)
                                ) {
                                    currentTags.forEach { tag ->
                                        InputChip(
                                            selected = false,
                                            onClick = { removeTag(tag) },
                                            label = { Text("#$tag") },
                                            trailingIcon = {
                                                Icon(
                                                    Icons.Default.Close,
                                                    contentDescription = "Remove tag",
                                                    modifier = Modifier
                                                        .size(16.dp)
                                                        .clickable { removeTag(tag) }
                                                )
                                            }
                                        )
                                    }
                                    Box(
                                        modifier = Modifier.align(Alignment.CenterVertically),
                                        contentAlignment = Alignment.CenterStart
                                    ) {
                                        if (inputText.isEmpty() && currentTags.isEmpty()) {
                                            Text(
                                                text = "Add tags...",
                                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                                style = MaterialTheme.typography.bodyLarge
                                            )
                                        }
                                        innerTextField()
                                    }
                                }
                            },
                            singleLine = false,
                            enabled = true,
                            interactionSource = interactionSource,
                            contentPadding = PaddingValues(12.dp)
                        )
                    }
                )

                Text(
                    text = "Type comma (,) to add a tag",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(top = 4.dp)
                )

                if (suggestions.isNotEmpty()) {
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "Suggestions",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    LazyRow(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        items(suggestions) { suggestion ->
                            SuggestionChip(
                                onClick = { addTag(suggestion) },
                                label = { Text("#$suggestion") }
                            )
                        }
                    }
                }
            }
        },
        confirmButton = {
            TextButton(onClick = { confirmTags() }) {
                Text("Save")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )

    LaunchedEffect(Unit) {
        focusRequester.requestFocus()
    }
}
