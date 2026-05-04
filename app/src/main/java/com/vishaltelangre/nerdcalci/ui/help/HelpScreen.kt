package com.vishaltelangre.nerdcalci.ui.help

import android.text.Spannable
import android.text.SpannableStringBuilder
import android.text.style.BackgroundColorSpan
import android.util.Log
import android.widget.TextView
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.ScrollState
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.ArrowDownward
import androidx.compose.material.icons.filled.ArrowUpward
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LocalContentColor
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.produceState
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.layout.positionInParent
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.content.res.ResourcesCompat
import com.vishaltelangre.nerdcalci.R
import io.noties.markwon.Markwon
import io.noties.markwon.core.MarkwonTheme
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HelpScreen(onBack: () -> Unit) {
    val scrollState = rememberScrollState()
    val coroutineScope = rememberCoroutineScope()
    val context = LocalContext.current

    // Read the markdown text from the assets/REFERENCE.md file bundled during build
    val markdownText by produceState(initialValue = "", context) {
        value = try {
            withContext(Dispatchers.IO) {
                context.assets.open("REFERENCE.md").bufferedReader().use { it.readText() }
            }
        } catch (e: Exception) {
            Log.e("HelpScreen", "Failed to load REFERENCE.md from assets", e)
            "Error loading language reference. Please report this issue."
        }
    }

    val tocEndIndex = markdownText.indexOf("\n## ")
    val (tocText, contentText) = if (tocEndIndex != -1) {
        markdownText.substring(0, tocEndIndex) to markdownText.substring(tocEndIndex)
    } else {
        "" to markdownText
    }

    val tocItems = remember(tocText) {
        if (tocText.isBlank()) emptyList()
        else {
            val tocLinkRegex = Regex("\\[([^\\]]+)\\]\\((#[^)]+)\\)")
            tocText.split("\n")
                .filter { it.isNotBlank() }
                .mapNotNull { line ->
                    val match = tocLinkRegex.find(line)
                    if (match != null) {
                        val title = match.groupValues[1]
                        val anchor = match.groupValues[2].substring(1)
                        val indent = line.takeWhile { it == ' ' }.length
                        TocItem(title, anchor, indent)
                    } else null
                }
        }
    }

    var showTocMenu by remember { mutableStateOf(false) }
    var androidViewTop by remember { mutableStateOf(0f) }
    var textViewRef by remember { mutableStateOf<TextView?>(null) }

    val primaryColor = MaterialTheme.colorScheme.primary.toArgb()
    val markwon: Markwon = remember(context, primaryColor) {
        val firaCodeTypeface = ResourcesCompat.getFont(context, R.font.fira_code_regular)
        val defaultTextSize = TextView(context).textSize

        Markwon.builder(context)
            .usePlugin(object : io.noties.markwon.AbstractMarkwonPlugin() {
                override fun configureTheme(builder: MarkwonTheme.Builder) {
                    builder.codeTextSize((defaultTextSize * 0.85f).toInt())
                    builder.linkColor(primaryColor)
                    builder.listItemColor(primaryColor)
                    builder.headingTextSizeMultipliers(floatArrayOf(2f, 1.5f, 1.17f, 1f, .83f, .67f))
                    firaCodeTypeface?.let {
                        builder.codeTypeface(it)
                        builder.codeBlockTypeface(it)
                    }
                }
            })
            .usePlugin(io.noties.markwon.ext.tables.TablePlugin.create(context))
            .build()
    }

    val renderedText = remember(contentText, markwon) {
        if (contentText.isBlank()) null else markwon.render(markwon.parse(contentText))
    }

    var isSearchActive by remember { mutableStateOf(false) }
    var searchQuery by remember { mutableStateOf("") }
    var currentMatchIndex by remember { mutableIntStateOf(0) }

    val matchOffsets = remember(renderedText, searchQuery) {
        if (searchQuery.isBlank() || renderedText == null) emptyList()
        else {
            val plainText = renderedText.toString()
            val result = mutableListOf<IntRange>()
            var index = plainText.indexOf(searchQuery, ignoreCase = true)
            while (index != -1) {
                result.add(index until index + searchQuery.length)
                index = plainText.indexOf(searchQuery, index + searchQuery.length, ignoreCase = true)
            }
            result
        }
    }

    LaunchedEffect(currentMatchIndex, matchOffsets, androidViewTop) {
        if (isSearchActive && matchOffsets.isNotEmpty() && currentMatchIndex in matchOffsets.indices) {
            textViewRef?.let { tv ->
                val offset = matchOffsets[currentMatchIndex].first
                tv.layout?.let { layout ->
                    val line = layout.getLineForOffset(offset)
                    val lineTop = layout.getLineTop(line)
                    scrollState.animateScrollTo((androidViewTop + lineTop).toInt())
                }
            }
        }
    }

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = { Text("Help", color = MaterialTheme.colorScheme.onSurface) },
                navigationIcon = {
                    IconButton(onClick = {
                        if (isSearchActive) {
                            isSearchActive = false
                            searchQuery = ""
                        } else {
                            onBack()
                        }
                    }) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, "Back", tint = MaterialTheme.colorScheme.onSurface)
                    }
                },
                actions = {
                    IconButton(onClick = {
                        isSearchActive = !isSearchActive
                        if (!isSearchActive) searchQuery = ""
                    }) {
                        Icon(
                            if (isSearchActive) Icons.Default.Close else Icons.Default.Search,
                            if (isSearchActive) "Close search" else "Find in page",
                            tint = MaterialTheme.colorScheme.onSurface
                        )
                    }
                    if (!isSearchActive && tocItems.isNotEmpty()) {
                        Box {
                            IconButton(onClick = { showTocMenu = true }) {
                                Icon(
                                    Icons.Default.MoreVert,
                                    "Table of contents",
                                    tint = MaterialTheme.colorScheme.onSurface
                                )
                            }
                            val configuration = LocalConfiguration.current
                            val screenHeight = configuration.screenHeightDp.dp
                            DropdownMenu(
                                expanded = showTocMenu,
                                onDismissRequest = { showTocMenu = false },
                                modifier = Modifier
                                    .heightIn(max = screenHeight * 0.8f)
                                    .navigationBarsPadding()
                            ) {
                                tocItems.forEach { item ->
                                    val cleanTitle = item.title.replace("\\&", "&")
                                    DropdownMenuItem(
                                        text = {
                                            Text(
                                                cleanTitle,
                                                modifier = Modifier.padding(start = (item.indent * 8).dp)
                                            )
                                        },
                                        onClick = {
                                            showTocMenu = false
                                            textViewRef?.let { tv ->
                                                scrollToAnchor(
                                                    item.anchor,
                                                    tv,
                                                    scrollState,
                                                    androidViewTop,
                                                    coroutineScope
                                                )
                                            }
                                        }
                                    )
                                }
                            }
                        }
                    }
                },
                colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface
                )
            )
        },
        floatingActionButton = {
            AnimatedVisibility(
                visible = scrollState.value > 100,
                enter = fadeIn(),
                exit = fadeOut()
            ) {
                FloatingActionButton(
                    onClick = {
                        coroutineScope.launch {
                            scrollState.animateScrollTo(0)
                        }
                    },
                    containerColor = MaterialTheme.colorScheme.primaryContainer,
                    contentColor = MaterialTheme.colorScheme.onPrimaryContainer
                ) {
                    Icon(Icons.Default.ArrowUpward, "Scroll to top")
                }
            }
        },
        containerColor = MaterialTheme.colorScheme.background
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
        ) {
            if (isSearchActive) {
                FindBar(
                    query = searchQuery,
                    onQueryChange = {
                        searchQuery = it
                        currentMatchIndex = 0
                    },
                    matchCount = matchOffsets.size,
                    currentMatchIndex = currentMatchIndex,
                    onNext = {
                        if (matchOffsets.isNotEmpty()) {
                            currentMatchIndex = (currentMatchIndex + 1) % matchOffsets.size
                        }
                    },
                    onPrev = {
                        if (matchOffsets.isNotEmpty()) {
                            currentMatchIndex = (currentMatchIndex - 1 + matchOffsets.size) % matchOffsets.size
                        }
                    },
                    onClose = {
                        isSearchActive = false
                        searchQuery = ""
                    }
                )
                HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f))
            }
            HelpScreenContent(
                markwon = markwon,
                renderedText = renderedText,
                scrollState = scrollState,
                searchQuery = searchQuery,
                matchOffsets = matchOffsets,
                currentMatchIndex = currentMatchIndex,
                onTextViewReady = { textViewRef = it },
                onPositionReady = { androidViewTop = it }
            )
        }
    }
}

