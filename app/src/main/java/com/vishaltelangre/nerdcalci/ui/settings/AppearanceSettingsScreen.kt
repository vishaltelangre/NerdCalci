package com.vishaltelangre.nerdcalci.ui.settings

import android.os.Build
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import com.google.android.material.color.DynamicColors
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.DarkMode
import androidx.compose.material.icons.filled.LightMode
import androidx.compose.material.icons.filled.Palette
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.ListItem
import androidx.compose.material3.ListItemDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SegmentedButton
import androidx.compose.material3.SegmentedButtonDefaults
import androidx.compose.material3.SingleChoiceSegmentedButtonRow
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.vishaltelangre.nerdcalci.ui.theme.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AppearanceSettingsScreen(
    currentTheme: String,
    onThemeChange: (String) -> Unit,
    colorPalette: String,
    onColorPaletteChange: (String) -> Unit,
    dynamicColorEnabled: Boolean,
    onDynamicColorEnabledChange: (Boolean) -> Unit,
    onBack: () -> Unit
) {
    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = { Text("Appearance") },
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
            SettingsSection(title = "App theme")

            SingleChoiceSegmentedButtonRow(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 8.dp)
            ) {
                val options = listOf(
                    Triple("light", "Light", Icons.Default.LightMode),
                    Triple("dark", "Dark", Icons.Default.DarkMode),
                    Triple("system", "System", Icons.Default.DarkMode)
                )

                options.forEachIndexed { index, (value, label, icon) ->
                    SegmentedButton(
                        selected = currentTheme == value,
                        onClick = { onThemeChange(value) },
                        shape = SegmentedButtonDefaults.itemShape(
                            index = index,
                            count = options.size
                        ),
                        icon = {
                            SegmentedButtonDefaults.Icon(active = currentTheme == value) {
                                Icon(
                                    imageVector = icon,
                                    contentDescription = label,
                                    modifier = Modifier.size(24.dp)
                                )
                            }
                        }
                    ) {
                        Text(
                            text = label,
                            style = MaterialTheme.typography.labelLarge
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            val context = LocalContext.current
            val isDarkTheme = when (currentTheme) {
                "light" -> false
                "dark" -> true
                else -> isSystemInDarkTheme()
            }

            val currentColorScheme = MaterialTheme.colorScheme
            val configuration = androidx.compose.ui.platform.LocalConfiguration.current
            val palettes = remember(isDarkTheme, dynamicColorEnabled, configuration) {
                buildList {
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S && DynamicColors.isDynamicColorAvailable()) {
                        add(
                            PaletteInfo(
                                id = "dynamic",
                                name = "Dynamic color",
                                primary = if (dynamicColorEnabled) currentColorScheme.primary else (if (isDarkTheme) dynamicDarkColorScheme(context) else dynamicLightColorScheme(context)).primary,
                                secondary = if (dynamicColorEnabled) currentColorScheme.secondary else (if (isDarkTheme) dynamicDarkColorScheme(context) else dynamicLightColorScheme(context)).secondary,
                                tertiary = if (dynamicColorEnabled) currentColorScheme.tertiary else (if (isDarkTheme) dynamicDarkColorScheme(context) else dynamicLightColorScheme(context)).tertiary,
                                description = "Use colors from your wallpaper"
                            )
                        )
                    }
                    add(PaletteInfo("midnight", "Midnight Glow", MidPrimaryLight, MidSecondaryLight, MidTertiaryLight))
                    add(PaletteInfo("solar", "Solar Flare", SolarPrimaryLight, SolarSecondaryLight, SolarTertiaryLight))
                    add(PaletteInfo("arctic", "Arctic Frost", ArcticPrimaryLight, ArcticSecondaryLight, ArcticTertiaryLight))
                    add(PaletteInfo("nature", "Nature's Breath", NaturePrimaryLight, NatureSecondaryLight, NatureTertiaryLight))
                    add(PaletteInfo("royal", "Royal Velvet", RoyalPrimaryLight, RoyalSecondaryLight, RoyalTertiaryLight))
                }
            }

            SettingsSection(title = "Color palettes")

            Column(
                modifier = Modifier.padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                palettes.forEach { palette ->
                    val isSelected = if (palette.id == "dynamic") {
                        dynamicColorEnabled
                    } else {
                        !dynamicColorEnabled && colorPalette == palette.id
                    }

                    PaletteItem(
                        info = palette,
                        isSelected = isSelected,
                        onClick = {
                            if (palette.id == "dynamic") {
                                onDynamicColorEnabledChange(true)
                            } else {
                                onDynamicColorEnabledChange(false)
                                onColorPaletteChange(palette.id)
                            }
                        }
                    )
                }
            }

            Spacer(modifier = Modifier.height(24.dp))
        }
    }
}

data class PaletteInfo(
    val id: String,
    val name: String,
    val primary: Color,
    val secondary: Color,
    val tertiary: Color,
    val description: String? = null
)

@Composable
fun PaletteItem(
    info: PaletteInfo,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    Card(
        onClick = onClick,
        modifier = Modifier
            .fillMaxWidth()
            .defaultMinSize(minHeight = if (info.description != null) 80.dp else 72.dp),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (isSelected) {
                MaterialTheme.colorScheme.primaryContainer
            } else {
                MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
            }
        ),
        border = if (isSelected) {
            BorderStroke(width = 2.dp, color = MaterialTheme.colorScheme.primary)
        } else null
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 20.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Palette Preview Circles
            Box(contentAlignment = Alignment.Center) {
                Row(horizontalArrangement = Arrangement.spacedBy((-8).dp)) {
                    Box(modifier = Modifier.size(32.dp).clip(CircleShape).background(info.primary).border(1.dp, Color.White.copy(alpha = 0.2f), CircleShape))
                    Box(modifier = Modifier.size(32.dp).clip(CircleShape).background(info.secondary).border(1.dp, Color.White.copy(alpha = 0.2f), CircleShape))
                    Box(modifier = Modifier.size(32.dp).clip(CircleShape).background(info.tertiary).border(1.dp, Color.White.copy(alpha = 0.2f), CircleShape))
                }
            }

            Spacer(modifier = Modifier.width(16.dp))

            Column(modifier = Modifier.weight(1.0f)) {
                Text(
                    text = info.name,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = if (isSelected) MaterialTheme.colorScheme.onPrimaryContainer else MaterialTheme.colorScheme.onSurface
                )
                if (info.description != null) {
                    Text(
                        text = info.description,
                        style = MaterialTheme.typography.bodySmall,
                        color = if (isSelected) MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.7f) else MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            if (isSelected) {
                Icon(
                    imageVector = Icons.Default.CheckCircle,
                    contentDescription = "Selected",
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(24.dp)
                )
            }
        }
    }
}
