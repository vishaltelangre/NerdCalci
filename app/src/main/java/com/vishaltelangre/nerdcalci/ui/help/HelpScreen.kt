package com.vishaltelangre.nerdcalci.ui.help

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.vishaltelangre.nerdcalci.ui.theme.FiraCodeFamily

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
                .padding(horizontal = 16.dp)
                .verticalScroll(rememberScrollState())
        ) {
            Spacer(modifier = Modifier.height(16.dp))

            HelpSection(
                title = "Basic Calculations",
                content = buildAnnotatedString {
                    append("Perform calculations line by line:\n")
                    withStyle(SpanStyle(fontFamily = FiraCodeFamily)) {
                        append("2 + 3\n5 * 4  # × works too\n10 / 2  # ÷ works too")
                    }
                }
            )

            HelpSection(
                title = "Variables",
                content = buildAnnotatedString {
                    append("Assign values to variables and reuse them:\n")
                    withStyle(SpanStyle(fontFamily = FiraCodeFamily)) {
                        append("a = 10\nb = 20\na + b")
                    }
                }
            )

            HelpSection(
                title = "Percentages",
                content = buildAnnotatedString {
                    append("Calculate percentages easily:\n")
                    withStyle(SpanStyle(fontFamily = FiraCodeFamily)) {
                        append("20% of 500\n15% off 1000\n50000 + 10%\n50000 - 5%")
                    }
                }
            )

            HelpSection(
                title = "Comments",
                content = buildAnnotatedString {
                    append("Add comments using # symbol:\n")
                    withStyle(SpanStyle(fontFamily = FiraCodeFamily)) {
                        append("# Price calculations:\nprice = 100  # base price\nprice * 1.18  # with 18% tax")
                    }
                }
            )

            Spacer(modifier = Modifier.height(16.dp))
        }
    }
}

@Composable
private fun HelpSection(title: String, content: androidx.compose.ui.text.AnnotatedString) {
    Column {
        Text(
            text = title,
            style = MaterialTheme.typography.titleSmall,
            color = MaterialTheme.colorScheme.primary,
            modifier = Modifier.padding(bottom = 8.dp)
        )
        Text(
            text = content,
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurface
        )
        Spacer(modifier = Modifier.height(16.dp))
        HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)
        Spacer(modifier = Modifier.height(16.dp))
    }
}
