package com.vishaltelangre.nerdcalci.ui.theme

import androidx.compose.ui.graphics.Color

// Dark mode
val PrimaryDark = Color(0xFFD0BCFF)
val SecondaryDark = Color(0xFFCCC2DC)
val TertiaryDark = Color(0xFFEFB8C8)

// Light mode
val PrimaryLight = Color(0xFF6650a4)
val SecondaryLight = Color(0xFF625b71)
val TertiaryLight = Color(0xFF7D5260)

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
