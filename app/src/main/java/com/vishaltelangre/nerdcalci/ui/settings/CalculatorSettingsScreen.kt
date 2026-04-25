package com.vishaltelangre.nerdcalci.ui.settings

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.BorderHorizontal
import androidx.compose.material.icons.filled.Calculate
import androidx.compose.material.icons.filled.Chat
import androidx.compose.material.icons.filled.FlashOn
import androidx.compose.material.icons.filled.FormatListNumbered
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.MoreHoriz
import androidx.compose.material.icons.filled.Pin
import androidx.compose.material.icons.filled.Public
import androidx.compose.material.icons.filled.SpaceBar
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
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import com.vishaltelangre.nerdcalci.core.Constants
import com.vishaltelangre.nerdcalci.core.MathEngine
import com.vishaltelangre.nerdcalci.ui.components.RegionSelectorDialog
import com.vishaltelangre.nerdcalci.ui.theme.FiraCodeFamily
import com.vishaltelangre.nerdcalci.utils.RegionUtils
import kotlin.math.roundToInt

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CalculatorSettingsScreen(
    precision: Int,
    onPrecisionChange: (Int) -> Unit,
    rationalMode: Boolean,
    onRationalModeChange: (Boolean) -> Unit,
    regionCode: String,
    onRegionCodeChange: (String) -> Unit,
    groupingSeparatorEnabled: Boolean,
    onGroupingSeparatorEnabledChange: (Boolean) -> Unit,
    showPrecisionEllipsis: Boolean,
    onShowPrecisionEllipsisChange: (Boolean) -> Unit,
    editorFontSize: Float,
    onEditorFontSizeChange: (Float) -> Unit,
    showLineNumbers: Boolean,
    onShowLineNumbersChange: (Boolean) -> Unit,
    showSuggestions: Boolean,
    onShowSuggestionsChange: (Boolean) -> Unit,
    showSymbolsShortcuts: Boolean,
    onShowSymbolsShortcutsChange: (Boolean) -> Unit,
    showNumbersShortcuts: Boolean,
    onShowNumbersShortcutsChange: (Boolean) -> Unit,
    onBack: () -> Unit
) {
    var showRegionDialog by remember { mutableStateOf(false) }
    var sliderValue by remember(precision) { mutableStateOf(if (precision == Constants.PRECISION_OFF) Constants.DEFAULT_PRECISION.toFloat() else precision.toFloat()) }
    var editorFontSizeSlider by remember(editorFontSize) { mutableStateOf(editorFontSize) }
    val availableRegions = remember { RegionUtils.getAvailableRegions() }

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = { Text("Calculator") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, "Back")
                    }
                },
                colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
                    containerColor = MaterialTheme.colorScheme.background
                )
            )
        },
        containerColor = MaterialTheme.colorScheme.background
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .verticalScroll(rememberScrollState())
        ) {
            SettingsSection(title = "Math logic")

            SettingsToggleItem(
                icon = Icons.Default.Info,
                title = "Result precision",
                subtitle = "Limit decimal places in results. Turn off to show full precision.",
                checked = precision != Constants.PRECISION_OFF,
                onCheckedChange = { enabled ->
                    if (enabled) {
                        sliderValue = Constants.DEFAULT_PRECISION.toFloat()
                        onPrecisionChange(Constants.DEFAULT_PRECISION)
                    } else {
                        onPrecisionChange(Constants.PRECISION_OFF)
                    }
                }
            )

            if (precision != Constants.PRECISION_OFF) {
                SettingsSliderItem(
                    icon = Icons.Default.Info,
                    title = "Decimal places",
                    value = sliderValue,
                    onValueChange = { sliderValue = it },
                    onValueChangeFinished = { onPrecisionChange(sliderValue.roundToInt()) },
                    valueRange = Constants.MIN_PRECISION.toFloat()..Constants.MAX_PRECISION.toFloat(),
                    steps = Constants.MAX_PRECISION - Constants.MIN_PRECISION - 1,
                    valueFormatter = { "${it.roundToInt()} decimal places" },
                    modifier = Modifier.padding(start = 24.dp)
                )
            }

            SettingsToggleItem(
                icon = Icons.Default.BorderHorizontal,
                title = "Rational mode",
                subtitle = "Display results as a ratio of two integers (fractions) for exactness when possible.",
                checked = rationalMode,
                onCheckedChange = onRationalModeChange
            )

            SettingsDropdownItem(
                icon = Icons.Default.Public,
                title = "Region",
                value = if (regionCode == RegionUtils.SYSTEM_DEFAULT) {
                    val systemCountryName = availableRegions
                        .find { it.first == java.util.Locale.getDefault().country }?.second
                        ?: java.util.Locale.getDefault().displayCountry
                    if (systemCountryName.isNotEmpty()) "System default ($systemCountryName)" else "System default"
                } else
                    availableRegions.find { it.first == regionCode }?.second
                        ?: regionCode,
                onClick = { showRegionDialog = true }
            )

            SettingsToggleItem(
                icon = Icons.Default.SpaceBar,
                title = "Show grouping separators",
                subtitle = null,
                checked = groupingSeparatorEnabled,
                onCheckedChange = onGroupingSeparatorEnabledChange,
                modifier = Modifier.padding(start = 24.dp)
            )

            Text(
                text = remember(regionCode, groupingSeparatorEnabled, precision) {
                    MathEngine.formatDisplayResult(
                        "12345678.90",
                        precision,
                        regionCode = regionCode,
                        groupingSeparatorEnabled = groupingSeparatorEnabled
                    )
                },
                style = MaterialTheme.typography.labelLarge.copy(fontFamily = FiraCodeFamily),
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(horizontal = 80.dp).padding(bottom = 16.dp)
            )

            SettingsToggleItem(
                icon = Icons.Default.MoreHoriz,
                title = "Truncate with ellipsis",
                subtitle = "Show ellipsis (e.g., 0.123…) for results with more decimal places than the current precision setting.",
                checked = showPrecisionEllipsis,
                onCheckedChange = onShowPrecisionEllipsisChange
            )

            val mutedEllipsisColor = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f)
            Text(
                text = remember(showPrecisionEllipsis, precision, mutedEllipsisColor) {
                    val base = "0.12345678901"
                    val formatted = MathEngine.formatDisplayResult(
                        base,
                        precision,
                        showEllipsis = showPrecisionEllipsis
                    )
                    buildAnnotatedString {
                        if (showPrecisionEllipsis && formatted.endsWith("…")) {
                            append(formatted.dropLast(1))
                            withStyle(SpanStyle(color = mutedEllipsisColor, fontWeight = FontWeight.Normal)) {
                                append("…")
                            }
                        } else {
                            append(formatted)
                        }
                    }
                },
                style = MaterialTheme.typography.labelLarge.copy(fontFamily = FiraCodeFamily),
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(horizontal = 56.dp).padding(bottom = 16.dp)
            )

            SettingsSection(title = "Editor")

            SettingsSliderItem(
                icon = Icons.Default.Info,
                title = "Font size",
                value = editorFontSizeSlider,
                onValueChange = { editorFontSizeSlider = it },
                onValueChangeFinished = { onEditorFontSizeChange(editorFontSizeSlider) },
                valueRange = Constants.MIN_EDITOR_FONT_SIZE..Constants.MAX_EDITOR_FONT_SIZE,
                steps = (Constants.MAX_EDITOR_FONT_SIZE - Constants.MIN_EDITOR_FONT_SIZE - 1).roundToInt(),
                valueFormatter = { it.roundToInt().toString() }
            )

            SettingsToggleItem(
                icon = Icons.Default.FormatListNumbered,
                title = "Show line numbers",
                subtitle = "You can also toggle this temporarily for each file from the calculator menu.",
                checked = showLineNumbers,
                onCheckedChange = onShowLineNumbersChange
            )

            SettingsToggleItem(
                icon = Icons.Default.Chat,
                title = "Show suggestions",
                subtitle = "You can also toggle this temporarily for each file from the calculator menu.",
                checked = showSuggestions,
                onCheckedChange = onShowSuggestionsChange
            )

            SettingsToggleItem(
                icon = Icons.Default.Calculate,
                title = "Show symbols shortcuts",
                subtitle = "You can also toggle this temporarily for each file from the calculator menu.",
                checked = showSymbolsShortcuts,
                onCheckedChange = onShowSymbolsShortcutsChange
            )

            SettingsToggleItem(
                icon = Icons.Default.Pin,
                title = "Show numbers shortcuts (Numpad)",
                subtitle = "You can also toggle this temporarily for each file from the calculator menu.",
                checked = showNumbersShortcuts,
                onCheckedChange = onShowNumbersShortcutsChange
            )
        }
    }

    RegionSelectorDialog(
        visible = showRegionDialog,
        currentRegionCode = regionCode,
        onSelect = {
            onRegionCodeChange(it)
            showRegionDialog = false
        },
        onDismiss = { showRegionDialog = false }
    )
}
