package com.vishaltelangre.nerdcalci.ui.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.vishaltelangre.nerdcalci.core.MathEngine
import com.vishaltelangre.nerdcalci.utils.RegionUtils
import com.vishaltelangre.nerdcalci.ui.theme.FiraCodeFamily

@Composable
fun RegionSelectorDialog(
    visible: Boolean,
    currentRegionCode: String,
    onSelect: (String) -> Unit,
    onDismiss: () -> Unit
) {
    if (!visible) return

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Select region") },
        text = {
            Column(modifier = Modifier.heightIn(max = 400.dp)) {
                val availableRegions = RegionUtils.getAvailableRegions()
                val systemLocaleCountry = java.util.Locale.getDefault().country
                val quickOptionCodes = listOfNotNull(
                    systemLocaleCountry.takeIf { it.isNotEmpty() },
                    "US".takeIf { systemLocaleCountry != "US" }
                )
                val quickOptions = quickOptionCodes.mapNotNull { code ->
                    availableRegions.find { it.first == code }
                }
                val systemCountryName = availableRegions.find { it.first == systemLocaleCountry }?.second
                    ?: java.util.Locale.getDefault().displayCountry
                val systemDefaultName = if (systemCountryName.isNotEmpty()) {
                    "System default ($systemCountryName)"
                } else {
                    "System default"
                }

                LazyColumn(
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    item {
                        RegionOption(
                            name = systemDefaultName,
                            code = RegionUtils.SYSTEM_DEFAULT,
                            isSelected = currentRegionCode == RegionUtils.SYSTEM_DEFAULT,
                            onClick = { onSelect(RegionUtils.SYSTEM_DEFAULT) }
                        )
                    }
                    if (quickOptions.isNotEmpty()) {
                        items(quickOptions) { (code, name) ->
                            RegionOption(
                                name = name,
                                code = code,
                                isSelected = currentRegionCode == code,
                                onClick = { onSelect(code) }
                            )
                        }
                    }
                    items(availableRegions) { (code, name) ->
                        RegionOption(
                            name = name,
                            code = code,
                            isSelected = currentRegionCode == code,
                            onClick = { onSelect(code) }
                        )
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
private fun RegionOption(
    name: String,
    code: String,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    Card(
        onClick = onClick,
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (isSelected)
                MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.5f)
            else
                MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f)
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = name,
                    style = MaterialTheme.typography.bodyLarge,
                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                    color = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface
                )
            }
            if (isSelected) {
                Spacer(modifier = Modifier.width(8.dp))
                Icon(
                    imageVector = Icons.Default.Check,
                    contentDescription = "Selected",
                    tint = MaterialTheme.colorScheme.primary
                )
            }
        }
    }
}
