package com.vishaltelangre.nerdcalci.ui.calculator

import com.vishaltelangre.nerdcalci.core.Builtins
import com.vishaltelangre.nerdcalci.core.MathEngine
import com.vishaltelangre.nerdcalci.utils.RegionUtils
import com.vishaltelangre.nerdcalci.core.UnitCategory
import com.vishaltelangre.nerdcalci.core.UnitConverter
import com.vishaltelangre.nerdcalci.core.Lexer
import com.vishaltelangre.nerdcalci.core.TokenKind
import androidx.compose.foundation.background
import androidx.activity.compose.BackHandler
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.ime
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.layout.wrapContentWidth
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.Redo
import androidx.compose.material.icons.automirrored.filled.Undo
import androidx.compose.material.icons.automirrored.filled.HelpOutline
import androidx.compose.material.icons.filled.CleaningServices
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.FileCopy
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Chat
import androidx.compose.material.icons.filled.ChatBubbleOutline
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Calculate
import androidx.compose.material.icons.filled.Pin
import androidx.compose.material.icons.filled.FormatListNumbered
import androidx.compose.material.icons.filled.Functions
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.LockOpen
import androidx.compose.material.icons.filled.BorderHorizontal
import androidx.compose.material.icons.filled.SpaceBar
import androidx.compose.material.icons.outlined.Functions
import androidx.compose.material.icons.filled.Image
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.PictureAsPdf
import androidx.compose.material.icons.filled.ViewHeadline
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.VerticalDivider
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.border
import android.widget.Toast
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.platform.LocalDensity
import androidx.navigation.NavHostController
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.input.key.Key
import androidx.compose.ui.input.key.KeyEventType
import androidx.compose.ui.input.key.key
import androidx.compose.ui.input.key.onKeyEvent
import androidx.compose.ui.input.key.type
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.TextRange
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.window.Popup
import androidx.compose.ui.window.PopupProperties
import androidx.compose.ui.unit.IntOffset
import androidx.compose.foundation.BorderStroke
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.layout.positionInWindow
import androidx.compose.ui.text.rememberTextMeasurer
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.text.input.OffsetMapping
import androidx.compose.ui.text.input.TransformedText
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.draw.drawWithContent
import androidx.compose.material.icons.outlined.Info
import androidx.compose.ui.graphics.drawscope.Stroke
import com.vishaltelangre.nerdcalci.core.Constants
import com.vishaltelangre.nerdcalci.data.local.entities.LineEntity
import com.vishaltelangre.nerdcalci.data.local.entities.FileEntity
import com.vishaltelangre.nerdcalci.ui.components.DeleteFileDialog
import com.vishaltelangre.nerdcalci.ui.components.RenameFileDialog
import com.vishaltelangre.nerdcalci.ui.components.FileInfoDialog
import com.vishaltelangre.nerdcalci.ui.theme.FiraCodeFamily
import com.vishaltelangre.nerdcalci.utils.ExportUtils
import com.vishaltelangre.nerdcalci.utils.SyntaxUtils
import com.vishaltelangre.nerdcalci.utils.getSuggestionContext
import com.vishaltelangre.nerdcalci.utils.TokenType
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.CoroutineScope
import com.vishaltelangre.nerdcalci.utils.calculateFuzzyMatch
import com.vishaltelangre.nerdcalci.utils.Suggestion
import com.vishaltelangre.nerdcalci.utils.SuggestionType
import com.vishaltelangre.nerdcalci.utils.getIdentifierRangeAt
import com.vishaltelangre.nerdcalci.ui.theme.ResultSuccess
import android.content.res.Configuration

import com.vishaltelangre.nerdcalci.ui.theme.SyntaxColors

/**
 * Apply syntax highlighting to calculator expressions.
 *
 * Colors adapt to dark/light theme for optimal readability.
 */
private class SyntaxHighlightingTransformation(
    private val numberColor: Color,
    private val variableColor: Color,
    private val keywordColor: Color,
    private val functionColor: Color,
    private val operatorColor: Color,
    private val percentColor: Color,
    private val commentColor: Color,
    private val conversionColor: Color,
    private val defaultColor: Color,
    private val fileVariables: Map<String, String> = emptyMap(),
    private val isNonFirstLine: Boolean
) : VisualTransformation {
    /**
     * Leading space workaround:
     * We prepend a dummy space (" ") to the displayText of all non-first lines.
     * This space serves as a reliable target for Backspace events on all Android keyboards.
     * Deleting this space triggers a line merge or deletion in [LineRow].
     *
     * Offset mapping:
     * - Original (0..N) maps to Transformed (1..N+1) if hasLeadingSpace
     * - Transformed (1..N+1) maps to Original (0..N) if hasLeadingSpace
     * - Transformed index 0 (the dummy space itself) is clamped to Original index 0
     */
    override fun filter(text: AnnotatedString): TransformedText {
        val hasLeadingSpace = isNonFirstLine && text.text.startsWith(" ")
        val isDummySpace = hasLeadingSpace && text.text.length == 1

        val transformedText = if (hasLeadingSpace) {
            applySyntaxHighlighting(
                text.text.substring(1),
                numberColor,
                variableColor,
                keywordColor,
                functionColor,
                operatorColor,
                percentColor,
                commentColor,
                conversionColor,
                defaultColor,
                fileVariables
            )
        } else {
            applySyntaxHighlighting(
                text.text,
                numberColor,
                variableColor,
                keywordColor,
                functionColor,
                operatorColor,
                percentColor,
                commentColor,
                conversionColor,
                defaultColor,
                fileVariables
            )
        }

        val offsetMapping = object : OffsetMapping {
            override fun originalToTransformed(offset: Int): Int {
                // Force cursor to index 0 visually so it looks like the line is empty.
                if (isDummySpace && offset == 1) return 0

                if (hasLeadingSpace) {
                    return if (offset == 0) 0 else offset - 1
                }
                return offset
            }

            override fun transformedToOriginal(offset: Int): Int {
                // Map visual position 0 back to original index 1 so backspace still hits the space.
                if (isDummySpace && offset == 0) return 1

                if (hasLeadingSpace) {
                    return offset + 1
                }
                return offset
            }
        }

        return TransformedText(transformedText, offsetMapping)
    }
}

/**
 * Apply syntax highlighting to calculator expressions.
 *
 * Colors adapt to dark/light theme for optimal readability.
 */
private fun applySyntaxHighlighting(
    text: String,
    numberColor: Color,
    variableColor: Color,
    keywordColor: Color,
    functionColor: Color,
    operatorColor: Color,
    percentColor: Color,
    commentColor: Color,
    conversionColor: Color,
    defaultColor: Color,
    fileVariables: Map<String, String> = emptyMap()
): AnnotatedString {
    return buildAnnotatedString {
        val tokens = SyntaxUtils.parseSyntaxTokens(text)
        var lastFileVariableNode = false
        var dotSeenAfterFileVar = false

        for (token in tokens) {
            val elementText = text.substring(token.start, token.end)
            val color = when (token.type) {
                TokenType.Number -> numberColor
                TokenType.Variable -> variableColor
                TokenType.Keyword -> keywordColor
                TokenType.Function -> functionColor
                TokenType.Operator -> operatorColor
                TokenType.Percent -> percentColor
                TokenType.Comment -> commentColor
                TokenType.Conversion -> conversionColor
                TokenType.StringLiteral -> keywordColor
                TokenType.Default -> defaultColor
            }

            val isTargetAfterDot = (token.type == TokenType.Variable || token.type == TokenType.Keyword || token.type == TokenType.Function) && dotSeenAfterFileVar

            if (isTargetAfterDot) {
                withStyle(SpanStyle(color = color, fontWeight = FontWeight.Bold, fontStyle = FontStyle.Italic)) {
                    append(elementText)
                }
                // Reset state after consumption to prevent chained leaks.
                dotSeenAfterFileVar = false
                lastFileVariableNode = false
            } else {
                if (token.type == TokenType.Variable || token.type == TokenType.Keyword || token.type == TokenType.Function) {
                    withStyle(SpanStyle(color = color, fontWeight = FontWeight.Bold)) {
                        append(elementText)
                    }
                } else if (token.type == TokenType.Comment) {
                    withStyle(SpanStyle(color = color, fontStyle = FontStyle.Italic)) {
                        append(elementText)
                    }
                } else {
                    withStyle(SpanStyle(color = color)) {
                        append(elementText)
                    }
                }

                if (token.type == TokenType.Variable && fileVariables.containsKey(elementText)) {
                    // Found a variable that holds a file reference
                    lastFileVariableNode = true
                    dotSeenAfterFileVar = false
                } else if (token.type == TokenType.Default && elementText == "." && lastFileVariableNode) {
                    // Found a dot operator directly following a file variable
                    dotSeenAfterFileVar = true
                } else if (token.type != TokenType.Default || elementText.trim().isNotEmpty()) {
                    // Any other non-whitespace token breaks the chain for dot notation
                    lastFileVariableNode = false
                    dotSeenAfterFileVar = false
                }
            }
        }
    }
}

