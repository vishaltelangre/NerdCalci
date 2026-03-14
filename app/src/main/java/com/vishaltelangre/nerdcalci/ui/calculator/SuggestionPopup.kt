package com.vishaltelangre.nerdcalci.ui.calculator

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shadow
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.*
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Popup
import androidx.compose.ui.window.PopupProperties
import com.vishaltelangre.nerdcalci.ui.theme.FiraCodeFamily
import com.vishaltelangre.nerdcalci.utils.findClosingParenthesis
import com.vishaltelangre.nerdcalci.utils.getIdentifierRangeAt
import android.content.res.Configuration

/**
 * Constants used for sizing and positioning the suggestion popup.
 */
private object SuggestionPopupConstants {
    // Maximum width as a fraction of the screen width
    const val MaxWidthPortraitFraction = 0.75f
    const val MaxWidthLandscapeFraction = 0.35f

    // Limits for popup dimensions
    val MinStableWidth = 100.dp
    val MaxPopupHeight = 125.dp
    val EstimatedItemHeight = 44.dp

    // Buffers and offsets
    val SafetyWidthBuffer = 8.dp
    val LabelWidth = 44.dp
    val BadgeHeight = 24.dp
    val StaticElementsWidth = LabelWidth + 26.dp // Label (44dp) + Row padding (20dp) + Spacer (6dp)
    val VerticalGap = 4.dp

    // Thresholds for deciding if popup should be shown above or below the cursor
    val SpaceBelowThreshold = 150.dp
    val CurrentLineTopThreshold = 200.dp
    val TopOffsetMargin = 60.dp
}

/**
 * A popup that displays autocomplete suggestions for variables and functions.
 * Calculates position relative to the cursor.
 */
