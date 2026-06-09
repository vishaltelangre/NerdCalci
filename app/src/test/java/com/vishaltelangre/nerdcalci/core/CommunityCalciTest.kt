package com.vishaltelangre.nerdcalci.core

import com.vishaltelangre.nerdcalci.data.local.entities.LineEntity
import java.io.File
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertNotEquals
import org.junit.Test
import org.junit.runner.RunWith
import org.junit.runners.Parameterized

@RunWith(Parameterized::class)
class CommunityCalciTest(
    private val calciFile: File,
) {

    companion object {
        private const val COMMUNITY_DIR = "community-submissions"

        private fun findCommunityDir(): File {
            val workingDir = File(System.getProperty("user.dir") ?: ".")

            val fromApp = File(workingDir, "../$COMMUNITY_DIR")
            if (fromApp.isDirectory) return fromApp.canonicalFile

            val fromRoot = File(workingDir, COMMUNITY_DIR)
            if (fromRoot.isDirectory) return fromRoot.canonicalFile

            error(
                "Cannot locate '$COMMUNITY_DIR/' directory. " +
                "Searched in '${workingDir.canonicalPath}' and its parent."
            )
        }

        @JvmStatic
        @Parameterized.Parameters(name = "{0}")
        fun calciFiles(): List<Array<Any>> {
            val dir = findCommunityDir()
            val files = dir.walkTopDown()
                .filter { it.isFile && it.extension == "nerdcalci" }
                .sortedBy { it.name }
                .toList()

            require(files.isNotEmpty()) {
                "No .nerdcalci files found in '${dir.canonicalPath}'. " +
                "Add at least one calci to the community-submissions/ directory."
            }

            return files.map { arrayOf(it) }
        }
    }

    @Test
    fun `every expression line in the calci produces no error`() = runBlocking {
        val rawLines = calciFile.readLines()

        val lines = rawLines.mapIndexed { index, expression ->
            LineEntity(
                id = 0L,
                fileId = 1L,
                sortOrder = index,
                expression = expression,
                result = "",
            )
        }

        val results = MathEngine.calculate(lines)

        results.forEachIndexed { index, resultLine ->
            val raw = rawLines[index].trim()
            val isBlankOrComment = raw.isEmpty() || raw.startsWith("#")
            if (isBlankOrComment) return@forEachIndexed

            assertNotEquals(
                "Line ${index + 1} of '${calciFile.name}' produced an error.\n" +
                "  Expression : ${rawLines[index]}\n" +
                "  Result     : ${resultLine.result}\n" +
                "  Error detail: ${
                    runBlocking {
                        MathEngine.getErrorDetails(lines, index)
                    } ?: "(no details)"
                }",
                "Err",
                resultLine.result,
            )
        }
    }
}