@Composable
private fun FindBar(
    query: String,
    onQueryChange: (String) -> Unit,
    matchCount: Int,
    currentMatchIndex: Int,
    onNext: () -> Unit,
    onPrev: () -> Unit,
    onClose: () -> Unit
) {
    val focusRequester = remember { FocusRequester() }
    LaunchedEffect(Unit) {
        focusRequester.requestFocus()
    }

    Surface(
        modifier = Modifier.fillMaxWidth(),
        color = MaterialTheme.colorScheme.surfaceVariant
    ) {
        Row(
            modifier = Modifier
                .padding(horizontal = 12.dp, vertical = 8.dp)
                .fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                Icons.Default.Search,
                contentDescription = null,
                modifier = Modifier.size(20.dp),
                tint = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Box(modifier = Modifier
                .weight(1f)
                .padding(horizontal = 12.dp)) {
                if (query.isEmpty()) {
                    Text(
                        "Find in page",
                        style = MaterialTheme.typography.bodyLarge,
                        color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f)
                    )
                }
                BasicTextField(
                    value = query,
                    onValueChange = onQueryChange,
                    modifier = Modifier
                        .fillMaxWidth()
                        .focusRequester(focusRequester),
                    textStyle = MaterialTheme.typography.bodyLarge.copy(color = MaterialTheme.colorScheme.onSurface),
                    singleLine = true,
                    cursorBrush = SolidColor(MaterialTheme.colorScheme.primary)
                )
            }
            if (query.isNotEmpty()) {
                Text(
                    text = if (matchCount > 0) "${currentMatchIndex + 1}/$matchCount" else "0/0",
                    style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold),
                    color = if (matchCount > 0) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.error,
                    modifier = Modifier.padding(horizontal = 4.dp)
                )
                IconButton(onClick = onPrev, enabled = matchCount > 0) {
                    Icon(Icons.Default.ArrowUpward, "Previous match", modifier = Modifier.size(20.dp))
                }
                IconButton(onClick = onNext, enabled = matchCount > 0) {
                    Icon(Icons.Default.ArrowDownward, "Next match", modifier = Modifier.size(20.dp))
                }
                IconButton(onClick = { onQueryChange("") }) {
                    Icon(Icons.Default.Close, "Clear", modifier = Modifier.size(20.dp))
                }
            }
        }
    }
}