@Composable
fun SuggestionPopup(
    suggestions: List<Suggestion>,
    isFocused: Boolean,
    forceDismissSuggestions: Boolean,
    onDismissSuggestions: () -> Unit,
    textFieldValue: TextFieldValue,
    onTextFieldValueChange: (TextFieldValue) -> Unit,
    onValueChange: (String) -> Unit,
    textLayoutResult: TextLayoutResult?,
    boxPosition: Offset,
    keywordColor: Color,
    functionColor: Color,
    variableColor: Color
) {
    // Only show if there are suggestions, field is focused, and it hasn't been manually dismissed.
    if (suggestions.isEmpty() || !isFocused || forceDismissSuggestions) return

    val configuration = LocalConfiguration.current
    val density = LocalDensity.current
    val isPortrait = configuration.orientation == Configuration.ORIENTATION_PORTRAIT

    // Calculate the maximum allowed width based on orientation to prevent the popup
    // from covering too much of the editor.
    val maxWidthFraction = if (isPortrait)
        SuggestionPopupConstants.MaxWidthPortraitFraction
    else
        SuggestionPopupConstants.MaxWidthLandscapeFraction

    val screenWidth = configuration.screenWidthDp.dp
    val screenHeight = configuration.screenHeightDp.dp
    val maxPopupWidth = screenWidth * maxWidthFraction

    val textMeasurer = rememberTextMeasurer()
    val suggestionTextStyle = MaterialTheme.typography.bodyMedium.copy(
        fontFamily = FiraCodeFamily
    )

    // Calculate the width of the longest suggestion to ensure the popup is wide enough.
    // We do this up front so the popup width doesn't "jump" when scrolling through results.
    val widestSuggestionWidth = remember(suggestions, density) {
        val maxTextWidthPx = suggestions.maxOfOrNull { suggestion ->
            val isItalic = isItalicType(suggestion.type)

            // Build the same AnnotatedString used in the UI to get an accurate measurement.
            val annotatedString = buildSuggestionText(suggestion)

            textMeasurer.measure(
                text = annotatedString,
                style = suggestionTextStyle.copy(
                    fontStyle = if (isItalic) FontStyle.Italic else FontStyle.Normal
                ),
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            ).size.width
        } ?: 0

        with(density) { maxTextWidthPx.toDp() } +
                SuggestionPopupConstants.StaticElementsWidth +
                SuggestionPopupConstants.SafetyWidthBuffer
    }

    // The stable width is determined by the content but capped by screen constraints.
    val stableWidth = minOf(maxPopupWidth, maxOf(SuggestionPopupConstants.MinStableWidth, widestSuggestionWidth))

    // Estimate total height to avoid a large empty gap when the list is small.
    val estimatedContentHeight = (suggestions.size * SuggestionPopupConstants.EstimatedItemHeight.value).dp
    val popupHeight = minOf(SuggestionPopupConstants.MaxPopupHeight, estimatedContentHeight)

    // Determine where the cursor is currently located on the screen to anchor the popup.
    val cursorIndex = textFieldValue.selection.start

    // Handle race condition where layout result is behind text field value.
    val cursorRect = if (textLayoutResult != null && cursorIndex <= textLayoutResult.layoutInput.text.length) {
        textLayoutResult.getCursorRect(cursorIndex)
    } else {
        null
    }
    // Only show the popup once we have a valid cursor measurement.
    if (cursorRect == null) return

    val cursorLeft = cursorRect.left
    val cursorTop = cursorRect.top
    val cursorBottom = cursorRect.bottom

    // Screen positions in Dp for boundary checking
    val boxTopDp = with(density) { boxPosition.y.toDp() }
    val currentLineTopDp = with(density) { (boxPosition.y + cursorTop).toDp() }
    val currentLineBottomDp = with(density) { (boxPosition.y + cursorBottom).toDp() }
    val imeHeightDp = with(density) { WindowInsets.ime.getBottom(density).toDp() }

    // Decide whether to show the popup above or below the line based on available space.
    val spaceBelow = screenHeight - currentLineBottomDp - imeHeightDp
    val showAbove = spaceBelow < SuggestionPopupConstants.SpaceBelowThreshold &&
                    currentLineTopDp > SuggestionPopupConstants.CurrentLineTopThreshold

    val gapPx = with(density) { SuggestionPopupConstants.VerticalGap.roundToPx() }

    // Ensure the popup doesn't bleed off the right edge of the screen.
    val xOffset = if (with(density) { (cursorLeft.toDp() + stableWidth) } > screenWidth) {
        with(density) { (screenWidth - stableWidth).roundToPx() }
    } else {
        cursorLeft.toInt()
    }

    Popup(
        alignment = Alignment.TopStart,
        offset = if (showAbove) {
            IntOffset(x = xOffset, y = cursorTop.toInt() - with(density) { popupHeight.roundToPx() + gapPx })
        } else {
            IntOffset(x = xOffset, y = cursorBottom.toInt() + gapPx)
        },
        onDismissRequest = onDismissSuggestions,
        properties = PopupProperties(
            focusable = false,
            dismissOnClickOutside = false,
            dismissOnBackPress = true
        )
    ) {
        Surface(
            modifier = Modifier
                .width(stableWidth)
                .heightIn(max = if (showAbove)
                    minOf(SuggestionPopupConstants.MaxPopupHeight, boxTopDp - SuggestionPopupConstants.TopOffsetMargin)
                else
                    SuggestionPopupConstants.MaxPopupHeight
                ),
            color = MaterialTheme.colorScheme.surface,
            shape = RoundedCornerShape(12.dp),
            shadowElevation = 12.dp,
            border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.8f))
        ) {
            LazyColumn(modifier = Modifier.fillMaxWidth()) {
                items(suggestions) { suggestion ->
                    SuggestionItem(
                        suggestion = suggestion,
                        keywordColor = keywordColor,
                        functionColor = functionColor,
                        variableColor = variableColor,
                        onClick = {
                            handleSuggestionClick(
                                suggestion = suggestion,
                                textFieldValue = textFieldValue,
                                onTextFieldValueChange = onTextFieldValueChange,
                                onValueChange = onValueChange
                            )
                        }
                    )
                }
            }
        }
    }
}

