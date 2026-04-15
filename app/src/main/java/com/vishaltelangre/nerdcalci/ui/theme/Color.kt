package com.vishaltelangre.nerdcalci.ui.theme

import androidx.compose.ui.graphics.Color

// Palette: Midnight Glow (Default)
val MidPrimaryDark = Color(0xFFD0BCFF)
val MidSecondaryDark = Color(0xFFCCC2DC)
val MidTertiaryDark = Color(0xFFEFB8C8)
val MidPrimaryLight = Color(0xFF6750A4)
val MidSecondaryLight = Color(0xFF625B71)
val MidTertiaryLight = Color(0xFF7D5260)
val MidPrimaryContainerDark = Color(0xFF4F378B)
val MidPrimaryContainerLight = Color(0xFFEADDFF)

// Palette: Solar Flare
val SolarPrimaryDark = Color(0xFFFFB74D)
val SolarSecondaryDark = Color(0xFFFF9800)
val SolarTertiaryDark = Color(0xFFFB8C00)
val SolarPrimaryLight = Color(0xFFE65100)
val SolarSecondaryLight = Color(0xFFF57C00)
val SolarTertiaryLight = Color(0xFFFFB300)
val SolarPrimaryContainerDark = Color(0xFF632B00)
val SolarPrimaryContainerLight = Color(0xFFFFDDB3)

// Palette: Arctic Frost
val ArcticPrimaryDark = Color(0xFF80DEEA)
val ArcticSecondaryDark = Color(0xFF26C6DA)
val ArcticTertiaryDark = Color(0xFF00BCD4)
val ArcticPrimaryLight = Color(0xFF006064)
val ArcticSecondaryLight = Color(0xFF00838F)
val ArcticTertiaryLight = Color(0xFF0097A7)
val ArcticPrimaryContainerDark = Color(0xFF004D40)
val ArcticPrimaryContainerLight = Color(0xFFB2EBF2)

// Palette: Nature's Breath
val NaturePrimaryDark = Color(0xFF81C784)
val NatureSecondaryDark = Color(0xFF43A047)
val NatureTertiaryDark = Color(0xFF2E7D32)
val NaturePrimaryLight = Color(0xFF1B5E20)
val NatureSecondaryLight = Color(0xFF2E7D32)
val NatureTertiaryLight = Color(0xFF388E3C)
val NaturePrimaryContainerDark = Color(0xFF003300)
val NaturePrimaryContainerLight = Color(0xFFC8E6C9)

// Palette: Royal Velvet
val RoyalPrimaryDark = Color(0xFFE57373)
val RoyalSecondaryDark = Color(0xFFD32F2F)
val RoyalTertiaryDark = Color(0xFFB71C1C)
val RoyalPrimaryLight = Color(0xFF880E4F)
val RoyalSecondaryLight = Color(0xFFAD1457)
val RoyalTertiaryLight = Color(0xFFC2185B)
val RoyalPrimaryContainerDark = Color(0xFF4A0000)
val RoyalPrimaryContainerLight = Color(0xFFFFCDD2)

// Calculator result color
val ResultSuccess = Color(0xFF00C853)
val ResultError = Color(0xFFF44336)

object SyntaxColors {
  // Dark mode
  val NumberColorDark = Color(0xFFFFD54F) // Bright yellow for numbers
  val VariableColorDark = Color(0xFF64FFDA) // Bright cyan for variables
  val KeywordColorDark = Color(0xFFFF80AB) // Pink for keywords (e.g. sum, total)
  val FunctionColorDark = Color(0xFFFFB74D) // Bright orange for functions
  val OperatorColorDark = Color.White // White for operators (=, +, -, etc)
  val PercentColorDark = Color(0xFFFFAB40) // Bright amber for percentages
  val CommentColorDark = Color(0xFF607D8B) // Dimmer blue-gray for comments
  val ConversionColorDark = Color(0xFF82B1FF) // Bright blue for to/in/as

  // Light mode
  val NumberColorLight = Color(0xFF09885A) // Bright green for numbers
  val VariableColorLight = Color(0xFF001080) // Bright blue for variables
  val KeywordColorLight = Color(0xFFAF00DB) // Purple for keywords
  val FunctionColorLight = Color(0xFFD84315) // Deep burnt orange for functions
  val OperatorColorLight = Color(0xFF000000) // Black for operators (=, +, -, etc)
  val PercentColorLight = Color(0xFFA31515) // Bright red for percentages
  val CommentColorLight = Color(0xFF5A7A5A) // Dimmer but readable green
  val ConversionColorLight = Color(0xFF2979FF) // Deep vibrant blue for to/in/as
}
