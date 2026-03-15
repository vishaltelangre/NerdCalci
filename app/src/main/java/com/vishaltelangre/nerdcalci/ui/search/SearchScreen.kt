package com.vishaltelangre.nerdcalci.ui.search

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
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
import com.vishaltelangre.nerdcalci.ui.calculator.CalculatorViewModel
import com.vishaltelangre.nerdcalci.utils.FileFuzzyMatcher
import com.vishaltelangre.nerdcalci.utils.FileFuzzyMatchResult
import com.vishaltelangre.nerdcalci.ui.components.FileRowCard

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SearchScreen(
    viewModel: CalculatorViewModel,
    onFileClick: (Long) -> Unit,
    onBack: () -> Unit
) {
    val files by viewModel.allFiles.collectAsState(initial = emptyList())
    var searchQuery by rememberSaveable { mutableStateOf("") }
    val trimmedQuery = searchQuery.trim()

    val searchResultsListState = rememberSaveable(saver = LazyListState.Saver) {
        LazyListState()
    }

    val suggestions = remember(trimmedQuery, files) {
        val query = trimmedQuery
        if (query.isEmpty()) {
            files.take(10).map { it to FileFuzzyMatchResult(0, emptyList()) }
        } else {
            files.mapNotNull { file ->
                FileFuzzyMatcher.fuzzyMatch(file.name, query)?.let { file to it }
            }.sortedByDescending { it.second.score }
        }
    }

    Scaffold(
        topBar = {
            SearchBar(
                inputField = {
                    SearchBarDefaults.InputField(
                        query = searchQuery,
                        onQueryChange = { searchQuery = it.replace("\n", "") },
                        onSearch = { },
                        expanded = true,
                        onExpandedChange = { if (!it) onBack() },
                        placeholder = { Text("Search files") },
                        leadingIcon = {
                            IconButton(onClick = onBack) {
                                Icon(
                                    imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                                    contentDescription = "Back"
                                )
                            }
                        },
                        trailingIcon = {
                            if (searchQuery.isNotBlank()) {
                                IconButton(onClick = { searchQuery = "" }) {
                                    Icon(
                                        imageVector = Icons.Default.Close,
                                        contentDescription = "Clear search text"
                                    )
                                }
                            }
                        }
                    )
                },
                expanded = true,
                onExpandedChange = { if (!it) onBack() },
                colors = SearchBarDefaults.colors(
                    containerColor = MaterialTheme.colorScheme.background
                ),
                modifier = Modifier.fillMaxWidth()
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
                    items(items = suggestions, key = { it.first.id }) { (file, match) ->
                        val highlightedName = buildHighlightedFileName(
                            fileName = file.name,
                            matchedIndices = match.matchedIndices,
                            highlightColor = MaterialTheme.colorScheme.primary
                        )
                        FileRowCard(
                            file = file,
                            title = highlightedName,
                            onClick = { onFileClick(file.id) },
                            modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
                        )
                    }
                }
            }
        },
        containerColor = MaterialTheme.colorScheme.background
    ) { padding ->
        // Content is handled by the SearchBar expanded state
        Spacer(modifier = Modifier.padding(padding))
    }

    BackHandler(onBack = onBack)
}

private fun buildHighlightedFileName(
    fileName: String,
    matchedIndices: List<Int>,
    highlightColor: Color
): AnnotatedString {
    if (matchedIndices.isEmpty()) {
        return AnnotatedString(fileName)
    }

    return buildAnnotatedString {
        val indicesSet = matchedIndices.toSet()
        fileName.forEachIndexed { index, char ->
            if (index in indicesSet) {
                withStyle(
                    style = SpanStyle(
                        color = highlightColor,
                        fontWeight = FontWeight.SemiBold
                    )
                ) {
                    append(char)
                }
            } else {
                append(char)
            }
        }
    }
}