/**
 * Extract suggestions from calculator expressions for autocomplete.
 *
 * Parses both variable assignments (e.g., "price = 100") and user-defined functions
 * (e.g., "f(x) = x * 2") and extracts their names and types.
 *
 * Only extracts variables/functions from lines BEFORE the specified sortOrder to prevent
 * forward references.
 *
 * @param lines All lines in the file
 * @param upToSortOrder Only extract from lines with sortOrder < this value
 * @return Set of Suggestions defined before the specified line
 */
@OptIn(ExperimentalMaterial3Api::class)
/**
 * Extracts suggestions from all lines in a single pass, returning a list where each element
 * contains the available variables and file variables for the corresponding line.
 */
private fun extractAllSuggestions(lines: List<LineEntity>): List<Pair<Set<Suggestion>, Map<String, String>>> {
    val result = mutableListOf<Pair<Set<Suggestion>, Map<String, String>>>()

    // Use a map to track suggestions by name for easy overwriting/updates
    val suggestionMap = mutableMapOf<String, Suggestion>()

    // Initialize with defaults (Dynamic variables, constants, global functions)
    MathEngine.dynamicVariableNames.forEach { suggestionMap[it] = Suggestion(it, SuggestionType.DYNAMIC_VARIABLE) }
    Builtins.constantNames.forEach { suggestionMap[it] = Suggestion(it, SuggestionType.CONSTANT) }
    Builtins.functionNames.forEach { suggestionMap[it] = Suggestion(it, SuggestionType.GLOBAL_FUNCTION) }
    suggestionMap["file"] = Suggestion("file", SuggestionType.GLOBAL_FUNCTION)
    suggestionMap["convert"] = Suggestion("convert", SuggestionType.GLOBAL_FUNCTION)

    val currentFileVariables = mutableMapOf<String, String>()
    val varFuncNamePattern = Constants.VAR_FUNC_NAME_PATTERN.removePrefix("^").removeSuffix("$")

    var lastSet = suggestionMap.values.toSet()
    var lastMap = currentFileVariables.toMap()

    lines.forEach { line ->
        // Add current state as the result for this line
        // Optimization: Reuse the last set/map if no changes occurred in this line
        result.add(lastSet to lastMap)

        // Update state with variables/functions from THIS line for SUBSEQUENT lines
        // Strip comments first
        val hashIndex = line.expression.indexOf('#')
        val exprWithoutComment = if (hashIndex >= 0) {
            line.expression.substring(0, hashIndex).trim()
        } else {
            line.expression
        }

        if (exprWithoutComment.contains("=")) {
            // Match both `var =` and `f(...) =`
            val regex = Regex("""^\s*($varFuncNamePattern)(?:\s*\((.*?)\))?\s*=""")
            val matchResult = regex.find(exprWithoutComment)
            if (matchResult != null) {
                val name = matchResult.groupValues[1].trim()

                // If it matched the (...) part or if there is a '(' after it, it's a function
                val afterIdentifier = exprWithoutComment.substring(matchResult.groups[1]!!.range.last + 1).trimStart()
                val isFunction = afterIdentifier.startsWith("(")

                if (isFunction) {
                    suggestionMap[name] = Suggestion(name, SuggestionType.LOCAL_FUNCTION)
                    lastSet = suggestionMap.values.toSet()
                } else {
                    suggestionMap[name] = Suggestion(name, SuggestionType.VARIABLE)
                    lastSet = suggestionMap.values.toSet()

                    // Reset file variable if reassigned to something else.
                    // Use a snapshot so `f = f` can preserve the existing file link.
                    val previousFileVariables = currentFileVariables.toMap()
                    val wasFileVariable = previousFileVariables.containsKey(name)
                    currentFileVariables.remove(name)

                    val rhs = exprWithoutComment.substring(matchResult.groups[0]!!.range.last + 1).trim()
                    val fileMatch = Regex("""file\(\s*"([^"]+)"\s*\)""").matchEntire(rhs)
                    when {
                        fileMatch != null -> currentFileVariables[name] = fileMatch.groupValues[1]
                        rhs in previousFileVariables -> currentFileVariables[name] = previousFileVariables.getValue(rhs)
                    }

                    // Only recreate map if it actually changed
                    if (wasFileVariable || fileMatch != null || rhs in previousFileVariables) {
                        lastMap = currentFileVariables.toMap()
                    }
                }
            }
        } else {
            val incrementOrDecrement = Regex("""^\s*($varFuncNamePattern)\s*(\+\+|--)\s*$""").find(exprWithoutComment)
            if (incrementOrDecrement != null) {
                val name = incrementOrDecrement.groupValues[1].trim()
                if (currentFileVariables.remove(name) != null) {
                    lastMap = currentFileVariables.toMap()
                }
            } else {
                val compoundAssignment = Regex("""^\s*($varFuncNamePattern)\s*(\+=|-=|\*=|/=|%=)""").find(exprWithoutComment)
                if (compoundAssignment != null) {
                    val name = compoundAssignment.groupValues[1].trim()
                    if (currentFileVariables.remove(name) != null) {
                        lastMap = currentFileVariables.toMap()
                    }
                }
            }
        }
    }
    return result
}

@OptIn(ExperimentalMaterial3Api::class)
/**
 * Main calculator editor screen.
 *
 * @param fileId ID of the file to edit
 * @param viewModel ViewModel managing calculator state and operations
 * @param onBack Callback when back button is pressed
 * @param onHelp Callback when help button is pressed
 * @param onNavigateToFile Callback when navigating to a different file (used for duplicate)
 */
