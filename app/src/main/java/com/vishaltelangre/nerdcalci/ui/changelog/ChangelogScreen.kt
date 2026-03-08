package com.vishaltelangre.nerdcalci.ui.changelog

import android.util.Log
import android.widget.TextView
import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Info
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.content.res.ResourcesCompat
import com.vishaltelangre.nerdcalci.R
import io.noties.markwon.Markwon
import io.noties.markwon.core.CorePlugin
import io.noties.markwon.core.MarkwonTheme
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.util.regex.Pattern

data class ChangelogSection(
    val title: String,
    val content: String
)

data class ChangelogVersion(
    val version: String,
    val date: String,
    val sections: List<ChangelogSection>
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ChangelogScreen(onBack: () -> Unit) {
    val context = LocalContext.current
    val changelogData by produceState<List<ChangelogVersion>?>(initialValue = null) {
        value = withContext(Dispatchers.IO) {
            try {
                val text = context.assets.open("CHANGELOG.md").bufferedReader().use { it.readText() }
                ChangelogParser.parse(text)
            } catch (e: Exception) {
                Log.e("ChangelogScreen", "Failed to load changelog", e)
                emptyList()
            }
        }
    }

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = { Text("Changelog", fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
                colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
                    containerColor = MaterialTheme.colorScheme.background
                )
            )
        },
        containerColor = MaterialTheme.colorScheme.background
    ) { padding ->
        if (changelogData == null) {
            // Loading state
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                CircularProgressIndicator()
            }
        } else if (changelogData!!.isEmpty()) {
            // Empty state
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(32.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                Icon(
                    imageVector = Icons.Default.Info,
                    contentDescription = null,
                    modifier = Modifier.size(64.dp),
                    tint = MaterialTheme.colorScheme.secondary.copy(alpha = 0.5f)
                )
                Spacer(modifier = Modifier.height(16.dp))
                Text(
                    text = "No changelog found",
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        } else {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding),
                contentPadding = PaddingValues(bottom = 32.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                item {
                    WavyDivider(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(24.dp)
                            .padding(vertical = 8.dp)
                    )
                }

                items(changelogData!!) { item ->
                    ChangelogVersionHeader(item)
                    item.sections.forEach { section ->
                        ChangelogSectionItem(section)
                    }
                    Spacer(modifier = Modifier.height(16.dp))
                }
            }
        }
    }
}

@Composable
fun ChangelogVersionHeader(item: ChangelogVersion) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Surface(
            color = MaterialTheme.colorScheme.primaryContainer,
            shape = RoundedCornerShape(8.dp)
        ) {
            Text(
                text = item.version,
                modifier = Modifier.padding(horizontal = 12.dp, vertical = 4.dp),
                style = MaterialTheme.typography.labelLarge,
                color = MaterialTheme.colorScheme.onPrimaryContainer,
                fontWeight = FontWeight.Bold
            )
        }
        Text(
            text = item.date,
            style = MaterialTheme.typography.labelMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            fontWeight = FontWeight.Medium
        )
    }
}

@Composable
private fun getCategoryColor(category: String): Color {
    return when (category.lowercase()) {
        "added" -> Color(0xFF4CAF50) // Green
        "changed" -> MaterialTheme.colorScheme.primary
        "fixed" -> Color(0xFFFF9800) // Orange
        "deprecated" -> Color(0xFFF44336) // Red
        "removed" -> Color(0xFF757575) // Gray
        "security" -> Color(0xFF9C27B0) // Purple
        else -> MaterialTheme.colorScheme.secondary
    }
}

@Composable
fun ChangelogSectionItem(section: ChangelogSection) {
    val categoryColor = getCategoryColor(section.title)

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 4.dp)
    ) {
        if (section.title.isNotEmpty()) {
            Text(
                text = section.title,
                style = MaterialTheme.typography.labelSmall,
                color = categoryColor,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(bottom = 4.dp)
            )
        }

        Card(
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surfaceContainerHigh
            ),
            elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(12.dp)
        ) {
            MarkdownText(
                markdown = section.content,
                modifier = Modifier.padding(12.dp)
            )
        }
    }
}

