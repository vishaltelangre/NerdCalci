package com.vishaltelangre.nerdcalci.core

import com.vishaltelangre.nerdcalci.data.local.entities.LineEntity
import org.junit.Assert.assertEquals
import kotlinx.coroutines.runBlocking

/**
 * Shared helper to create a LineEntity for testing.
 */
fun createLine(expression: String, fileId: Long = 1L, sortOrder: Int = 0): LineEntity {
    return LineEntity(
        id = 0L,
        fileId = fileId,
        expression = expression,
        result = "",
        sortOrder = sortOrder
    )
}

/**
 * Shared helper to create multiple LineEntity objects for testing.
 */
fun createLines(vararg expressions: String, fileId: Long = 1L): List<LineEntity> {
    return expressions.mapIndexed { index, expr -> createLine(expr, fileId, index) }
}

/**
 * Shared helper to calculate results from multiple expressions.
 */
suspend fun testCalculate(
    vararg expressions: String,
    loader: FileContextLoader? = null,
    rationalMode: Boolean = false
): List<LineEntity> {
    val lines = createLines(*expressions)
    return MathEngine.calculate(lines, loader, rationalMode)
}

/**
 * Shared helper to calculate results and run assertions in a block.
 */
fun testCalculate(
    vararg expressions: String,
    loader: FileContextLoader? = null,
    rationalMode: Boolean = false,
    block: suspend (List<LineEntity>) -> kotlin.Unit
) = runBlocking {
    val results = testCalculate(*expressions, loader = loader, rationalMode = rationalMode)
    block(results)
}

/**
 * Shared helper to calculate results from multiple expressions starting from a specific index.
 */
fun testCalculateFrom(
    vararg expressions: String,
    changedIndex: Int,
    loader: FileContextLoader? = null,
    rationalMode: Boolean = false,
    block: suspend (List<LineEntity>) -> kotlin.Unit
) = runBlocking {
    val lines = createLines(*expressions)
    val results = MathEngine.calculateFrom(lines, changedIndex, loader, emptySet(), rationalMode)
    block(results)
}

/**
 * Shared helper to assert that a line has a specific error message.
 */
fun assertError(
    expectedMsg: String,
    line: LineEntity,
    allLines: List<LineEntity>,
    index: Int,
    loader: FileContextLoader? = null,
    loadingStack: Set<String> = emptySet(),
    rationalMode: Boolean = false
) = runBlocking {
    assertEquals("Err", line.result)
    val actualMsg = MathEngine.getErrorDetails(allLines, index, loader, loadingStack, rationalMode)
    assertEquals(expectedMsg, actualMsg)
}

/**
 * Overloaded assertError to work directly with calculation results.
 */
fun assertError(
    expectedMsg: String,
    results: List<LineEntity>,
    index: Int,
    loader: FileContextLoader? = null,
    loadingStack: Set<String> = emptySet(),
    rationalMode: Boolean = false
) {
    assertError(expectedMsg, results[index], results, index, loader, loadingStack, rationalMode)
}