@Composable
private fun SuggestionItem(
    suggestion: Suggestion,
    keywordColor: Color,
    functionColor: Color,
    variableColor: Color,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(start = 8.dp, end = 12.dp, top = 4.dp, bottom = 4.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        val typeLabel = when (suggestion.type) {
            SuggestionType.LOCAL_FUNCTION -> "FUNC"
            SuggestionType.GLOBAL_FUNCTION -> "GLOBAL\nFUNC"
            SuggestionType.DYNAMIC_VARIABLE -> "DYNAMIC\nVAR"
            SuggestionType.CONSTANT -> "CONST"
            SuggestionType.VARIABLE -> "VAR"
        }

        val (itemColor, isItalic) = when (suggestion.type) {
            SuggestionType.DYNAMIC_VARIABLE -> keywordColor to true
            SuggestionType.LOCAL_FUNCTION, SuggestionType.GLOBAL_FUNCTION -> functionColor to true
            SuggestionType.VARIABLE, SuggestionType.CONSTANT -> variableColor to true
        }

        val isMultiline = typeLabel.contains("\n")
        val baseFontSize = MaterialTheme.typography.labelSmall.fontSize

        // Labels with multiple lines (e.g., GLOBAL FUNC) use smaller text and tighter leading.
        val currentFontSize = if (isMultiline) baseFontSize * 0.62f else baseFontSize * 0.82f
        val currentLineHeight = if (isMultiline) baseFontSize * 0.75f else baseFontSize * 1.0f

        // The badge identifies the kind of suggestion (Variable, Function, etc.)
        Surface(
            modifier = Modifier
                .width(SuggestionPopupConstants.LabelWidth)
                .height(SuggestionPopupConstants.BadgeHeight),
            shape = RoundedCornerShape(4.dp),
            color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.05f),
            border = BorderStroke(0.5.dp, MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.08f))
        ) {
            Box(contentAlignment = Alignment.Center) {
                Text(
                    text = typeLabel,
                    style = MaterialTheme.typography.labelSmall.copy(
                        fontFamily = FiraCodeFamily,
                        fontSize = currentFontSize,
                        lineHeight = currentLineHeight,
                        textAlign = TextAlign.Center,
                        shadow = Shadow(
                            color = Color.White.copy(alpha = 0.22f),
                            offset = Offset(0f, 1f),
                            blurRadius = 0f
                        )
                    ),
                    color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.32f),
                    modifier = Modifier.padding(horizontal = 1.dp)
                )
            }
        }

        Spacer(modifier = Modifier.width(6.dp))

        // Text content with fuzzy-match bolding applied to matched segments.
        Text(
            text = buildSuggestionText(suggestion),
            style = MaterialTheme.typography.bodyMedium.copy(
                fontFamily = FiraCodeFamily,
                fontStyle = if (isItalic) FontStyle.Italic else FontStyle.Normal
            ),
            color = itemColor,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )
    }
}

/**
 * Constructs an AnnotatedString where characters that matched the user's input are bolded.
 */
private fun buildSuggestionText(suggestion: Suggestion): AnnotatedString {
    return buildAnnotatedString {
        val name = suggestion.name
        val matchedIndices = suggestion.matchIndices.toSet()
        for (i in name.indices) {
            if (i in matchedIndices) {
                // Characters matching the user's current word are bolded for feedback.
                withStyle(SpanStyle(fontWeight = FontWeight.ExtraBold)) {
                    append(name[i])
                }
            } else {
                append(name[i])
            }
        }

        // Functions are shown with empty parentheses in the list to visually distinguish them.
        if (suggestion.type == SuggestionType.LOCAL_FUNCTION ||
            suggestion.type == SuggestionType.GLOBAL_FUNCTION) {
            append("()")
        }
    }
}

private fun isItalicType(type: SuggestionType): Boolean {
    return when (type) {
        SuggestionType.DYNAMIC_VARIABLE,
        SuggestionType.LOCAL_FUNCTION,
        SuggestionType.GLOBAL_FUNCTION,
        SuggestionType.VARIABLE,
        SuggestionType.CONSTANT -> true
    }
}

/**
 * Handles the logic for inserting the suggestion into the text field.
 * Manages word boundaries, syntax-aware completion, and cursor placement.
 */
private fun handleSuggestionClick(
    suggestion: Suggestion,
    textFieldValue: TextFieldValue,
    onTextFieldValueChange: (TextFieldValue) -> Unit,
    onValueChange: (String) -> Unit
) {
    val text = textFieldValue.text
    val cursorPos = textFieldValue.selection.start

    // Find the full word currently under or just before the cursor.
    val range = text.getIdentifierRangeAt(if (cursorPos > 0) cursorPos - 1 else 0)
    val wordStart = range.first
    var wordEnd = range.last + 1

    val hasParens = wordEnd < text.length && text[wordEnd] == '('
    val isFunctionSuggestion = suggestion.type == SuggestionType.LOCAL_FUNCTION ||
                                 suggestion.type == SuggestionType.GLOBAL_FUNCTION

    // If replacing a function call with a simple variable, remove existing parens.
    if (!isFunctionSuggestion && hasParens) {
        wordEnd = text.findClosingParenthesis(wordEnd) + 1
    }

    // Determine the exact string to insert.
    val replacementText = if (isFunctionSuggestion) {
        // Only append parens if they aren't already there.
        if (hasParens) suggestion.name else "${suggestion.name}()"
    } else {
        suggestion.name
    }

    val newText = text.substring(0, wordStart) + replacementText + text.substring(wordEnd)

    // Cursor placement rules:
    // 1. Inside empty parens if we just added a function.
    // 2. Otherwise at the end of the newly inserted word.
    val newCursorPos = if (isFunctionSuggestion && !hasParens) {
        wordStart + replacementText.length - 1
    } else {
        wordStart + replacementText.length
    }

    onTextFieldValueChange(
        textFieldValue.copy(
            text = newText,
            selection = TextRange(newCursorPos)
        )
    )
    onValueChange(newText.trim())
}