@Composable
fun MarkdownText(markdown: String, modifier: Modifier = Modifier) {
    val context = LocalContext.current
    val markwon = remember {
        val firaCodeTypeface = ResourcesCompat.getFont(context, R.font.fira_code_regular)
        // Retrieve default body text size from MaterialTheme instead of creating a View
        val defaultTextSizeSp = 16f

        Markwon.builder(context)
            .usePlugin(object : CorePlugin() {
                override fun configureTheme(builder: MarkwonTheme.Builder) {
                    builder.codeTextSize((defaultTextSizeSp * 0.85f).toInt())
                    firaCodeTypeface?.let {
                        builder.codeTypeface(it)
                        builder.codeBlockTypeface(it)
                    }
                }
            })
            .build()
    }

    val textColor = MaterialTheme.colorScheme.onSurface.toArgb()

    AndroidView(
        modifier = modifier,
        factory = { ctx ->
            TextView(ctx).apply {
                isVerticalScrollBarEnabled = false
            }
        },
        update = { textView ->
            textView.setTextColor(textColor)
            markwon.setMarkdown(textView, markdown)
        }
    )
}

@Composable
fun WavyDivider(modifier: Modifier = Modifier) {
    var isAnimating by remember { mutableStateOf(true) }
    LaunchedEffect(Unit) {
        kotlinx.coroutines.delay(10000) // Stop animation after 10 seconds to save battery
        isAnimating = false
    }

    val infiniteTransition = rememberInfiniteTransition(label = "wave")
    val phase by if (isAnimating) {
        infiniteTransition.animateFloat(
            initialValue = 0f,
            targetValue = 1f,
            animationSpec = infiniteRepeatable(
                animation = tween(2000, easing = LinearEasing),
                repeatMode = RepeatMode.Restart
            ),
            label = "phase"
        )
    } else {
        remember { mutableStateOf(0f) }
    }

    val color = MaterialTheme.colorScheme.primary.copy(alpha = 0.3f)
    Canvas(modifier = modifier) {
        val width = size.width
        val height = size.height
        val path = Path()
        val waveWidth = 48.dp.toPx()
        val waveHeight = 6.dp.toPx()

        val offset = phase * waveWidth

        path.moveTo(-waveWidth + offset, height / 2)
        var x = -waveWidth + offset
        while (x < width + waveWidth) {
            path.relativeQuadraticTo(waveWidth / 4, -waveHeight, waveWidth / 2, 0f)
            path.relativeQuadraticTo(waveWidth / 4, waveHeight, waveWidth / 2, 0f)
            x += waveWidth
        }

        drawPath(
            path = path,
            color = color,
            style = Stroke(width = 2.dp.toPx())
        )
    }
}

private object ChangelogParser {
    private val VERSION_PATTERN = Pattern.compile("## \\[(.*?)\\] - (.*?)\\n(.*?)(?=\\n## |$)", Pattern.DOTALL)
    private val SECTION_PATTERN = Pattern.compile("### (.*?)\\n(.*?)(?=\\n### |$)", Pattern.DOTALL)

    fun parse(text: String): List<ChangelogVersion> {
        val versions = mutableListOf<ChangelogVersion>()
        val versionMatcher = VERSION_PATTERN.matcher(text)

        while (versionMatcher.find()) {
            val vname = versionMatcher.group(1) ?: ""
            val vdate = versionMatcher.group(2) ?: ""
            val vcontent = versionMatcher.group(3) ?: ""

            val sections = mutableListOf<ChangelogSection>()
            val sectionMatcher = SECTION_PATTERN.matcher(vcontent)

            var foundSection = false
            while (sectionMatcher.find()) {
                val sname = sectionMatcher.group(1) ?: ""
                val scontent = sectionMatcher.group(2)?.trim() ?: ""
                sections.add(ChangelogSection(sname, scontent))
                foundSection = true
            }

            if (!foundSection) {
                // For older entries that don't have ### headers
                sections.add(ChangelogSection("", vcontent.trim()))
            }

            versions.add(ChangelogVersion(vname, vdate, sections))
        }
        return versions
    }
}
