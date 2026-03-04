package com.vishaltelangre.nerdcalci.ui.help

import androidx.compose.foundation.background
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.produceState
import androidx.compose.runtime.remember
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.unit.sp
import androidx.core.content.res.ResourcesCompat
import io.noties.markwon.core.CorePlugin
import io.noties.markwon.core.MarkwonTheme
import com.vishaltelangre.nerdcalci.R
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import android.text.method.ScrollingMovementMethod
import android.util.Log
import android.widget.TextView
import io.noties.markwon.Markwon

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HelpScreen(onBack: () -> Unit) {
    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = { Text("Help", color = MaterialTheme.colorScheme.onSurface) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, "Back", tint = MaterialTheme.colorScheme.onSurface)
                    }
                },
                colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface
                )
            )
        },
        containerColor = MaterialTheme.colorScheme.background
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
        ) {
            HelpScreenContent()
        }
    }
}

@Composable
private fun HelpScreenContent() {
    val context = LocalContext.current
    val markwon = remember {
        val firaCodeTypeface = ResourcesCompat.getFont(context, R.font.fira_code_regular)

        val defaultTextSize = android.widget.TextView(context).textSize

        Markwon.builder(context)
            .usePlugin(object : CorePlugin() {
                override fun configureTheme(builder: MarkwonTheme.Builder) {
                    builder.codeTextSize((defaultTextSize * 0.85f).toInt())
                    firaCodeTypeface?.let {
                        builder.codeTypeface(it)
                        builder.codeBlockTypeface(it)
                    }
                }
            })
            .usePlugin(io.noties.markwon.ext.tables.TablePlugin.create(context))
            .build()
    }

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

    val markdownTextColor = MaterialTheme.colorScheme.onSurface.toArgb()

    AndroidView(
        modifier = Modifier
            .fillMaxSize()
            .padding(start = 16.dp, end = 16.dp, top = 16.dp),
        factory = { ctx ->
            TextView(ctx).apply {
                movementMethod = ScrollingMovementMethod.getInstance()
                isVerticalScrollBarEnabled = true
            }
        },
        update = { textView ->
            textView.setTextColor(markdownTextColor)
            markwon.setMarkdown(textView, markdownText)
        }
    )
}
