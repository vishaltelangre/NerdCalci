package com.vishaltelangre.nerdcalci.ui.help

import android.util.Log
import android.widget.TextView
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.ScrollState
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.ArrowUpward
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.produceState
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.layout.positionInParent
import androidx.compose.ui.platform.LocalContext
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

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = { Text("Help", color = MaterialTheme.colorScheme.onSurface) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, "Back", tint = MaterialTheme.colorScheme.onSurface)
                    }
                },
                actions = {
                    if (tocItems.isNotEmpty()) {
                        Box {
                            IconButton(onClick = { showTocMenu = true }) {
                                Icon(
                                    Icons.Default.MoreVert,
                                    "Table of contents",
                                    tint = MaterialTheme.colorScheme.onSurface
                                )
                            }
                            DropdownMenu(
                                expanded = showTocMenu,
                                onDismissRequest = { showTocMenu = false },
                                modifier = Modifier.heightIn(max = 480.dp)
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
            HelpScreenContent(
                contentText = contentText,
                scrollState = scrollState,
                onTextViewReady = { textViewRef = it },
                onPositionReady = { androidViewTop = it }
            )
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
    contentText: String,
    scrollState: ScrollState,
    onTextViewReady: (TextView) -> Unit,
    onPositionReady: (Float) -> Unit
) {
    val context = LocalContext.current
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

    val markdownTextColor = MaterialTheme.colorScheme.onSurface.toArgb()

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
                markwon.setMarkdown(textView, contentText)
            }
        )
    }
}