@Composable
fun CalculatorScreen(
    fileId: Long,
    viewModel: CalculatorViewModel,
    regionCode: String,
    showPrecisionEllipsis: Boolean,
    editorFontSize: Float,
    onBack: () -> Unit,
    onHelp: () -> Unit,
    onNavigateToFile: (Long) -> Unit = {}
) {
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()
    val listState = rememberLazyListState()
    val handleBack = {
        coroutineScope.launch {
            viewModel.deleteFileIfEmptyAndRecent(fileId)
            onBack()
        }
        Unit
    }

    BackHandler(onBack = handleBack)

    val globalShowLineNumbers by viewModel.showLineNumbers.collectAsState()
    var localShowLineNumbers by rememberSaveable(fileId) { mutableStateOf<Boolean?>(null) }
    val effectiveShowLineNumbers = localShowLineNumbers ?: globalShowLineNumbers

    val globalShowSuggestions by viewModel.showSuggestions.collectAsState()
    var localShowSuggestions by rememberSaveable(fileId) { mutableStateOf<Boolean?>(null) }
    val effectiveShowSuggestions = localShowSuggestions ?: globalShowSuggestions

    val globalRationalMode by viewModel.rationalMode.collectAsState()
    var localRationalMode by rememberSaveable(fileId) { mutableStateOf<Boolean?>(null) }
    val effectiveRationalMode = localRationalMode ?: globalRationalMode

    val precision by viewModel.precision.collectAsState()
    val files by viewModel.allFiles.collectAsState(initial = emptyList())
    val canUndoMap by viewModel.canUndo.collectAsState()
    val canRedoMap by viewModel.canRedo.collectAsState()
    val canUndo = canUndoMap[fileId] ?: false
    val canRedo = canRedoMap[fileId] ?: false

    LaunchedEffect(fileId, effectiveRationalMode) {
        viewModel.recalculateFile(fileId, effectiveRationalMode)
    }

    val lines by viewModel.getLines(fileId).collectAsState(initial = emptyList())


    val globalShowSymbolsShortcuts by viewModel.showSymbolsShortcuts.collectAsState()
    var localShowSymbolsShortcuts by rememberSaveable(fileId) { mutableStateOf<Boolean?>(null) }
    val effectiveShowSymbolsShortcuts = localShowSymbolsShortcuts ?: globalShowSymbolsShortcuts

    val globalShowNumbersShortcuts by viewModel.showNumbersShortcuts.collectAsState()
    var localShowNumbersShortcuts by rememberSaveable(fileId) { mutableStateOf<Boolean?>(null) }
    val effectiveShowNumbersShortcuts = localShowNumbersShortcuts ?: globalShowNumbersShortcuts

    val globalGroupingSeparatorEnabled by viewModel.groupingSeparatorEnabled.collectAsState()
    var localGroupingSeparatorEnabled by rememberSaveable(fileId) { mutableStateOf<Boolean?>(null) }
    val effectiveGroupingSeparatorEnabled = localGroupingSeparatorEnabled ?: globalGroupingSeparatorEnabled

    var showMenu by remember { mutableStateOf(false) }
    var showRenameDialog by remember { mutableStateOf(false) }
    var showClearConfirmDialog by remember { mutableStateOf(false) }
    var showDeleteConfirmDialog by remember { mutableStateOf(false) }
    var showInfoDialog by remember { mutableStateOf(false) }
    val scratchpadFileId by viewModel.scratchpadFileId.collectAsState(initial = null)
    val isScratchpad = scratchpadFileId != null && fileId == scratchpadFileId
    val currentFile = files.find { it.id == fileId }
    val isLocked = currentFile?.isLocked ?: false
    val fileName = if (isScratchpad) Constants.SCRATCHPAD_DISPLAY_NAME else (currentFile?.name ?: "Editor")

    // Track which line should be focused and cursor position
    var focusLineId by remember { mutableStateOf<Long?>(null) }
    var focusCursorPosition by remember { mutableStateOf<Int?>(null) }
    var pendingScrollLineId by remember { mutableStateOf<Long?>(null) }

    // Track if auto-focus has been tried for the current file to avoid repeating it
    var hasAttemptedAutoFocus by rememberSaveable(fileId) { mutableStateOf(false) }

    // Auto-focus the first line if the file is new/empty
    LaunchedEffect(lines) {
        if (!hasAttemptedAutoFocus && lines.isNotEmpty()) {
            hasAttemptedAutoFocus = true
            val firstLine = lines.firstOrNull() ?: return@LaunchedEffect
            if (lines.size == 1 && firstLine.expression.isEmpty()) {
                focusLineId = firstLine.id
                focusCursorPosition = 0
            }
        }
    }

    // Track which line is currently focused by the user
    var currentlyFocusedLineId by remember { mutableStateOf<Long?>(null) }

    // Track toolbar text insertion requests (used for inserting symbols using custom keyboard shortcuts)
    var insertTextRequest by remember { mutableStateOf<Pair<Long, String>?>(null) }
    // Check if keyboard is visible
    val density = LocalDensity.current
    val imeInsets = WindowInsets.ime

    // Auto-focus and scroll to newly created lines
    LaunchedEffect(lines, pendingScrollLineId) {
        val targetId = pendingScrollLineId ?: return@LaunchedEffect

        // Wait a bit for the keyboard/toolbar to settle before calculating scroll
        delay(50)

        val targetIndex = lines.indexOfFirst { it.id == targetId }
        if (targetIndex >= 0) {
            val layoutInfo = listState.layoutInfo
            val visibleItems = layoutInfo.visibleItemsInfo
            val targetVisibleItem = visibleItems.find { it.index == targetIndex }

            // If the item is below the viewport or at the bottom edge,
            // scroll so it stays at the bottom (pushing previous lines up).
            // We include a 48dp margin at the bottom for breathing room and to clear the toolbar.
            val viewportHeight = layoutInfo.viewportSize.height
            val bottomMarginPx = (48 * density.density).toInt()

            if (targetVisibleItem == null) {
                // Not visible at all
                val lastVisibleIndex = visibleItems.lastOrNull()?.index ?: -1
                if (targetIndex > lastVisibleIndex) {
                    // It's below. Scroll so it's at the bottom + margin.
                    // Since we don't know the exact height yet, we use a reasonable default (48dp).
                    val estimatedItemHeight = (48 * density.density).toInt()
                    listState.animateScrollToItem(targetIndex, -(viewportHeight - estimatedItemHeight - bottomMarginPx))
                } else {
                    // It's above. Scroll to top.
                    listState.animateScrollToItem(targetIndex)
                }
            } else {
                // Item is partially or fully visible.
                val isFullyVisible = targetVisibleItem.offset >= layoutInfo.viewportStartOffset &&
                    (targetVisibleItem.offset + targetVisibleItem.size) <= viewportHeight - bottomMarginPx

                if (!isFullyVisible || targetVisibleItem.offset + targetVisibleItem.size > viewportHeight - bottomMarginPx - 10) {
                    // If it's near or past the bottom margin, dock it at the bottom margin.
                    val offset = viewportHeight - targetVisibleItem.size - bottomMarginPx
                    listState.animateScrollToItem(targetIndex, -offset)
                }
            }
            pendingScrollLineId = null
        }
    }

    // Check if keyboard is visible
    val isKeyboardVisible = imeInsets.getBottom(density) > 0

    // Theme-aware colors - respect app theme setting, not system
    val currentTheme by viewModel.currentTheme.collectAsState()
    val isDarkTheme = when (currentTheme) {
        "light" -> false
        "dark" -> true
        else -> isSystemInDarkTheme()
    }
    val numberColor = if (isDarkTheme) SyntaxColors.NumberColorDark else SyntaxColors.NumberColorLight
    val variableColor = if (isDarkTheme) SyntaxColors.VariableColorDark else SyntaxColors.VariableColorLight
    val keywordColor = if (isDarkTheme) SyntaxColors.KeywordColorDark else SyntaxColors.KeywordColorLight
    val operatorColor = if (isDarkTheme) SyntaxColors.OperatorColorDark else SyntaxColors.OperatorColorLight
    val percentColor = if (isDarkTheme) SyntaxColors.PercentColorDark else SyntaxColors.PercentColorLight
    val commentColor = if (isDarkTheme) SyntaxColors.CommentColorDark else SyntaxColors.CommentColorLight
    val functionColor = if (isDarkTheme) SyntaxColors.FunctionColorDark else SyntaxColors.FunctionColorLight
    val conversionColor = if (isDarkTheme) SyntaxColors.ConversionColorDark else SyntaxColors.ConversionColorLight

    // Pre-calculate suggestions for all lines once per list update
    val allSuggestionsByLine by remember(lines) {
        derivedStateOf { extractAllSuggestions(lines) }
    }

    Scaffold(
        modifier = Modifier
            .fillMaxSize()
            .imePadding(),
        topBar = {
            Column {
                TopAppBar(
                    title = {
                        Column(
                            modifier = if (!isScratchpad && !isLocked) {
                                Modifier.clickable { showRenameDialog = true }
                            } else {
                                Modifier
                            }
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text(
                                    text = fileName,
                                    color = MaterialTheme.colorScheme.onSurface,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                                if (isLocked) {
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Icon(
                                        Icons.Default.Lock,
                                        contentDescription = "Locked",
                                        tint = MaterialTheme.colorScheme.onSurfaceVariant,
                                        modifier = Modifier.size(16.dp)
                                    )
                                }
                            }
                            if (isScratchpad) {
                                Text(
                                    text = "Temporary file • Changes not saved",
                                    style = MaterialTheme.typography.bodySmall.copy(fontSize = 10.sp),
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                            if (isLocked) {
                                Text(
                                    text = "Locked file • Cannot be modified",
                                    style = MaterialTheme.typography.bodySmall.copy(fontSize = 10.sp),
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                    },
                    navigationIcon = {
                        IconButton(onClick = handleBack) {
                            Icon(
                                Icons.AutoMirrored.Filled.ArrowBack,
                                "Back",
                                tint = MaterialTheme.colorScheme.onSurface
                            )
                        }
                    },
                    actions = {
                        if (!isLocked) {
                            IconButton(
                                onClick = { viewModel.undo(fileId, effectiveRationalMode) },
                                enabled = canUndo
                            ) {
                                Icon(
                                    Icons.AutoMirrored.Filled.Undo,
                                    "Undo",
                                    tint = if (canUndo) MaterialTheme.colorScheme.onSurface else MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }

                            IconButton(
                                onClick = { viewModel.redo(fileId, effectiveRationalMode) },
                                enabled = canRedo
                            ) {
                                Icon(
                                    Icons.AutoMirrored.Filled.Redo,
                                    "Redo",
                                    tint = if (canRedo) MaterialTheme.colorScheme.onSurface else MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }

                        Box {
                            IconButton(onClick = { showMenu = true }) {
                                Icon(
                                    Icons.Default.MoreVert,
                                    "More options",
                                    tint = MaterialTheme.colorScheme.onSurface
                                )
                            }
                            val configuration = LocalConfiguration.current
                            val screenHeight = configuration.screenHeightDp.dp
                            DropdownMenu(
                                expanded = showMenu,
                                onDismissRequest = { showMenu = false },
                                modifier = Modifier
                                    .heightIn(max = screenHeight * 0.8f)
                                    .navigationBarsPadding()
                            ) {
                                DropdownMenuItem(
                                    text = { Text("Help") },
                                    leadingIcon = {
                                        Icon(
                                            Icons.AutoMirrored.Filled.HelpOutline,
                                            contentDescription = null
                                        )
                                    },
                                    onClick = {
                                        showMenu = false
                                        onHelp()
                                    }
                                )
                                HorizontalDivider()

                                val leadIcon = if (effectiveShowLineNumbers) Icons.Default.ViewHeadline else Icons.Default.FormatListNumbered
                                DropdownMenuItem(
                                    text = { Text(if (effectiveShowLineNumbers) "Hide line numbers" else "Show line numbers") },
                                    onClick = {
                                        val nextValue = !effectiveShowLineNumbers
                                        localShowLineNumbers =
                                            nextValue.takeUnless { it == globalShowLineNumbers }
                                        showMenu = false
                                    },
                                    leadingIcon = {
                                        Icon(
                                            leadIcon,
                                            contentDescription = null
                                        )
                                    }
                                )
                                if (!isLocked) {
                                    DropdownMenuItem(
                                        text = { Text(if (effectiveShowSuggestions) "Hide suggestions" else "Show suggestions") },
                                        onClick = {
                                            val nextValue = !effectiveShowSuggestions
                                            localShowSuggestions =
                                                nextValue.takeUnless { it == globalShowSuggestions }
                                            showMenu = false
                                        },
                                        leadingIcon = {
                                            Icon(
                                                if (effectiveShowSuggestions) Icons.Default.ChatBubbleOutline else Icons.Default.Chat,
                                                contentDescription = null
                                            )
                                        }
                                    )

                                    DropdownMenuItem(
                                        text = { Text(if (effectiveShowSymbolsShortcuts) "Hide symbols shortcuts" else "Show symbols shortcuts") },
                                        onClick = {
                                            val nextValue = !effectiveShowSymbolsShortcuts
                                            localShowSymbolsShortcuts =
                                                nextValue.takeUnless { it == globalShowSymbolsShortcuts }
                                            showMenu = false
                                        },
                                        leadingIcon = {
                                            Icon(
                                                Icons.Default.Calculate,
                                                contentDescription = null
                                            )
                                        }
                                    )
                                    DropdownMenuItem(
                                        text = { Text(if (effectiveShowNumbersShortcuts) "Hide numbers shortcuts" else "Show numbers shortcuts") },
                                        onClick = {
                                            val nextValue = !effectiveShowNumbersShortcuts
                                            localShowNumbersShortcuts =
                                                nextValue.takeUnless { it == globalShowNumbersShortcuts }
                                            showMenu = false
                                        },
                                        leadingIcon = {
                                            Icon(
                                                Icons.Default.Pin,
                                                contentDescription = null
                                            )
                                        }
                                    )
                                }
                                DropdownMenuItem(
                                    text = { Text(if (effectiveRationalMode) "Disable rational mode" else "Enable rational mode") },
                                    onClick = {
                                        val nextValue = !effectiveRationalMode
                                        localRationalMode =
                                            nextValue.takeUnless { it == globalRationalMode }
                                        showMenu = false
                                    },
                                    leadingIcon = {
                                        Icon(
                                            Icons.Default.BorderHorizontal,
                                            contentDescription = null
                                        )
                                    }
                                )
                                DropdownMenuItem(
                                    text = { Text(if (effectiveGroupingSeparatorEnabled) "Disable grouping separator" else "Enable grouping separator") },
                                    onClick = {
                                        val nextValue = !effectiveGroupingSeparatorEnabled
                                        localGroupingSeparatorEnabled =
                                            nextValue.takeUnless { it == globalGroupingSeparatorEnabled }
                                        showMenu = false
                                    },
                                    leadingIcon = { Icon(Icons.Default.SpaceBar, contentDescription = null) }
                                )
                                if (!isScratchpad) {
                                    DropdownMenuItem(
                                        text = { Text(if (isLocked) "Unlock File" else "Lock File") },
                                        leadingIcon = {
                                            Icon(
                                                if (isLocked) Icons.Default.LockOpen else Icons.Default.Lock,
                                                contentDescription = null
                                            )
                                        },
                                        onClick = {
                                            showMenu = false
                                            viewModel.toggleLockFile(fileId)
                                        }
                                    )
                                }
                                if (!isScratchpad) {
                                    DropdownMenuItem(
                                        text = { Text("Rename File") },
                                        leadingIcon = {
                                            Icon(
                                                Icons.Default.Edit,
                                                contentDescription = null
                                            )
                                        },
                                        enabled = !isLocked,
                                        onClick = {
                                            showMenu = false
                                            showRenameDialog = true
                                        }
                                    )
                                    DropdownMenuItem(
                                        text = { Text("File info") },
                                        leadingIcon = {
                                            Icon(
                                                Icons.Outlined.Info,
                                                contentDescription = null
                                            )
                                        },
                                        onClick = {
                                            showMenu = false
                                            showInfoDialog = true
                                        }
                                    )
                                }
                                DropdownMenuItem(
                                    text = { Text("Duplicate File") },
                                    leadingIcon = {
                                        Icon(
                                            Icons.Default.FileCopy,
                                            contentDescription = null
                                        )
                                    },
                                    onClick = {
                                        showMenu = false
                                        viewModel.duplicateFile(context, fileId) { newFileId ->
                                            onNavigateToFile(newFileId)
                                        }
                                    }
                                )
                                HorizontalDivider()
                                DropdownMenuItem(
                                    text = { Text("Copy File Content") },
                                    leadingIcon = {
                                        Icon(
                                            Icons.Default.ContentCopy,
                                            contentDescription = null
                                        )
                                    },
                                    onClick = {
                                        showMenu = false
                                        coroutineScope.launch {
                                            val result =
                                                viewModel.copyFileToClipboard(context, fileId)
                                            result.onSuccess { message ->
                                                Toast.makeText(context, message, Toast.LENGTH_SHORT)
                                                    .show()
                                            }.onFailure { error ->
                                                Toast.makeText(
                                                    context,
                                                    "Copy failed: ${error.message}",
                                                    Toast.LENGTH_LONG
                                                ).show()
                                            }
                                        }
                                    }
                                )
                                DropdownMenuItem(
                                    text = { Text("Export as PDF") },
                                    leadingIcon = {
                                        Icon(
                                            Icons.Default.PictureAsPdf,
                                            contentDescription = null
                                        )
                                    },
                                    onClick = {
                                        showMenu = false
                                        val safeFileName = fileName.replace(Regex("[^a-zA-Z0-9.-]"), "_")
                                        coroutineScope.launch {
                                            ExportUtils.exportAsPdf(context, safeFileName, lines, precision, viewModel.regionCode.value, effectiveGroupingSeparatorEnabled)
                                        }
                                    }
                                )
                                DropdownMenuItem(
                                    text = { Text("Export as Image") },
                                    leadingIcon = {
                                        Icon(
                                            Icons.Default.Image,
                                            contentDescription = null
                                        )
                                    },
                                    onClick = {
                                        showMenu = false
                                        val safeFileName = fileName.replace(Regex("[^a-zA-Z0-9.-]"), "_")
                                        coroutineScope.launch {
                                            ExportUtils.exportAsImage(context, safeFileName, lines, precision, viewModel.regionCode.value, effectiveGroupingSeparatorEnabled)
                                        }
                                    }
                                )
                                HorizontalDivider()
                                DropdownMenuItem(
                                    text = { Text("Clear File") },
                                    leadingIcon = {
                                        Icon(
                                            Icons.Default.CleaningServices,
                                            contentDescription = null
                                        )
                                    },
                                    enabled = !isLocked,
                                    onClick = {
                                        showMenu = false
                                        showClearConfirmDialog = true
                                    }
                                )
                                if (!isScratchpad) {
                                    DropdownMenuItem(
                                        text = { Text("Delete File") },
                                        leadingIcon = {
                                            Icon(
                                                Icons.Default.Delete,
                                                contentDescription = null
                                            )
                                        },
                                        enabled = !isLocked,
                                        onClick = {
                                            showMenu = false
                                            showDeleteConfirmDialog = true
                                        }
                                    )
                                }
                            }
                        }
                    },
                    colors = TopAppBarDefaults.topAppBarColors(containerColor = MaterialTheme.colorScheme.surface)
                )
                HorizontalDivider(
                    thickness = 1.dp,
                    color = MaterialTheme.colorScheme.outlineVariant
                )
            }
        },
        bottomBar = {
            if (isKeyboardVisible && (effectiveShowSymbolsShortcuts || effectiveShowNumbersShortcuts)) {
                Column {
                    HorizontalDivider(
                        thickness = 1.dp,
                        color = MaterialTheme.colorScheme.outlineVariant
                    )

                    if (effectiveShowSymbolsShortcuts) {
                        // Symbols shortcuts bar
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .horizontalScroll(rememberScrollState())
                                .background(MaterialTheme.colorScheme.surfaceVariant)
                                .padding(start = 8.dp, end = 8.dp, top = 8.dp, bottom = 4.dp),
                            horizontalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            val symbols = listOf(".", "_", "(", ")", "#", "=", "+", "-", "×", "÷", "%", "^", "\"", "°", "π")

                            symbols.forEach { symbol ->
                                ShortcutButton(text = symbol) {
                                    currentlyFocusedLineId?.let { lineId ->
                                        insertTextRequest = Pair(lineId, symbol)
                                    }
                                }
                            }
                        }
                    }

                    if (effectiveShowNumbersShortcuts) {
                        // Numbers shortcuts bar
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .horizontalScroll(rememberScrollState())
                                .background(MaterialTheme.colorScheme.surfaceVariant)
                                .padding(start = 8.dp, end = 8.dp, top = 4.dp, bottom = 8.dp),
                            horizontalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            val numbers = listOf("1", "2", "3", "4", "5", "6", "7", "8", "9", "0")
                            numbers.forEach { number ->
                                ShortcutButton(text = number) {
                                    currentlyFocusedLineId?.let { lineId ->
                                        insertTextRequest = Pair(lineId, number)
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    ) { paddingValues ->
        // Editor area
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            val textMeasurer = rememberTextMeasurer()
            val gutterStyle = MaterialTheme.typography.bodySmall.copy(
                fontFamily = FiraCodeFamily,
                fontSize = 12.sp
            )
            val digitWidthPx = remember(textMeasurer, gutterStyle) {
                textMeasurer.measure("0", style = gutterStyle).size.width
            }
            val digitWidth = with(LocalDensity.current) { digitWidthPx.toDp() }

            val maxLineDigits = lines.size.toString().length
            val numberWidth = (digitWidth * maxLineDigits)
            val gutterOffset = 16.dp + numberWidth // 8dp start + 8dp end padding

            // Full-height vertical dividers as background
            Row(modifier = Modifier.fillMaxSize()) {
                if (effectiveShowLineNumbers) {
                    Box(modifier = Modifier.width(gutterOffset))
                    VerticalDivider(
                        modifier = Modifier.fillMaxHeight(),
                        thickness = 1.dp,
                        color = MaterialTheme.colorScheme.outlineVariant
                    )
                }
                Box(modifier = Modifier.weight(1f))
                VerticalDivider(
                    modifier = Modifier.fillMaxHeight(),
                    thickness = 1.dp,
                    color = MaterialTheme.colorScheme.outlineVariant
                )
                // Continuous background for the result column
                Box(
                    modifier = Modifier
                        .width(120.dp)
                        .fillMaxHeight()
                        .background(MaterialTheme.colorScheme.surface)
                        .background(MaterialTheme.colorScheme.scrim.copy(alpha = 0.15f))
                )
            }

            // LazyColumn with lines
            LazyColumn(
                state = listState,
                modifier = Modifier.fillMaxSize()
            ) {

                itemsIndexed(lines, key = { _, line -> line.id }) { index, line ->
                    // Get pre-calculated available variables for this line
                    val (availableVariables, fileVariables) = allSuggestionsByLine.getOrElse(index) { emptySet<Suggestion>() to emptyMap<String, String>() }

                    LineRow(
                        line = line,
                        lineNumber = index + 1,
                        showLineNumbers = effectiveShowLineNumbers,
                        showSuggestions = effectiveShowSuggestions,
                        isLocked = isLocked,
                        precision = precision,
                        regionCode = regionCode,
                        editorFontSize = editorFontSize,
                        numberWidth = numberWidth,
                        availableVariables = availableVariables,
                        fileVariables = fileVariables,
                        showPrecisionEllipsis = showPrecisionEllipsis,
                        allFiles = files,
                        onGetSuggestionsForFile = viewModel::getSuggestionsForFile,
                        shouldFocus = focusLineId == line.id,
                        focusCursorPos = if (focusLineId == line.id) focusCursorPosition else null,
                        insertTextRequest = if (insertTextRequest?.first == line.id) insertTextRequest?.second else null,
                        onInsertHandled = { insertTextRequest = null },
                        numberColor = numberColor,
                        variableColor = variableColor,
                        keywordColor = keywordColor,
                        functionColor = functionColor,
                        operatorColor = operatorColor,
                        percentColor = percentColor,
                        commentColor = commentColor,
                        conversionColor = conversionColor,
                        onFocused = {
                            currentlyFocusedLineId = line.id
                            focusLineId = null
                            focusCursorPosition = null
                        },
                        onBlur = {
                            if (currentlyFocusedLineId == line.id) {
                                currentlyFocusedLineId = null
                            }
                        },
                        groupingSeparatorEnabled = effectiveGroupingSeparatorEnabled,
                        onValueChange = { newValue, newVersion ->
                            viewModel.updateLine(line.copy(expression = newValue, version = newVersion), effectiveRationalMode)
                            // Scroll to this line only if it's being edited but off-screen
                            if (currentlyFocusedLineId == line.id) {
                                coroutineScope.launch {
                                    val visibleItems = listState.layoutInfo.visibleItemsInfo
                                    val isVisible = visibleItems.any { it.index == index }
                                    if (!isVisible) {
                                        listState.animateScrollToItem(index)
                                    }
                                }
                            }
                        },
                        onEnter = { expression, splitIndex ->
                            coroutineScope.launch {
                                val newId = viewModel.splitLine(line.id, splitIndex, expression, effectiveRationalMode)
                                focusLineId = newId
                                // New lines created via Enter (splitting) have a leading space;
                                // we want to focus at the very start of the moved text (pos 0).
                                focusCursorPosition = 0
                                pendingScrollLineId = newId
                            }
                        },
                        onDelete = {
                            if (lines.size > 1) {
                                val prevIndex = index - 1
                                val prevLine = lines.getOrNull(prevIndex)
                                if (prevLine != null) {
                                    focusLineId = prevLine.id
                                    focusCursorPosition = prevLine.expression.length
                                }
                                viewModel.deleteLine(line, effectiveRationalMode)
                            }
                        },
                        onMergeWithPrevious = {
                            if (index > 0) {
                                val prevLine = lines[index - 1]
                                val currentLine = line
                                focusLineId = prevLine.id
                                focusCursorPosition = prevLine.expression.length

                                coroutineScope.launch {
                                    viewModel.mergeLines(prevLine.id, currentLine.id, effectiveRationalMode)
                                }
                            }
                        },
                        onNavigateUp = {
                            if (index > 0) {
                                val prevLine = lines[index - 1]
                                focusLineId = prevLine.id
                                focusCursorPosition = prevLine.expression.length
                            }
                        },
                        onNavigateDown = {
                            if (index < lines.size - 1) {
                                val nextLine = lines[index + 1]
                                focusLineId = nextLine.id
                                focusCursorPosition = 0
                            }
                        },
                        onCopyResult = { result ->
                            viewModel.copyToClipboard(context, result)
                        },
                        bottomOffset = paddingValues.calculateBottomPadding(),
                        onGetErrorMessage = { lineId -> viewModel.getLineErrorMessage(fileId, lineId, effectiveRationalMode) }
                    )
                    if (index < lines.size - 1) {
                        HorizontalDivider(
                            thickness = 1.dp,
                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.20f)
                        )
                    }
                }
            }
        }
    }

    // Rename File dialog
    if (showRenameDialog) {
        RenameFileDialog(
            viewModel = viewModel,
            fileId = fileId,
            currentName = fileName,
            onDismiss = { showRenameDialog = false },
            onConfirm = { newName ->
                val success = viewModel.renameFile(context, fileId, newName)
                if (success) {
                    showRenameDialog = false
                }
                success
            }
        )
    }

    if (showInfoDialog && currentFile != null) {
        FileInfoDialog(
            viewModel = viewModel,
            file = currentFile,
            onDismiss = { showInfoDialog = false }
        )
    }

    // Clear All confirmation dialog
    if (showClearConfirmDialog) {
        AlertDialog(
            onDismissRequest = { showClearConfirmDialog = false },
            title = { Text("Clear all lines?") },
            text = { Text("This will delete all lines in this file. This action cannot be undone.") },
            confirmButton = {
                TextButton(
                    onClick = {
                        showClearConfirmDialog = false
                        viewModel.clearAllLines(fileId)
                    }
                ) {
                    Text("Clear", color = MaterialTheme.colorScheme.error)
                }
            },
            dismissButton = {
                TextButton(onClick = { showClearConfirmDialog = false }) {
                    Text("Cancel")
                }
            }
        )
    }

    // Delete File confirmation dialog
    if (showDeleteConfirmDialog) {
        DeleteFileDialog(
            fileName = fileName,
            onDismiss = { showDeleteConfirmDialog = false },
            onConfirm = {
                val fileDeleted = viewModel.deleteFile(context, fileId)
                if (fileDeleted) {
                    onBack()
                }
                fileDeleted
            }
        )
    }
}

private fun formatResult(
    text: String,
    precision: Int,
    regionCode: String,
    groupingSeparatorEnabled: Boolean,
    showEllipsis: Boolean = false
): String {
    return MathEngine.formatDisplayResult(
        text,
        precision,
        regionCode = regionCode,
        groupingSeparatorEnabled = groupingSeparatorEnabled,
        showEllipsis = showEllipsis
    )
}

private fun formatAnnotatedResult(
    text: String,
    precision: Int,
    regionCode: String,
    groupingSeparatorEnabled: Boolean,
    showPrecisionEllipsis: Boolean,
    resultColor: Color
): AnnotatedString {
    val formatted = formatResult(text, precision, regionCode, groupingSeparatorEnabled, showPrecisionEllipsis)
    return buildAnnotatedString {
        val ellipsisIndex = formatted.indexOf('…')
        if (showPrecisionEllipsis && ellipsisIndex != -1) {
            append(formatted.substring(0, ellipsisIndex))
            withStyle(SpanStyle(color = resultColor.copy(alpha = 0.5f), fontWeight = FontWeight.Normal)) {
                append("…")
            }
            append(formatted.substring(ellipsisIndex + 1))
        } else {
            append(formatted)
        }
    }
}

@Composable
private fun ShortcutButton(
    text: String,
    onClick: () -> Unit
) {
    Surface(
        onClick = onClick,
        modifier = Modifier
            .width(37.dp)
            .height(37.dp),
        shape = RoundedCornerShape(8.dp),
        color = MaterialTheme.colorScheme.surface,
        tonalElevation = 1.dp,
        shadowElevation = 2.dp
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .border(
                    width = 0.5.dp,
                    color = MaterialTheme.colorScheme.outline.copy(alpha = 0.3f),
                    shape = RoundedCornerShape(8.dp)
                ),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = text,
                style = MaterialTheme.typography.bodyLarge.copy(
                    fontFamily = FiraCodeFamily,
                    fontWeight = FontWeight.Medium
                ),
                color = MaterialTheme.colorScheme.onSurface
            )
        }
    }
}


@Composable
private fun LineRow(
    line: LineEntity,
    lineNumber: Int,
    showLineNumbers: Boolean,
    showSuggestions: Boolean,
    isLocked: Boolean,
    showPrecisionEllipsis: Boolean,
    precision: Int,
    regionCode: String,
    editorFontSize: Float,
    numberWidth: Dp,
    availableVariables: Set<Suggestion>,
    fileVariables: Map<String, String> = emptyMap(),
    allFiles: List<FileEntity>,
    onGetSuggestionsForFile: suspend (String) -> Set<Suggestion>,
    shouldFocus: Boolean,
    focusCursorPos: Int?,
    insertTextRequest: String?,
    onInsertHandled: () -> Unit,
    numberColor: Color,
    variableColor: Color,
    keywordColor: Color,
    functionColor: Color,
    operatorColor: Color,
    percentColor: Color,
    commentColor: Color,
    conversionColor: Color,
    onFocused: () -> Unit,
    onBlur: () -> Unit,
    groupingSeparatorEnabled: Boolean,
    onValueChange: (String, Long) -> Unit,
    onEnter: (String, Int) -> Unit,
    onDelete: () -> Unit,
    onMergeWithPrevious: () -> Unit,
    onNavigateUp: () -> Unit,
    onNavigateDown: () -> Unit,
    onCopyResult: (String) -> Unit,
    bottomOffset: Dp = 0.dp,
    onGetErrorMessage: suspend (Long) -> String? = { null }
) {
    // Add leading space for backspace detection trick (but not for first line)
    // This allows us to capture backspace even when TextField is at the start of original text
    val displayText = if (lineNumber > 1) " " + line.expression else line.expression
    val defaultTextColor = MaterialTheme.colorScheme.onSurface

    var textFieldValue by remember(line.id) {
        mutableStateOf(TextFieldValue(text = displayText))
    }


    var isFocused by remember { mutableStateOf(false) }

    // Track the last expression and version sent to the ViewModel.
    // Used only for the unfocused sync path — not for stale-echo detection while typing.
    var lastSentExpression by remember(line.id) { mutableStateOf<String?>(null) }
    var lastSentVersion by remember(line.id) { mutableStateOf(line.version) }

    // Guard flag to prevent double-triggering of backspace handlers
    var backspaceHandled by remember { mutableStateOf(false) }

    // Reset the backspace guard flag after a short delay or when the line expression changes
    LaunchedEffect(line.expression, line.id, line.version) {
        backspaceHandled = false
    }

    val syntaxHighlightingTransformation = remember(
        numberColor, variableColor, keywordColor, functionColor,
        operatorColor, percentColor, commentColor, conversionColor, defaultTextColor, fileVariables, lineNumber
    ) {
        SyntaxHighlightingTransformation(
            numberColor = numberColor,
            variableColor = variableColor,
            keywordColor = keywordColor,
            functionColor = functionColor,
            operatorColor = operatorColor,
            percentColor = percentColor,
            commentColor = commentColor,
            conversionColor = conversionColor,
            defaultColor = defaultTextColor,
            fileVariables = fileVariables,
            isNonFirstLine = lineNumber > 1
        )
    }

    val focusRequester = remember { FocusRequester() }

    // Autocomplete suggestions
    val density = LocalDensity.current
    val imeInsets = WindowInsets.ime
    val isKeyboardVisible = imeInsets.getBottom(density) > 0
    var forceDismissSuggestions by remember(textFieldValue.text, textFieldValue.selection) { mutableStateOf(false) }

    // Auto-dismiss suggestions when keyboard is closed
    LaunchedEffect(isKeyboardVisible) {
        if (!isKeyboardVisible && isFocused) {
            forceDismissSuggestions = true
        }
    }
    val suggestionContext = remember(textFieldValue.text, textFieldValue.selection, fileVariables) {
        val cursorPos = textFieldValue.selection.start
        val text = textFieldValue.text
        val beforeCursor = if (cursorPos > 0) text.substring(0, cursorPos) else ""
        getSuggestionContext(
            beforeCursor,
            text,
            cursorPos,
            fileVariables
        )
    }

    val currentWord = suggestionContext.word
    val contextType = suggestionContext.type
    val isExplicitTrigger = suggestionContext.isExplicitTrigger

    var remoteSuggestions by remember { mutableStateOf<Set<Suggestion>>(emptySet()) }

    // Extracts the target file name when user types a dot notation expression (e.g. `file("OtherFile").` or `var.`).
    val dotFileName = remember(textFieldValue.text, textFieldValue.selection, fileVariables) {
        val cursorPos = textFieldValue.selection.start
        val text = textFieldValue.text
        if (cursorPos > 0) {
            val beforeCursor = text.substring(0, cursorPos)
            // Checks for dot notation expression with either:
            // - a normal variable identifier (e.g., myFile)
            // - a file() expression (e.g., file("OtherFile"))
            val dotRegex = Regex("""(\w+|\bfile\(\s*"[^"]*"\s*\))\s*\.\s*(\w*)$""")
            val dotMatch = dotRegex.find(beforeCursor)
            if (dotMatch != null) {
                val objectName = dotMatch.groupValues[1]
                // If it's `file("OtherFile").something`, extract "OtherFile" literal directly
                if (objectName.startsWith("file(")) {
                    val m = Regex("""file\(\s*"([^"]+)"\s*\)""").find(objectName)
                    m?.groupValues?.getOrNull(1)
                } else {
                    // Otherwise resolve linked file name using local variable references mapping
                    fileVariables[objectName]
                }
            } else null
        } else null
    }

    var loadingSuggestions by remember { mutableStateOf(false) }

    LaunchedEffect(dotFileName) {
        if (dotFileName != null) {
            loadingSuggestions = true
            try {
                remoteSuggestions = onGetSuggestionsForFile(dotFileName)
            } finally {
                loadingSuggestions = false
            }
        } else {
            remoteSuggestions = emptySet()
            loadingSuggestions = false
        }
    }

    val combinedVariables = remember(availableVariables, remoteSuggestions, dotFileName, suggestionContext) {
        val isDotNotation = suggestionContext.type == SuggestionType.VARIABLE && suggestionContext.isExplicitTrigger
        if (isDotNotation) {
            if (dotFileName != null) remoteSuggestions else emptySet()
        } else {
            availableVariables
        }
    }

    var suggestions by remember { mutableStateOf<List<Suggestion>>(emptyList()) }

    LaunchedEffect(suggestionContext, combinedVariables, allFiles, forceDismissSuggestions, showSuggestions, loadingSuggestions, textFieldValue.text, textFieldValue.selection) {
        if (forceDismissSuggestions || !showSuggestions) {
            suggestions = emptyList()
            return@LaunchedEffect
        }

        // Debounce suggestion calculation to reduce UI lag during rapid typing
        kotlinx.coroutines.delay(100)

        val cursorPos = textFieldValue.selection.start
        val text = textFieldValue.text
        val beforeCursor = if (cursorPos > 0) text.substring(0, cursorPos) else ""

        val newSuggestions = if (currentWord.isNotEmpty() || isExplicitTrigger || contextType == SuggestionType.UNIT || contextType == SuggestionType.CONVERSION) {
            val tokens = runCatching {
                Lexer(beforeCursor).tokenize()
            }.getOrElse { emptyList() }
            val cleanTokens = tokens.filter { it.kind != TokenKind.EOF }

            if (loadingSuggestions && combinedVariables.isEmpty()) {
                listOf(Suggestion("Loading...", SuggestionType.VARIABLE))
            } else if (contextType == SuggestionType.FILE) {
                allFiles.filter { it.id != line.fileId }.mapNotNull { file ->
                    val match = file.name.calculateFuzzyMatch(currentWord, SuggestionType.FILE)
                    if (match != null && (currentWord.isEmpty() || file.name != currentWord)) {
                        Suggestion(
                            name = file.name,
                            type = SuggestionType.FILE,
                            matchIndices = match.matchIndices,
                            score = match.score
                        )
                    } else null
                }.sortedByDescending { it.score }
            } else if (contextType == SuggestionType.UNIT) {
                val category = suggestionContext.unitCategory
                val units = if (category != null) {
                    UnitConverter.UNITS.filter { u ->
                        u.category == category ||
                                (category == UnitCategory.SCALAR && u.category == UnitCategory.NUMERAL_SYSTEM) ||
                                (category == UnitCategory.NUMERAL_SYSTEM && u.category == UnitCategory.SCALAR)
                    }
                } else {
                    UnitConverter.UNITS
                }
                units.flatMap { unit ->
                    unit.symbols.map { symbol ->
                        Suggestion(name = symbol, type = SuggestionType.UNIT, replaceStart = suggestionContext.replaceStart)
                    }
                }.mapNotNull {
                    val match = it.name.calculateFuzzyMatch(currentWord, SuggestionType.UNIT)
                    if (match != null && it.name != currentWord) {
                        it.copy(matchIndices = match.matchIndices, score = if (currentWord.isEmpty()) 100 - units.indexOfFirst { u -> u.symbols.contains(it.name) } else match.score)
                    } else null
                }.sortedByDescending { it.score }
            } else if (contextType == SuggestionType.KEYWORD || contextType == SuggestionType.CONVERSION) {
                val keywords = listOf("to", "in", "as").map { Suggestion(name = it, type = SuggestionType.CONVERSION) }.mapNotNull {
                    val match = it.name.calculateFuzzyMatch(currentWord, SuggestionType.CONVERSION)
                    if (match != null && it.name != currentWord) {
                        it.copy(matchIndices = match.matchIndices, score = match.score)
                    } else null
                }

                val unitSuggestions = if (suggestionContext.unitStart != null) {
                    val unitQuery = (beforeCursor.substring(suggestionContext.unitStart, cursorPos)).trim()
                    val category = suggestionContext.unitCategory
                    val unitsForCategory = if (category != null) {
                        UnitConverter.UNITS.filter { u ->
                            u.category == category ||
                                    (category == UnitCategory.SCALAR && u.category == UnitCategory.NUMERAL_SYSTEM) ||
                                    (category == UnitCategory.NUMERAL_SYSTEM && u.category == UnitCategory.SCALAR)
                        }
                    } else {
                        UnitConverter.UNITS
                    }

                    unitsForCategory.flatMap { unit ->
                        unit.symbols.map { symbol ->
                            val match = symbol.calculateFuzzyMatch(unitQuery, SuggestionType.UNIT)
                            if (match != null && symbol != unitQuery) {
                                Suggestion(
                                    name = symbol,
                                    type = SuggestionType.UNIT,
                                    matchIndices = match.matchIndices,
                                    score = if (unitQuery.isEmpty()) 99 - UnitConverter.UNITS.indexOf(unit) % 100 else match.score,
                                    replaceStart = suggestionContext.unitStart
                                )
                            } else null
                        }
                    }.filterNotNull()
                } else emptyList()

                (keywords + unitSuggestions).sortedByDescending { it.score }
            } else {
                if (currentWord.isEmpty() || (currentWord.any { char -> char.isLetter() || char == '_' } &&
                    currentWord.all { char -> char.isLetterOrDigit() || char == '_' })) {
                    val prevToken = cleanTokens.getOrNull(cleanTokens.size - 2)
                    val showUnits = prevToken?.kind == TokenKind.NUMBER
                    val unitsList = if (showUnits) {
                        UnitConverter.UNITS.flatMap { u -> u.symbols.map { Suggestion(it, SuggestionType.UNIT) } }
                    } else emptyList()
                    val allSuggestions = combinedVariables + unitsList
                    allSuggestions.mapNotNull {
                        val match = it.name.calculateFuzzyMatch(currentWord, it.type)
                        if (match != null && it.name != currentWord) {
                            it.copy(matchIndices = match.matchIndices, score = match.score)
                        } else null
                    }.sortedByDescending { it.score }
                } else emptyList()
            }
        } else emptyList()

        suggestions = newSuggestions
    }

    // Handle back button to close suggestions first if they are open
    BackHandler(enabled = suggestions.isNotEmpty() && isFocused && !forceDismissSuggestions) {
        forceDismissSuggestions = true
    }

    // Sync with database updates
    LaunchedEffect(line.expression, line.version, lineNumber, isFocused) {
        // Hard bail: never overwrite local state while the user is typing.
        if (isFocused) return@LaunchedEffect

        if (textFieldValue.text != displayText) {
            val clampedStart = textFieldValue.selection.start.coerceIn(0, displayText.length)
            val clampedEnd = textFieldValue.selection.end.coerceIn(0, displayText.length)
            textFieldValue = textFieldValue.copy(
                text = displayText,
                selection = TextRange(clampedStart, clampedEnd)
            )
            // Sync our local tracking with the actual DB state
            lastSentExpression = line.expression
            lastSentVersion = line.version
        }
    }

    // Handle programmatic focus requests (from navigation or deletion)
    LaunchedEffect(shouldFocus, focusCursorPos) {
        if (shouldFocus && focusCursorPos != null) {
            focusRequester.requestFocus()

            // Set cursor position - focusCursorPos is the desired position in the expression
            // We need to map this to the displayText position
            val actualPos = if (lineNumber > 1) {
                // Account for the leading dummy space
                focusCursorPos + 1
            } else {
                focusCursorPos
            }
            val coercedPos = actualPos.coerceIn(0, textFieldValue.text.length)

            textFieldValue = textFieldValue.copy(
                selection = TextRange(coercedPos)
            )
            // onFocused() will be called by onFocusChanged when focus is gained
        }
    }

    // Handle toolbar text insertion
    LaunchedEffect(insertTextRequest) {
        if (insertTextRequest != null) {
            val currentSelection = textFieldValue.selection
            val currentText = textFieldValue.text
            val cursorPosition = currentSelection.start

            // Insert the text at cursor position
            val newText = currentText.substring(
                0,
                cursorPosition
            ) + insertTextRequest + currentText.substring(cursorPosition)
            val newCursorPosition = cursorPosition + insertTextRequest.length

            textFieldValue = textFieldValue.copy(
                text = newText,
                selection = TextRange(newCursorPosition)
            )

            val trimmedText = newText.trim()
            if (trimmedText != line.expression) {
                lastSentExpression = trimmedText
                lastSentVersion++
                onValueChange(trimmedText, lastSentVersion)
            }

            onInsertHandled()
        }
    }

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .height(IntrinsicSize.Min),
        verticalAlignment = Alignment.Top
    ) {
        if (showLineNumbers) {
            // Line Number Gutter
            Text(
                text = lineNumber.toString(),
                style = MaterialTheme.typography.bodySmall.copy(
                    fontFamily = FiraCodeFamily,
                    color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f),
                    fontSize = 12.sp
                ),
                modifier = Modifier
                    .padding(start = 8.dp, top = 10.dp)
                    .width(numberWidth),
                textAlign = TextAlign.End,
                softWrap = false,
                maxLines = 1
            )

            // Spacer for the global vertical divider
            Spacer(modifier = Modifier.width(9.dp)) // 8.dp right padding + 1.dp divider
        }

        // Editor with wrapping and autocomplete
        var boxPosition by remember { mutableStateOf(Offset.Zero) }
        var boxSize by remember { mutableStateOf(IntSize.Zero) }
        var textLayoutResult by remember { mutableStateOf<androidx.compose.ui.text.TextLayoutResult?>(null) }

        Box(
            modifier = Modifier
                .weight(1f)
                .onGloballyPositioned { coordinates ->
                    boxPosition = coordinates.positionInWindow()
                    boxSize = coordinates.size
                }
                .padding(horizontal = 10.dp, vertical = 10.dp)
        ) {
            Column {
                BasicTextField(
                    value = textFieldValue,
                    readOnly = isLocked,
                    onValueChange = { newValue ->
                        if (isLocked) return@BasicTextField
                        val filteredText = newValue.text.replace("\n", "")

                        // Handle Enter key (Line Splitting)
                        // Using indexOf('\n') is more reliable than selection.start because
                        // selection.start can jump ahead after character insertion.
                        if (newValue.text.contains("\n")) {
                            val splitIndexInTransformed = newValue.text.indexOf('\n')
                            val actualText = newValue.text.replace("\n", "")
                            val normalizedText = if (lineNumber > 1) actualText.removePrefix(" ") else actualText
                            val splitIndex = if (lineNumber > 1) {
                                (splitIndexInTransformed - 1).coerceAtLeast(0)
                            } else {
                                splitIndexInTransformed
                            }
                            onEnter(normalizedText, splitIndex)
                            return@BasicTextField
                        }

                        // Detect deletion of the leading dummy space (Leading space workaround)
                        // All non-first lines have a space at index 0 to reliably capture Backspace.
                        // If it's missing, the user pressed Backspace at the start of the line.
                        if (lineNumber > 1 && !filteredText.startsWith(" ")) {
                            if (!backspaceHandled) {
                                backspaceHandled = true
                                if (filteredText.isEmpty()) {
                                    onDelete() // Line was empty, just delete it
                                } else {
                                    onMergeWithPrevious() // Line has content, merge with previous
                                }
                            }
                            return@BasicTextField
                        }

                        // Strip dummy space from input before updating the ViewModel to keep data clean
                        val actualText = if (lineNumber > 1 && filteredText.startsWith(" ")) {
                            filteredText.substring(1)
                        } else {
                            filteredText
                        }

                        if (actualText != line.expression) {
                            lastSentExpression = actualText
                            lastSentVersion++
                            onValueChange(actualText, lastSentVersion)
                        }

                        textFieldValue = newValue
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .focusRequester(focusRequester)
                        .onFocusChanged { focusState ->
                            isFocused = focusState.isFocused
                            if (focusState.isFocused) {
                                onFocused()
                            } else {
                                onBlur()
                            }
                        }
                        .onKeyEvent { keyEvent ->
                            // Only handle KEY_DOWN to avoid double triggering
                            if (keyEvent.type == KeyEventType.KeyDown) {
                                when (keyEvent.key) {
                                    Key.DirectionUp -> {
                                        // Only navigate if cursor is at the start (pos 0 on line 1, pos <= 1 on others)
                                        val atStart = if (lineNumber == 1) {
                                            textFieldValue.selection.start == 0
                                        } else {
                                            textFieldValue.selection.start <= 1
                                        }
                                        if (atStart && !shouldFocus) {
                                            onNavigateUp()
                                            true // Consume the event
                                        } else {
                                            false
                                        }
                                    }

                                    Key.DirectionDown -> {
                                        // Only navigate if cursor is at the end
                                        if (textFieldValue.selection.start >= textFieldValue.text.length && !shouldFocus) {
                                            onNavigateDown()
                                            true // Consume the event
                                        } else {
                                            false
                                        }
                                    }

                                    Key.Backspace -> {
                                        // Detect backspace at the very beginning of the expression
                                        // On line 1, this is pos 0. On other lines, pos 1 is the actual start
                                        // because index 0 is the hidden dummy space.
                                        val atStart = if (lineNumber == 1) {
                                            textFieldValue.selection.start == 0
                                        } else {
                                            textFieldValue.selection.start <= 1
                                        }
                                        if (atStart && lineNumber > 1) {
                                            if (!backspaceHandled) {
                                                backspaceHandled = true
                                                if (textFieldValue.text.removePrefix(" ").isEmpty()) {
                                                    onDelete()
                                                } else {
                                                    onMergeWithPrevious()
                                                }
                                            }
                                            true // Consume the event to prevent default backspace behavior
                                        } else {
                                            false
                                        }
                                    }

                                    else -> false
                                }
                            } else {
                                false
                            }
                        },
                    textStyle = MaterialTheme.typography.bodyLarge.copy(
                        color = MaterialTheme.colorScheme.onSurface,
                        fontFamily = FiraCodeFamily,
                        fontSize = editorFontSize.sp,
                        lineHeight = (editorFontSize * 1.35f).sp
                    ),
                    cursorBrush = SolidColor(MaterialTheme.colorScheme.onSurface),
                    visualTransformation = syntaxHighlightingTransformation,
                    keyboardOptions = KeyboardOptions(imeAction = ImeAction.Default),
                    keyboardActions = KeyboardActions(onDone = {
                        val actualText = if (lineNumber > 1) textFieldValue.text.removePrefix(" ") else textFieldValue.text
                        val cursorInActual = if (lineNumber > 1) (textFieldValue.selection.start - 1).coerceAtLeast(0) else textFieldValue.selection.start
                        onEnter(actualText, cursorInActual)
                    }),
                    onTextLayout = { textLayoutResult = it },
                    decorationBox = { innerTextField ->
                        if (textFieldValue.text.trim().isEmpty() && lineNumber == 1) {
                            Text(
                                "Type here...",
                                style = MaterialTheme.typography.bodyLarge.copy(
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                    fontFamily = FiraCodeFamily,
                                    fontSize = editorFontSize.sp,
                                    lineHeight = (editorFontSize * 1.35f).sp
                                )
                            )
                        }
                        innerTextField()
                    }
                )

                // Autocomplete suggestions dropdown
                SuggestionPopup(
                    suggestions = suggestions,
                    isFocused = isFocused,
                    forceDismissSuggestions = forceDismissSuggestions,
                    onDismissSuggestions = { forceDismissSuggestions = true },
                    isNonFirstLine = lineNumber > 1,
                    textFieldValue = textFieldValue,
                    onTextFieldValueChange = { textFieldValue = it },
                    onValueChange = { newText ->
                        lastSentExpression = newText
                        lastSentVersion++
                        onValueChange(newText, lastSentVersion)
                    },
                    textLayoutResult = textLayoutResult,
                    boxPosition = boxPosition,
                    keywordColor = keywordColor,
                    functionColor = functionColor,
                    variableColor = variableColor,
                    conversionColor = conversionColor,
                    replaceStart = suggestionContext.replaceStart,
                    argumentIndex = suggestionContext.argumentIndex,
                    needsSpace = suggestionContext.needsSpace,
                    bottomOffset = bottomOffset
                )
            }
        }


        // Result
        Box(
            modifier = Modifier
                .width(120.dp)
                .fillMaxHeight()
                .background(MaterialTheme.colorScheme.surfaceVariant)
                .padding(horizontal = 12.dp, vertical = 10.dp),
            contentAlignment = Alignment.TopEnd
        ) {
            val isError = line.result == "Err"
            val resultColor = if (isError) {
                MaterialTheme.colorScheme.error
            } else {
                ResultSuccess
            }

            var showTooltip by remember(line.id) { mutableStateOf(false) }
            var showCopiedTooltip by remember(line.id) { mutableStateOf(false) }
            var errorMessage by remember(line.id) { mutableStateOf<String?>(null) }

            LaunchedEffect(showCopiedTooltip) {
                if (showCopiedTooltip) {
                    delay(1000)
                    showCopiedTooltip = false
                }
            }

            LaunchedEffect(line.id, line.expression, isError, showTooltip) {
                if (!isError) {
                    // Reset state if line is no longer an error to prevent "double-tap"
                    // requirement if it becomes an error again later.
                    showTooltip = false
                    errorMessage = null
                } else if (showTooltip) {
                    // Real-time refresh: Re-fetch error details if the expression
                    // changes while the tooltip is already open.
                    errorMessage = null
                    errorMessage = try {
                        onGetErrorMessage(line.id)
                    } catch (e: Exception) {
                        "Couldn't load error details"
                    } ?: "Unknown error"
                }
            }

            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.End,
                modifier = (if (isError) {
                    Modifier
                        .clickable {
                            showTooltip = !showTooltip
                            if (!showTooltip) {
                                errorMessage = null
                            }
                        }
                } else {
                    Modifier.clickable {
                        if (line.result.isNotEmpty()) {
                            // Copying always uses the standard rounded version without ellipsis
                            onCopyResult(formatResult(line.result, precision, regionCode, groupingSeparatorEnabled, showEllipsis = false))
                            showCopiedTooltip = true
                        }
                    }
                }).fillMaxSize()
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = if (isError) Modifier.drawWithContent {
                        drawContent()
                        val stroke = Stroke(
                            width = 1.dp.toPx(),
                            pathEffect = PathEffect.dashPathEffect(floatArrayOf(4.dp.toPx(), 4.dp.toPx()), 0f)
                        )
                        val y = size.height
                        drawLine(
                            color = resultColor,
                            start = Offset(0f, y),
                            end = Offset(size.width, y),
                            strokeWidth = 1.dp.toPx(),
                            pathEffect = stroke.pathEffect
                        )
                    } else Modifier
                ) {
                    if (isError) {
                        Icon(
                            imageVector = Icons.Outlined.Info,
                            contentDescription = "Error Info",
                            tint = resultColor,
                            modifier = Modifier.padding(end = 4.dp).height(16.dp).width(16.dp)
                        )
                    }
                    Text(
                        text = formatAnnotatedResult(
                            line.result,
                            precision,
                            regionCode,
                            groupingSeparatorEnabled,
                            showPrecisionEllipsis,
                            resultColor
                        ),
                        style = MaterialTheme.typography.bodyLarge.copy(
                            fontFamily = FiraCodeFamily,
                            fontWeight = FontWeight.Bold,
                            fontSize = editorFontSize.sp,
                            lineHeight = (editorFontSize * 1.35f).sp
                        ),
                        color = resultColor
                    )
                }
            }

            if (showTooltip && isError) {
                val density = LocalDensity.current
                val yOffset = with(density) { 32.dp.roundToPx() }
                val xOffset = with(density) { (-8).dp.roundToPx() }

                Popup(
                    alignment = Alignment.TopEnd,
                    offset = IntOffset(xOffset, yOffset),
                    onDismissRequest = { showTooltip = false }
                ) {
                    Box(
                        modifier = Modifier
                            .background(
                                color = MaterialTheme.colorScheme.error,
                                shape = MaterialTheme.shapes.medium
                            )
                            .wrapContentHeight()
                            .border(
                                1.dp,
                                MaterialTheme.colorScheme.onError.copy(alpha = 0.5f),
                                MaterialTheme.shapes.medium
                            )
                            .padding(12.dp)
                    ) {
                        Text(
                            text = (errorMessage ?: "Loading error details...").parseBackticks(),
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onError
                        )
                    }
                }
            }

            if (showCopiedTooltip && !isError) {
                val density = LocalDensity.current
                val yOffset = with(density) { (-56).dp.roundToPx() }

                Popup(
                    alignment = Alignment.TopCenter,
                    offset = IntOffset(0, yOffset)
                ) {
                    Box(
                        modifier = Modifier
                            .background(
                                color = MaterialTheme.colorScheme.inverseSurface,
                                shape = MaterialTheme.shapes.medium
                            )
                            .padding(12.dp)
                    ) {
                        Text(
                            text = "Copied!",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.inverseOnSurface
                        )
                    }
                }
            }
        }
    }
}

private fun String.parseBackticks(): AnnotatedString {
    return buildAnnotatedString {
        val parts = this@parseBackticks.split("`")
        for (i in parts.indices) {
            if (i % 2 == 1) {
                withStyle(
                    SpanStyle(
                        fontWeight = FontWeight.Normal,
                        fontFamily = FiraCodeFamily,
                        background = Color.White.copy(alpha = 0.15f)
                    )
                ) {
                    append(parts[i])
                }
            } else {
                append(parts[i])
            }
        }
    }
}