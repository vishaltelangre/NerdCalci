package com.vishaltelangre.nerdcalci.ui.calculator

import com.vishaltelangre.nerdcalci.core.Builtins
import com.vishaltelangre.nerdcalci.core.MathEngine
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
import androidx.compose.foundation.layout.ime
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentHeight
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
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.FormatListNumbered
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
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
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
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.TextRange
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.rememberTextMeasurer
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.text.input.OffsetMapping
import androidx.compose.ui.text.input.TransformedText
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.window.Popup
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.draw.drawWithContent
import androidx.compose.material.icons.outlined.Info
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.unit.IntOffset
import com.vishaltelangre.nerdcalci.core.Constants
import com.vishaltelangre.nerdcalci.data.local.entities.LineEntity
import com.vishaltelangre.nerdcalci.ui.components.DeleteFileDialog
import com.vishaltelangre.nerdcalci.ui.components.RenameFileDialog
import com.vishaltelangre.nerdcalci.ui.components.FileInfoDialog
import com.vishaltelangre.nerdcalci.ui.theme.FiraCodeFamily
import com.vishaltelangre.nerdcalci.utils.ExportUtils
import com.vishaltelangre.nerdcalci.utils.SyntaxUtils
import com.vishaltelangre.nerdcalci.utils.TokenType
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

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
    private val defaultColor: Color
) : VisualTransformation {
    override fun filter(text: AnnotatedString): TransformedText {
        return TransformedText(
            applySyntaxHighlighting(
                text.text,
                numberColor,
                variableColor,
                keywordColor,
                functionColor,
                operatorColor,
                percentColor,
                commentColor,
                defaultColor
            ),
            OffsetMapping.Identity
        )
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
    defaultColor: Color
): AnnotatedString {
    return buildAnnotatedString {
        val tokens = SyntaxUtils.parseSyntaxTokens(text)
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
                TokenType.Default -> defaultColor
            }
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
private fun extractSuggestions(lines: List<LineEntity>, upToSortOrder: Int): Set<Suggestion> {
    val suggestionMap = mutableMapOf<String, Suggestion>()

    // Defaults (Dynamic variables, constants, global functions)
    MathEngine.dynamicVariableNames.forEach { suggestionMap[it] = Suggestion(it, SuggestionType.DYNAMIC_VARIABLE) }
    Builtins.constantNames.forEach { suggestionMap[it] = Suggestion(it, SuggestionType.CONSTANT) }
    Builtins.functionNames.forEach { suggestionMap[it] = Suggestion(it, SuggestionType.GLOBAL_FUNCTION) }

    lines.filter { it.sortOrder < upToSortOrder }.forEach { line ->
        // Strip comments first
        val hashIndex = line.expression.indexOf('#')
        val exprWithoutComment = if (hashIndex >= 0) {
            line.expression.substring(0, hashIndex).trim()
        } else {
            line.expression
        }

        val varFuncNamePattern = Constants.VAR_FUNC_NAME_PATTERN.removePrefix("^").removeSuffix("$")
        // Match both `var =` and `f(...) =`
        val regex = Regex("""^\s*($varFuncNamePattern)(?:\s*\((.*?)\))?\s*=""")
        val matchResult = regex.find(exprWithoutComment)
        if (matchResult != null) {
            val name = matchResult.groupValues[1].trim()

            // If it matched the (...) part, it's a function
            val isFunction = exprWithoutComment.substring(matchResult.groupValues[1].length).trimStart().startsWith("(")

            if (isFunction) {
                suggestionMap[name] = Suggestion(name, SuggestionType.LOCAL_FUNCTION)
            } else {
                suggestionMap[name] = Suggestion(name, SuggestionType.VARIABLE)
            }
        }
    }
    return suggestionMap.values.toSet()
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
    onBack: () -> Unit,
    onHelp: () -> Unit,
    onNavigateToFile: (Long) -> Unit = {}
) {
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()
    val listState = rememberLazyListState()

    val handleBack = {
        viewModel.deleteFileIfEmptyAndRecent(fileId)
        onBack()
    }

    BackHandler(onBack = handleBack)
    val lines by viewModel.getLines(fileId).collectAsState(initial = emptyList())
    val precision by viewModel.precision.collectAsState()
    val files by viewModel.allFiles.collectAsState(initial = emptyList())
    val canUndoMap by viewModel.canUndo.collectAsState()
    val canRedoMap by viewModel.canRedo.collectAsState()
    val canUndo = canUndoMap[fileId] ?: false
    val canRedo = canRedoMap[fileId] ?: false
    val globalShowLineNumbers by viewModel.showLineNumbers.collectAsState()
    var localShowLineNumbers by rememberSaveable(fileId) { mutableStateOf<Boolean?>(null) }
    val effectiveShowLineNumbers = localShowLineNumbers ?: globalShowLineNumbers
    var showMenu by remember { mutableStateOf(false) }
    var showRenameDialog by remember { mutableStateOf(false) }
    var showClearConfirmDialog by remember { mutableStateOf(false) }
    var showDeleteConfirmDialog by remember { mutableStateOf(false) }
    var showInfoDialog by remember { mutableStateOf(false) }
    val currentFile = files.find { it.id == fileId }
    val fileName = currentFile?.name ?: "Editor"

    // Track which line should be focused and cursor position
    var focusLineId by remember { mutableStateOf<Long?>(null) }
    var focusCursorPosition by remember { mutableStateOf<Int?>(null) }

    // Track which line is currently focused by the user
    var currentlyFocusedLineId by remember { mutableStateOf<Long?>(null) }

    // Track when a new line is requested to be added (for auto-focus)
    var requestNewLineAfterSortOrder by remember { mutableStateOf<Int?>(null) }

    // Track toolbar text insertion requests (used for inserting symbols using custom keyboard shortcuts)
    var insertTextRequest by remember { mutableStateOf<Pair<Long, String>?>(null) }

    // Auto-focus newly created lines
    LaunchedEffect(lines.size, requestNewLineAfterSortOrder) {
        requestNewLineAfterSortOrder?.let { sortOrder ->
            // Find the line that was just created (empty line with sortOrder = sortOrder + 1)
            val newLine = lines.find { it.sortOrder == sortOrder + 1 && it.expression.isEmpty() }
            newLine?.let {
                focusLineId = it.id
                focusCursorPosition =
                    if (lines.indexOf(it) == 0) 0 else 1 // Line 1 has no leading space
                requestNewLineAfterSortOrder = null
            }
        }
    }

    // Check if keyboard is visible
    val density = LocalDensity.current
    val imeInsets = WindowInsets.ime
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

    Scaffold(
        modifier = Modifier
            .fillMaxSize()
            .imePadding(),
        topBar = {
            Column {
                TopAppBar(
                    title = {
                        Text(
                            text = fileName,
                            color = MaterialTheme.colorScheme.onSurface,
                            modifier = Modifier.clickable { showRenameDialog = true },
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
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
                        IconButton(
                            onClick = { viewModel.undo(fileId) },
                            enabled = canUndo
                        ) {
                            Icon(
                                Icons.AutoMirrored.Filled.Undo,
                                "Undo",
                                tint = if (canUndo) MaterialTheme.colorScheme.onSurface else MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }

                        IconButton(
                            onClick = { viewModel.redo(fileId) },
                            enabled = canRedo
                        ) {
                            Icon(
                                Icons.AutoMirrored.Filled.Redo,
                                "Redo",
                                tint = if (canRedo) MaterialTheme.colorScheme.onSurface else MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }

                        Box {
                            IconButton(onClick = { showMenu = true }) {
                                Icon(
                                    Icons.Default.MoreVert,
                                    "More options",
                                    tint = MaterialTheme.colorScheme.onSurface
                                )
                            }
                            DropdownMenu(
                                expanded = showMenu,
                                onDismissRequest = { showMenu = false }) {
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

                                DropdownMenuItem(
                                    text = { Text("Rename File") },
                                    leadingIcon = {
                                        Icon(
                                            Icons.Default.Edit,
                                            contentDescription = null
                                        )
                                    },
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
                                        viewModel.duplicateFile(fileId) { newFileId ->
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
                                            ExportUtils.exportAsPdf(context, safeFileName, lines)
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
                                            ExportUtils.exportAsImage(context, safeFileName, lines)
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
                                    onClick = {
                                        showMenu = false
                                        showClearConfirmDialog = true
                                    }
                                )
                                DropdownMenuItem(
                                    text = { Text("Delete File") },
                                    leadingIcon = {
                                        Icon(
                                            Icons.Default.Delete,
                                            contentDescription = null
                                        )
                                    },
                                    onClick = {
                                        showMenu = false
                                        showDeleteConfirmDialog = true
                                    }
                                )
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
            if (isKeyboardVisible) {
                Column {
                    HorizontalDivider(
                        thickness = 1.dp,
                        color = MaterialTheme.colorScheme.outlineVariant
                    )
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .horizontalScroll(rememberScrollState())
                            .background(MaterialTheme.colorScheme.surfaceVariant)
                            .padding(horizontal = 8.dp, vertical = 8.dp),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        val symbols = listOf(".", "+", "-", "×", "÷", "=", "%", "(", ")", "_", "#")
                        symbols.forEach { symbol ->
                            Surface(
                                onClick = {
                                    currentlyFocusedLineId?.let { lineId ->
                                        insertTextRequest = Pair(lineId, symbol)
                                    }
                                },
                                modifier = Modifier
                                    .width(44.dp)
                                    .height(40.dp),
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
                                        text = symbol,
                                        style = MaterialTheme.typography.bodyLarge.copy(
                                            fontFamily = FiraCodeFamily,
                                            fontWeight = FontWeight.Medium
                                        ),
                                        color = MaterialTheme.colorScheme.onSurface
                                    )
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
                        .background(MaterialTheme.colorScheme.surfaceVariant)
                )
            }

            // LazyColumn with lines
            LazyColumn(
                state = listState,
                modifier = Modifier.fillMaxSize()
            ) {
                itemsIndexed(lines, key = { _, line -> line.id }) { index, line ->

                    // Compute available variables for this line (only from previous lines)
                    val availableVariables = extractSuggestions(lines, line.sortOrder)

                    LineRow(
                        line = line,
                        lineNumber = index + 1,
                        showLineNumbers = effectiveShowLineNumbers,
                        precision = precision,
                        numberWidth = numberWidth,
                        availableVariables = availableVariables,
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
                        onValueChange = { newValue ->
                            viewModel.updateLine(line.copy(expression = newValue))
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
                        onEnter = {
                            requestNewLineAfterSortOrder = line.sortOrder
                            viewModel.addLine(fileId, line.sortOrder + 1)
                            // Scroll to the newly created line
                            coroutineScope.launch {
                                delay(100)
                                val newLineIndex = index + 1
                                if (newLineIndex < lines.size + 1) {
                                    listState.animateScrollToItem(newLineIndex)
                                }
                            }
                        },
                        onDelete = {
                            if (lines.size > 1) {
                                // Focus previous line immediately (no delay) to keep keyboard open
                                val prevIndex = index - 1
                                val prevLine = lines.getOrNull(prevIndex)
                                if (prevLine != null) {
                                    val prevLineNumber = prevIndex + 1
                                    focusLineId = prevLine.id
                                    // For empty lines: line 1 has no leading space (pos 0), others at pos 1
                                    focusCursorPosition = if (prevLine.expression.isEmpty()) {
                                        if (prevLineNumber == 1) 0 else 1
                                    } else {
                                        prevLine.expression.length
                                    }
                                }
                                viewModel.deleteLine(line)
                            }
                        },
                        onNavigateUp = {
                            if (index > 0) {
                                val prevIndex = index - 1
                                val prevLine = lines[prevIndex]
                                val prevLineNumber = prevIndex + 1
                                focusLineId = prevLine.id
                                // For empty lines: line 1 has no leading space (pos 0), others at pos 1
                                focusCursorPosition = if (prevLine.expression.isEmpty()) {
                                    if (prevLineNumber == 1) 0 else 1
                                } else {
                                    prevLine.expression.length
                                }
                            }
                        },
                        onNavigateDown = {
                            if (index < lines.size - 1) {
                                val nextIndex = index + 1
                                val nextLine = lines[nextIndex]
                                val nextLineNumber = nextIndex + 1
                                focusLineId = nextLine.id
                                // For empty lines: line 1 has no leading space (pos 0), others at pos 1
                                focusCursorPosition = if (nextLine.expression.isEmpty()) {
                                    if (nextLineNumber == 1) 0 else 1
                                } else {
                                    nextLine.expression.length
                                }
                            }
                        },
                        onGetErrorMessage = { lineId -> viewModel.getLineErrorMessage(fileId, lineId) }
                    )
                    if (index < lines.size - 1) {
                        HorizontalDivider(
                            thickness = 1.dp,
                            color = MaterialTheme.colorScheme.outlineVariant
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
                viewModel.renameFile(fileId, newName.take(Constants.MAX_FILE_NAME_LENGTH))
                showRenameDialog = false
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
                showDeleteConfirmDialog = false
                viewModel.deleteFile(fileId)
                onBack()
            }
        )
    }
}

@Composable
private fun LineRow(
    line: LineEntity,
    lineNumber: Int,
    showLineNumbers: Boolean,
    precision: Int,
    numberWidth: Dp,
    availableVariables: Set<Suggestion>,
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
    onFocused: () -> Unit,
    onBlur: () -> Unit,
    onValueChange: (String) -> Unit,
    onEnter: () -> Unit,
    onDelete: () -> Unit,
    onNavigateUp: () -> Unit,
    onNavigateDown: () -> Unit,
    onGetErrorMessage: suspend (Long) -> String? = { null }
) {
    // Add leading space for backspace detection trick (but not for first line)
    val displayText = if (line.expression.isEmpty() && lineNumber > 1) " " else line.expression
    val defaultTextColor = MaterialTheme.colorScheme.onSurface

    var textFieldValue by remember(line.id) {
        mutableStateOf(TextFieldValue(text = displayText))
    }

    val syntaxHighlightingTransformation = remember(
        numberColor, variableColor, keywordColor, functionColor,
        operatorColor, percentColor, commentColor, defaultTextColor
    ) {
        SyntaxHighlightingTransformation(
            numberColor, variableColor, keywordColor, functionColor,
            operatorColor, percentColor, commentColor, defaultTextColor
        )
    }
    var previousSelection by remember { mutableStateOf(textFieldValue.selection) }
    var isFocused by remember { mutableStateOf(false) }
    val focusRequester = remember { FocusRequester() }

    // Autocomplete suggestions
    val currentWord = remember(textFieldValue.text, textFieldValue.selection) {
        val cursorPos = textFieldValue.selection.start
        val text = textFieldValue.text
        if (cursorPos > 0) {
            // Check if cursor is inside a comment (after #)
            val beforeCursor = text.substring(0, cursorPos)
            val hashIndex = beforeCursor.indexOf('#')
            if (hashIndex >= 0) {
                // Cursor is inside a comment, don't suggest
                ""
            } else {
                // Find the word before cursor
                val wordStart = beforeCursor.lastIndexOfAny(
                    charArrayOf(
                        ' ',
                        '+',
                        '-',
                        '*',
                        '/',
                        '×',
                        '÷',
                        '(',
                        ')',
                        '=',
                        ','
                    )
                ) + 1
                beforeCursor.substring(wordStart)
            }
        } else ""
    }

    val suggestions = remember(currentWord, availableVariables) {
        if (currentWord.isNotEmpty() && currentWord.all { it.isLetterOrDigit() || it == '_' }) {
            availableVariables.filter {
                it.name.startsWith(currentWord, ignoreCase = true) && it.name != currentWord
            }.sortedBy { it.name }
        } else emptyList()
    }

    // Sync with database updates
    LaunchedEffect(line.expression, lineNumber) {
        if (textFieldValue.text != displayText) {
            val selection = TextRange(
                textFieldValue.selection.start.coerceIn(0, displayText.length),
                textFieldValue.selection.end.coerceIn(0, displayText.length)
            )
            textFieldValue = textFieldValue.copy(
                text = displayText,
                selection = selection
            )
        }
    }

    // Handle programmatic focus requests (from navigation or deletion)
    LaunchedEffect(shouldFocus, focusCursorPos) {
        if (shouldFocus && focusCursorPos != null) {
            focusRequester.requestFocus()

            // Set cursor position - focusCursorPos is the desired position in the expression
            // We need to map this to the displayText position
            val actualPos = if (line.expression.isEmpty()) {
                // Line 1 has no leading space, others have space at position 0
                if (lineNumber == 1) 0 else 1
            } else {
                focusCursorPos.coerceIn(0, textFieldValue.text.length) // Ensure within bounds
            }

            textFieldValue = textFieldValue.copy(
                selection = TextRange(actualPos)
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
                onValueChange(trimmedText)
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
        Box(
            modifier = Modifier
                .weight(1f)
                .padding(horizontal = 10.dp, vertical = 10.dp)
        ) {
            Column {
                BasicTextField(
                    value = textFieldValue,
                    onValueChange = { newValue ->
                        val filteredText = newValue.text.replace("\n", "")

                        // Handle Enter key
                        if (newValue.text.contains("\n")) {
                            onEnter()
                            return@BasicTextField
                        }

                        // Detect backspace deleting the leading space (empty line deletion)
                        // This fixes an Android/Compose issue where we couldn't easily tell
                        // if the user pressed backspace on an empty line.
                        // This *hack* ensures there's always a dummy space on empty lines.
                        // When that space is deleted, AND the line was already empty,
                        // then we know the user is actually trying to delete the line itself.
                        if (filteredText.isEmpty() && line.expression.isEmpty() && lineNumber > 1) {
                            onDelete()
                            return@BasicTextField
                        }

                        // Strip leading space if user added real content
                        val actualText =
                            if (filteredText.startsWith(" ") && filteredText.length > 1) {
                                filteredText.substring(1)
                            } else if (filteredText == " ") {
                                "" // Just the space, treat as empty
                            } else {
                                filteredText
                            }

                        // Re-add leading space if text becomes empty (but not for line 1)
                        val displayText =
                            if (actualText.isEmpty() && lineNumber > 1) " " else actualText

                        var newSelection = newValue.selection
                        if (actualText.isEmpty() && line.expression.isNotEmpty() && lineNumber > 1) {
                            newSelection = TextRange(1)
                        } else if (actualText.isNotEmpty() && line.expression.isEmpty() && lineNumber > 1) {
                            newSelection = TextRange(
                                maxOf(0, newValue.selection.start - 1),
                                maxOf(0, newValue.selection.end - 1)
                            )
                        }

                        previousSelection = newSelection
                        textFieldValue = newValue.copy(
                            text = displayText,
                            selection = newSelection
                        )
                        onValueChange(actualText)
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
                                        // Only navigate if cursor is at the start
                                        // Line 1: position 0, Others: position 0 or 1 (before/on leading space)
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

                                    else -> false
                                }
                            } else {
                                false
                            }
                        },
                    textStyle = MaterialTheme.typography.bodyLarge.copy(
                        color = MaterialTheme.colorScheme.onSurface,
                        fontFamily = FiraCodeFamily
                    ),
                    cursorBrush = SolidColor(MaterialTheme.colorScheme.onSurface),
                    visualTransformation = syntaxHighlightingTransformation,
                    keyboardOptions = KeyboardOptions(imeAction = ImeAction.Done),
                    keyboardActions = KeyboardActions(onDone = { onEnter() }),
                    decorationBox = { innerTextField ->
                        if (textFieldValue.text.trim().isEmpty() && lineNumber == 1) {
                            Text(
                                "Type here...",
                                style = MaterialTheme.typography.bodyLarge.copy(
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                    fontFamily = FiraCodeFamily
                                )
                            )
                        }
                        innerTextField()
                    }
                )

                // Autocomplete suggestions dropdown
                if (suggestions.isNotEmpty() && isFocused) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(MaterialTheme.colorScheme.surfaceVariant)
                            .padding(vertical = 4.dp)
                    ) {
                        suggestions.take(5).forEach { suggestion ->
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clickable {
                                        // Replace current word with suggestion
                                        val cursorPos = textFieldValue.selection.start
                                        val text = textFieldValue.text
                                        val beforeCursor = text.substring(0, cursorPos)
                                        val wordStart = beforeCursor.lastIndexOfAny(
                                            charArrayOf(
                                                ' ', '+', '-', '*', '/', '×', '÷', '(', ')', '=', ','
                                            )
                                        ) + 1
                                        val replacementText = when (suggestion.type) {
                                            SuggestionType.LOCAL_FUNCTION, SuggestionType.GLOBAL_FUNCTION -> "${suggestion.name}()"
                                            else -> suggestion.name
                                        }
                                        val newText = text.substring(
                                            0,
                                            wordStart
                                        ) + replacementText + text.substring(cursorPos)
                                        val newCursorPos = wordStart + replacementText.length - if (replacementText.endsWith("()")) 1 else 0

                                        textFieldValue = textFieldValue.copy(
                                            text = newText,
                                            selection = TextRange(newCursorPos)
                                        )
                                        onValueChange(newText.trim())
                                    }
                                    .padding(horizontal = 8.dp, vertical = 6.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                val typeIcon = when (suggestion.type) {
                                    SuggestionType.LOCAL_FUNCTION -> "ƒ"
                                    SuggestionType.GLOBAL_FUNCTION -> "Gƒ"
                                    SuggestionType.DYNAMIC_VARIABLE -> "{X}"
                                    SuggestionType.CONSTANT -> "{C}"
                                    SuggestionType.VARIABLE -> "{x}"
                                }
                                val (itemColor, isItalic) = when (suggestion.type) {
                                    SuggestionType.DYNAMIC_VARIABLE -> keywordColor to true
                                    SuggestionType.LOCAL_FUNCTION, SuggestionType.GLOBAL_FUNCTION -> functionColor to true
                                    SuggestionType.VARIABLE, SuggestionType.CONSTANT -> variableColor to true
                                }
                                Text(
                                    text = typeIcon,
                                    style = MaterialTheme.typography.bodySmall.copy(
                                        fontFamily = FiraCodeFamily,
                                        fontStyle = if (isItalic) FontStyle.Italic else FontStyle.Normal
                                    ),
                                    color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f),
                                    modifier = Modifier.width(28.dp)
                                )
                                Text(
                                    text = suggestion.name,
                                    style = MaterialTheme.typography.bodyMedium.copy(
                                        fontFamily = FiraCodeFamily,
                                        fontStyle = if (isItalic) FontStyle.Italic else FontStyle.Normal
                                    ),
                                    color = itemColor
                                )
                            }
                        }
                    }
                }
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
                com.vishaltelangre.nerdcalci.ui.theme.ResultSuccess
            }

            var showTooltip by remember(line.id) { mutableStateOf(false) }
            var errorMessage by remember(line.id) { mutableStateOf<String?>(null) }

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
                modifier = if (isError) {
                    Modifier
                        .clickable {
                            showTooltip = !showTooltip
                            if (!showTooltip) {
                                errorMessage = null
                            }
                        }
                        .padding(bottom = 2.dp)
                        .drawWithContent {
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
                        }
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
                    text = MathEngine.formatDisplayResult(line.result, precision),
                    style = MaterialTheme.typography.bodyLarge.copy(
                        fontFamily = FiraCodeFamily,
                        fontWeight = FontWeight.Bold
                    ),
                    color = resultColor
                )
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
                            text = errorMessage ?: "Loading error details...",
                            style = MaterialTheme.typography.bodyMedium.copy(
                                fontFamily = FiraCodeFamily
                            ),
                            color = MaterialTheme.colorScheme.onError
                        )
                    }
                }
            }
        }
    }
}
