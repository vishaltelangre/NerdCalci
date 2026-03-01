package com.vishaltelangre.nerdcalci.ui.home

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SearchBar
import androidx.compose.material3.SearchBarDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import com.vishaltelangre.nerdcalci.data.local.entities.FileEntity
import com.vishaltelangre.nerdcalci.ui.components.FileRowCard

@OptIn(ExperimentalMaterial3Api::class)
@Composable
internal fun HomeSearchBar(
    query: String,
    files: List<FileEntity>,
    active: Boolean,
    onQueryChange: (String) -> Unit,
    onActiveChange: (Boolean) -> Unit,
    onClearQuery: () -> Unit,
    onDismissSearch: () -> Unit,
    onFileClick: (Long) -> Unit
) {
    val trimmedQuery = query.trim()
    val searchResultsListState = rememberSaveable(saver = LazyListState.Saver) {
        LazyListState()
    }
    val suggestions = remember(trimmedQuery, files) {
        if (trimmedQuery.isEmpty()) {
            files.take(6)
        } else {
            files
                .filter { it.name.contains(trimmedQuery, ignoreCase = true) }
                .take(6)
        }
    }

    SearchBar(
        inputField = {
            SearchBarDefaults.InputField(
                query = query,
                onQueryChange = onQueryChange,
                onSearch = { },
                expanded = active,
                onExpandedChange = onActiveChange,
                placeholder = { Text("Search files") },
                leadingIcon = {
                    if (active) {
                        IconButton(onClick = onDismissSearch) {
                            Icon(
                                imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                                contentDescription = "Back"
                            )
                        }
                    } else {
                        Icon(
                            imageVector = Icons.Default.Search,
                            contentDescription = null
                        )
                    }
                },
                trailingIcon = {
                    if (query.isNotBlank()) {
                        IconButton(onClick = onClearQuery) {
                            Icon(
                                imageVector = Icons.Default.Close,
                                contentDescription = "Clear search text"
                            )
                        }
                    } else if (active) {
                        IconButton(onClick = onDismissSearch) {
                            Icon(
                                imageVector = Icons.Default.Close,
                                contentDescription = "Close search"
                            )
                        }
                    }
                }
            )
        },
        expanded = active,
        onExpandedChange = onActiveChange,
        colors = SearchBarDefaults.colors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp)
    ) {
        Text(
            text = if (trimmedQuery.isBlank()) {
                "Recent files"
            } else {
                when (suggestions.size) {
                    0 -> "No matches"
                    1 -> "1 match"
                    else -> "${suggestions.size} matches"
                }
            },
            style = MaterialTheme.typography.labelLarge,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
        )
        LazyColumn(
            state = searchResultsListState,
            modifier = Modifier.fillMaxWidth()
        ) {
            if (suggestions.isNotEmpty()) {
                items(items = suggestions, key = { it.id }) { file ->
                    val highlightedName = buildHighlightedFileName(
                        fileName = file.name,
                        searchQuery = trimmedQuery,
                        highlightColor = MaterialTheme.colorScheme.primary
                    )
                    FileRowCard(
                        file = file,
                        title = highlightedName,
                        onClick = { onFileClick(file.id) },
                        modifier = Modifier.padding(horizontal = 16.dp, vertical = 4.dp)
                    )
                }
            }
        }
    }
}

private fun buildHighlightedFileName(
    fileName: String,
    searchQuery: String,
    highlightColor: Color
): AnnotatedString {
    val query = searchQuery.trim()
    if (query.isEmpty()) {
        return AnnotatedString(fileName)
    }

    val matchStart = fileName.indexOf(query, ignoreCase = true)
    if (matchStart == -1) {
        return AnnotatedString(fileName)
    }

    val matchEnd = matchStart + query.length
    return buildAnnotatedString {
        append(fileName.substring(0, matchStart))
        withStyle(
            style = SpanStyle(
                color = highlightColor,
                fontWeight = FontWeight.SemiBold
            )
        ) {
            append(fileName.substring(matchStart, matchEnd))
        }
        append(fileName.substring(matchEnd))
    }
}
