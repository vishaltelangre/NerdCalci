package com.vishaltelangre.nerdcalci

import java.io.File
import org.junit.Assert.assertTrue
import org.junit.Test

class FastlaneChangelogTest {

    companion object {
        const val MAX_CHANGELOG_LENGTH = 500
        private const val CHANGELOGS_DIR = "fastlane/metadata/android/en-US/changelogs"

        private fun findChangelogsDir(): File {
            val workingDir = File(System.getProperty("user.dir") ?: ".")
            val fromApp = File(workingDir, "../$CHANGELOGS_DIR")
            if (fromApp.isDirectory) return fromApp.canonicalFile

            val fromRoot = File(workingDir, CHANGELOGS_DIR)
            if (fromRoot.isDirectory) return fromRoot.canonicalFile

            error("Cannot locate '$CHANGELOGS_DIR' directory. Searched in '${workingDir.canonicalPath}' and its parent.")
        }
    }

    @Test
    fun `all fastlane changelogs do not exceed 500 characters`() {
        val dir = findChangelogsDir()
        val changelogs = dir.listFiles { file -> file.isFile && file.extension == "txt" }
            ?: emptyArray()

        assertTrue("Expected changelog files in ${dir.path}", changelogs.isNotEmpty())

        val violations = mutableListOf<String>()
        for (file in changelogs) {
            val charCount = file.readText().length
            if (charCount > MAX_CHANGELOG_LENGTH) {
                violations.add("${file.name}: $charCount chars (limit: $MAX_CHANGELOG_LENGTH)")
            }
        }

        assertTrue(
            "The following changelogs exceed the $MAX_CHANGELOG_LENGTH character limit:\n" +
                violations.joinToString("\n"),
            violations.isEmpty()
        )
    }

    @Test
    fun `changelog file names are valid integer version codes`() {
        val dir = findChangelogsDir()
        val changelogs = dir.listFiles { file -> file.isFile && file.extension == "txt" }
            ?: emptyArray()

        for (file in changelogs) {
            val baseName = file.nameWithoutExtension
            assertTrue(
                "Changelog file name '${file.name}' must be an integer versionCode",
                baseName.toIntOrNull() != null
            )
        }
    }
}
