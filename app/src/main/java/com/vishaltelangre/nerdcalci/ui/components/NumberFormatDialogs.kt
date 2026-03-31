package com.vishaltelangre.nerdcalci.ui.components

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.unit.dp
import com.vishaltelangre.nerdcalci.core.NumberDecimalPreset
import com.vishaltelangre.nerdcalci.core.NumberFormatSettings
import com.vishaltelangre.nerdcalci.core.NumberSeparatorPreset
import com.vishaltelangre.nerdcalci.ui.theme.FiraCodeFamily

@Composable
fun NumberSeparatorDialog(
    visible: Boolean,
    locale: java.util.Locale,
    settings: NumberFormatSettings,
    onSelect: (NumberSeparatorPreset) -> Unit,
    onDismiss: () -> Unit
) {
    if (!visible) return
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Separator") },
        text = {
            Column {
                NumberSeparatorPreset.entries.forEach { option ->
                    val enabled = isSeparatorOptionEnabled(locale, option, settings)
                    TextButton(
                        onClick = { onSelect(option) },
                        enabled = enabled,
                        modifier = Modifier
                            .fillMaxWidth()
                            .graphicsLayer(alpha = if (enabled) 1f else 0.38f)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text(
                                    text = numberSeparatorLabel(option),
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = if (settings.separators == option) {
                                        MaterialTheme.colorScheme.primary
                                    } else {
                                        MaterialTheme.colorScheme.onSurface
                                    }
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                separatorSymbolLabel(option, locale)?.let { CodeChip(text = it) }
                            }
                            if (settings.separators == option) {
                                Icon(
                                    imageVector = Icons.Default.Check,
                                    contentDescription = "Selected",
                                    tint = MaterialTheme.colorScheme.primary,
                                    modifier = Modifier.graphicsLayer(alpha = if (enabled) 1f else 0.35f)
                                )
                            }
                        }
                    }
                }
            }
        },
        confirmButton = {
            TextButton(onClick = onDismiss) { Text("Close") }
        }
    )
}

@Composable
fun NumberDecimalDialog(
    visible: Boolean,
    locale: java.util.Locale,
    settings: NumberFormatSettings,
    onSelect: (NumberDecimalPreset) -> Unit,
    onDismiss: () -> Unit
) {
    if (!visible) return
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Decimal") },
        text = {
            Column {
                NumberDecimalPreset.entries.forEach { option ->
                    val enabled = isDecimalOptionEnabled(locale, option, settings)
                    TextButton(
                        onClick = { onSelect(option) },
                        enabled = enabled,
                        modifier = Modifier
                            .fillMaxWidth()
                            .graphicsLayer(alpha = if (enabled) 1f else 0.38f)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text(
                                    text = numberDecimalLabel(option),
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = if (settings.decimal == option) {
                                        MaterialTheme.colorScheme.primary
                                    } else {
                                        MaterialTheme.colorScheme.onSurface
                                    }
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                decimalSymbolLabel(option, locale)?.let { CodeChip(text = it) }
                            }
                            if (settings.decimal == option) {
                                Icon(
                                    imageVector = Icons.Default.Check,
                                    contentDescription = "Selected",
                                    tint = MaterialTheme.colorScheme.primary,
                                    modifier = Modifier.graphicsLayer(alpha = if (enabled) 1f else 0.35f)
                                )
                            }
                        }
                    }
                }
            }
        },
        confirmButton = {
            TextButton(onClick = onDismiss) { Text("Close") }
        }
    )
}

private fun numberSeparatorLabel(value: NumberSeparatorPreset): String {
    return when (value) {
        NumberSeparatorPreset.LOCALE -> "System"
        NumberSeparatorPreset.COMMA -> "Comma"
        NumberSeparatorPreset.DOT -> "Dot"
        NumberSeparatorPreset.SPACE -> "Space"
        NumberSeparatorPreset.OFF -> "Off"
    }
}

private fun numberDecimalLabel(value: NumberDecimalPreset): String {
    return when (value) {
        NumberDecimalPreset.LOCALE -> "System"
        NumberDecimalPreset.COMMA -> "Comma"
        NumberDecimalPreset.DOT -> "Dot"
    }
}

private fun isSeparatorOptionEnabled(
    locale: java.util.Locale,
    option: NumberSeparatorPreset,
    settings: NumberFormatSettings
): Boolean {
    if (option == NumberSeparatorPreset.OFF) return true
    return option == settings.separators || separatorChar(locale, option) != decimalChar(locale, settings.decimal)
}

private fun isDecimalOptionEnabled(
    locale: java.util.Locale,
    option: NumberDecimalPreset,
    settings: NumberFormatSettings
): Boolean {
    return option == settings.decimal || decimalChar(locale, option) != separatorChar(locale, settings.separators)
}

private fun separatorChar(
    locale: java.util.Locale,
    option: NumberSeparatorPreset
): Char {
    val symbols = java.text.DecimalFormatSymbols.getInstance(locale)
    return when (option) {
        NumberSeparatorPreset.LOCALE -> symbols.groupingSeparator
        NumberSeparatorPreset.COMMA -> ','
        NumberSeparatorPreset.DOT -> '.'
        NumberSeparatorPreset.SPACE -> ' '
        NumberSeparatorPreset.OFF -> '\u0000'
    }
}

private fun decimalChar(
    locale: java.util.Locale,
    option: NumberDecimalPreset
): Char {
    val symbols = java.text.DecimalFormatSymbols.getInstance(locale)
    return when (option) {
        NumberDecimalPreset.LOCALE -> symbols.decimalSeparator
        NumberDecimalPreset.COMMA -> ','
        NumberDecimalPreset.DOT -> '.'
    }
}

@Composable
private fun CodeChip(text: String) {
    Surface(
        color = MaterialTheme.colorScheme.surfaceVariant,
        contentColor = MaterialTheme.colorScheme.onSurfaceVariant,
        shape = MaterialTheme.shapes.small
    ) {
        Text(
            text = text,
            style = MaterialTheme.typography.labelSmall.copy(fontFamily = com.vishaltelangre.nerdcalci.ui.theme.FiraCodeFamily),
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp)
        )
    }
}

private fun separatorSymbolLabel(option: NumberSeparatorPreset, locale: java.util.Locale): String? {
    return when (option) {
        NumberSeparatorPreset.LOCALE -> visibleSymbol(separatorChar(locale, option))
        NumberSeparatorPreset.COMMA -> ","
        NumberSeparatorPreset.DOT -> "."
        NumberSeparatorPreset.SPACE -> "␣"
        NumberSeparatorPreset.OFF -> null
    }
}

private fun decimalSymbolLabel(option: NumberDecimalPreset, locale: java.util.Locale): String? {
    return when (option) {
        NumberDecimalPreset.LOCALE -> visibleSymbol(decimalChar(locale, option))
        NumberDecimalPreset.COMMA -> ","
        NumberDecimalPreset.DOT -> "."
    }
}

private fun visibleSymbol(symbol: Char): String {
    return if (symbol == ' ' || symbol.isWhitespace()) "␣" else symbol.toString()
}
