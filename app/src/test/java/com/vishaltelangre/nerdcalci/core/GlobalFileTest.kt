package com.vishaltelangre.nerdcalci.core

import com.vishaltelangre.nerdcalci.data.local.entities.LineEntity
import org.junit.Assert.assertEquals
import org.junit.Test

class GlobalFileTest {

    private fun createMockLoaderWithGlobal(
        globalExpressions: List<String>,
        otherExpressions: Map<String, List<String>> = emptyMap()
    ): FileContextLoader {
        return object : FileContextLoader {
            val cache = mutableMapOf<String, MathContext>()
            
            override suspend fun loadContext(fileName: String, loadingStack: Set<String>): MathContext? {
                cache[fileName]?.let { return it }
                val lines = if (fileName == Constants.GLOBAL_NAMESPACE) {
                    globalExpressions.mapIndexed { i, expr -> createLine(expr, 2L, i) }
                } else {
                    val exprs = otherExpressions[fileName] ?: return null
                    exprs.mapIndexed { i, expr -> createLine(expr, 3L, i) }
                }
                val context = MathEngine.buildVariableState(lines, this, loadingStack)
                cache[fileName] = context
                return context
            }
        }
    }

    @Test
    fun `global variable resolves successfully`() {
        val loader = createMockLoaderWithGlobal(listOf("x = 42"))
        testCalculate("global.x", loader = loader) { results ->
            assertEquals("42.0", results[0].result)
        }
    }

    @Test
    fun `global function resolves successfully`() {
        val loader = createMockLoaderWithGlobal(listOf("double(n) = n * 2"))
        testCalculate("global.double(10)", loader = loader) { results ->
            assertEquals("20.0", results[0].result)
        }
    }

    @Test
    fun `file function with global namespace resolves successfully`() {
        val loader = createMockLoaderWithGlobal(listOf("x = 100"))
        testCalculate("file(\"global\").x", loader = loader) { results ->
            assertEquals("100.0", results[0].result)
        }
    }

    @Test
    fun `using global namespace directly returns error`() {
        testCalculate("global") { results ->
            assertError("`global` is a reserved namespace. Use `global.varName` to access the global file's contents.", results, 0)
        }
    }

    @Test
    fun `cannot assign value to global namespace`() {
        testCalculate("global = 5") { results ->
            assertError("`global` is a reserved namespace and cannot be used as a variable or function name", results, 0)
        }
    }

    @Test
    fun `cannot define function named global`() {
        testCalculate("global(x) = x + 1") { results ->
            assertError("`global` is a reserved namespace and cannot be used as a variable or function name", results, 0)
        }
    }

    @Test
    fun `accessing global namespace inside function body is blocked`() {
        val loader = createMockLoaderWithGlobal(listOf("x = 42"))
        testCalculate("f(n) = global.x + n", "f(10)", loader = loader) { results ->
            // Calling f(10) should fail because f tries to resolve global.x
            assertError("Cannot access the global file inside a function body", results, 1, loader = loader)
        }
    }

    @Test
    fun `circular dependency with global file triggers loop error`() {
        // Global file references 'other.y'
        // Other file 'other' references 'global.x'
        val loader = createMockLoaderWithGlobal(
            globalExpressions = listOf(
                "other = file(\"other\")",
                "x = other.y + 1"
            ),
            otherExpressions = mapOf(
                "other" to listOf(
                    "glob = file(\"global\")",
                    "y = glob.x + 1"
                )
            )
        )
        testCalculate("global.x", loader = loader) { results ->
            assertError("File `other` also references file `global`, causing an endless loop", results, 0, loader = loader)
        }
    }

    @Test
    fun `global function with date argument resolves successfully`() {
        val loader = createMockLoaderWithGlobal(
            listOf("depositPerDay(cost, saved, due) = (cost - saved) / dropUnit(days until due)")
        )
        testCalculate(
            "cost = 1000",
            "saved = 500",
            "due = date(2026, 10, 31)",
            "global.depositPerDay(cost, saved, due)",
            loader = loader
        ) { results ->
            assertEquals("1000.0", results[0].result)
            assertEquals("500.0", results[1].result)
            assertEquals("Oct 31", results[2].result)
            val result = results[3].result.toDouble()
            // days until 2026-10-31 is > 0, so 500 / days produces a valid positive float
            org.junit.Assert.assertTrue(result > 0)
        }
    }

    @Test
    fun `file function with unit arguments resolves successfully`() {
        val loader = createMockLoaderWithGlobal(
            globalExpressions = emptyList(),
            otherExpressions = mapOf(
                "helper" to listOf(
                    "addWeights(a, b) = a + b"
                )
            )
        )
        testCalculate(
            "h = file(\"helper\")",
            "h.addWeights(5 kg, 500 g)",
            loader = loader
        ) { results ->
            assertEquals("5500.0 g", results[1].result)
        }
    }
}