private fun slugify(text: String): String {
    return text.lowercase()
        .replace(Regex("[^a-z0-9]+"), "-")
        .trim('-')
        .replace(Regex("-+"), "-")
}

data class TocItem(val title: String, val anchor: String, val indent: Int)

private fun scrollToAnchor(
    anchor: String,
    textView: TextView,
    scrollState: ScrollState,
    androidViewTop: Float,
    coroutineScope: kotlinx.coroutines.CoroutineScope
) {
    val text = textView.text as? android.text.Spanned ?: return
    val lines = text.toString().split("\n")
    var matchOffset = -1
    var currentOffset = 0
    val slugAnchor = slugify(anchor)

    for (i in lines.indices) {
        val lineText = lines[i].trim()
        val cleanText = if (lineText.startsWith("#")) {
            lineText.replace(Regex("^#+\\s+"), "")
        } else {
            lineText
        }
        val slugClean = slugify(cleanText)

        if (slugClean.isNotEmpty() && slugClean == slugAnchor) {
            matchOffset = currentOffset
            break
        }
        currentOffset += lines[i].length + 1
    }

    if (matchOffset != -1) {
        textView.layout?.let { layout ->
            val lineIndex = layout.getLineForOffset(matchOffset)
            val lineTop = layout.getLineTop(lineIndex)
            coroutineScope.launch {
                scrollState.animateScrollTo((androidViewTop + lineTop).toInt())
            }
        }
    }
}

@Composable
private fun HelpScreenContent(
    markwon: Markwon,
    renderedText: android.text.Spanned?,
    scrollState: ScrollState,
    searchQuery: String,
    matchOffsets: List<IntRange>,
    currentMatchIndex: Int,
    onTextViewReady: (TextView) -> Unit,
    onPositionReady: (Float) -> Unit
) {
    val markdownTextColor = MaterialTheme.colorScheme.onSurface.toArgb()

    // Detect if the current theme is yellowish to avoid clashing with the default yellow highlights
    val primaryColorArgb = MaterialTheme.colorScheme.primary.toArgb()
    val isThemeYellowish = remember(primaryColorArgb) {
        val hsv = FloatArray(3)
        android.graphics.Color.colorToHSV(primaryColorArgb, hsv)
        // Hue between 20 (Orange) and 70 (Greenish-Yellow) with sufficient saturation
        hsv[0] in 20f..70f && hsv[1] > 0.4f
    }

    val highlightColor = remember(isThemeYellowish) {
        if (isThemeYellowish) {
            android.graphics.Color.argb(100, 0, 255, 255) // Cyan
        } else {
            android.graphics.Color.argb(100, 255, 255, 0) // Yellow
        }
    }

    val currentHighlightColor = remember(isThemeYellowish) {
        if (isThemeYellowish) {
            android.graphics.Color.argb(160, 0, 180, 255) // Bright Blue
        } else {
            android.graphics.Color.argb(160, 255, 150, 0) // Orange
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(scrollState)
    ) {
        AndroidView(
            modifier = Modifier
                .fillMaxWidth()
                .padding(start = 16.dp, end = 16.dp, top = 16.dp)
                .onGloballyPositioned { layoutCoordinates ->
                    onPositionReady(layoutCoordinates.positionInParent().y)
                },
            factory = { ctx ->
                TextView(ctx).apply {
                    isVerticalScrollBarEnabled = false
                    isNestedScrollingEnabled = false
                }.also { onTextViewReady(it) }
            },
            update = { textView ->
                textView.setTextColor(markdownTextColor)
                if (renderedText == null) return@AndroidView

                val spannable = SpannableStringBuilder(renderedText)

                if (searchQuery.isNotBlank() && matchOffsets.isNotEmpty()) {
                    matchOffsets.forEachIndexed { i, range ->
                        val color = if (i == currentMatchIndex) currentHighlightColor else highlightColor
                        spannable.setSpan(
                            BackgroundColorSpan(color),
                            range.first,
                            range.last + 1,
                            Spannable.SPAN_EXCLUSIVE_EXCLUSIVE
                        )
                    }
                }

                markwon.setParsedMarkdown(textView, spannable)
            }
        )
    }
}
