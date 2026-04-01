package com.vishaltelangre.nerdcalci.utils

import com.vishaltelangre.nerdcalci.data.local.entities.LineEntity
import java.io.ByteArrayInputStream
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test
import org.json.JSONObject

class FileUtilsTest {

    @Test
    fun `parseFileContent handles content without metadata`() {
        val content = "10 + 20\n30 * 40"
        val state = FileUtils.parseFileContent(content)

        assertEquals(listOf("10 + 20", "30 * 40"), state.expressions)
        assertFalse(state.metadata.isPinned)
        assertEquals(-1L, state.metadata.lastModified)
        assertEquals(-1L, state.metadata.createdAt)
    }

    @Test
    fun `parseFileContent parses full metadata correctly`() {
        val metadataJson = """{"version":2,"id":"sync-id","isPinned":true,"lastModified":123456789,"createdAt":987654321,"contentHash":"hash-123"}"""
        val content = "# @metadata $metadataJson\n10 + 20"
        val state = FileUtils.parseFileContent(content)

        assertEquals(listOf("10 + 20"), state.expressions)
        assertEquals(2, state.metadata.version)
        assertEquals("sync-id", state.metadata.id)
        assertTrue(state.metadata.isPinned)
        assertEquals(123456789L, state.metadata.lastModified)
        assertEquals(987654321L, state.metadata.createdAt)
        assertEquals("hash-123", state.metadata.contentHash)
    }

    @Test
    fun `parseFileContent handles partial metadata`() {
        // Only lastModified
        val metadataJson = """{"lastModified":123456789}"""
        val content = "# @metadata $metadataJson\n10 + 20"
        val state = FileUtils.parseFileContent(content)

        assertEquals(listOf("10 + 20"), state.expressions)
        assertFalse(state.metadata.isPinned) // Default
        assertEquals(123456789L, state.metadata.lastModified)
        assertEquals(-1L, state.metadata.createdAt) // Default
    }

    @Test
    fun `parseFileContent handles malformed metadata gracefully`() {
        val content = "# @metadata {invalid-json}\n10 + 20"
        val state = FileUtils.parseFileContent(content)

        assertEquals(listOf("10 + 20"), state.expressions)
        assertFalse(state.metadata.isPinned)
        assertEquals(-1L, state.metadata.lastModified)
    }

    @Test
    fun `parseFileContent falls back to defaults for wrong metadata types`() {
        val content = "# @metadata {\"isPinned\":\"yes\"}\n10 + 20\n30 * 40"
        val state = FileUtils.parseFileContent(content)

        assertEquals(listOf("10 + 20", "30 * 40"), state.expressions)
        assertFalse(state.metadata.isPinned)
        assertEquals(-1L, state.metadata.lastModified)
    }

    @Test
    fun `parseFileContent uses only first metadata header`() {
        val content = """
            # @metadata {"isPinned":true,"lastModified":123456789}
            # @metadata {"isPinned":false,"lastModified":987654321}
            10 + 20
            30 * 40
        """.trimIndent()
        val state = FileUtils.parseFileContent(content)

        assertEquals(listOf("10 + 20", "30 * 40"), state.expressions)
        assertTrue(state.metadata.isPinned)
        assertEquals(123456789L, state.metadata.lastModified)
    }

    @Test
    fun `formatPathForDisplayInternal handles null and blank`() {
        val root = "/storage/emulated/0"
        assertEquals("", FileUtils.formatPathForDisplayInternal(null, root))
        assertEquals("", FileUtils.formatPathForDisplayInternal("", root))
        assertEquals("", FileUtils.formatPathForDisplayInternal("   ", root))
    }

    @Test
    fun `formatPathForDisplayInternal handles secondary storage legacy paths`() {
        val path = "0123-4567:Documents/Backup"
        val root = "/storage/emulated/0"
        val expected = "/storage/0123-4567/Documents/Backup"
        assertEquals(expected, FileUtils.formatPathForDisplayInternal(path, root))
    }

    @Test
    fun `formatPathForDisplayInternal handles primary storage`() {
        val path = "primary:Documents/Backup"
        val root = "/storage/emulated/0"
        val expected = "/storage/emulated/0/Documents/Backup"
        assertEquals(expected, FileUtils.formatPathForDisplayInternal(path, root))
    }

    @Test
    fun `formatPathForDisplayInternal returns original path for unknown formats`() {
        val root = "/storage/emulated/0"
        val path = "/some/absolute/path"
        assertEquals(path, FileUtils.formatPathForDisplayInternal(path, root))

        val relative = "relative/path"
        assertEquals(relative, FileUtils.formatPathForDisplayInternal(relative, root))
    }

    @Test
    fun `calculateHash streams input without reading all text`() {
        val input = ByteArrayInputStream("streamed content".toByteArray())

        val hash = FileUtils.calculateHash(input)

        assertEquals("d9f93d83f082633feac23f4e3d5dea332ca698ba7b00dd6ef8a9e93bae65aa6b", hash)
    }

    @Test
    fun `parseFileContent strips various result formats`() {
        val content = """
            10 + 20 # {"result":"30"}
            1/2 + 1/2 # {"result":"1"}
            10/3 # {"result":"10/3"}
            1 trillion # {"result":"1,000,000,000,000"}
            1 lakh # {"result":"1,00,000"}
            5 km / 2 # {"result":"2.5 km"}
            4500 million in crores # {"result":"450 crores"}
            10 to hex # {"result":"0xA"}
            Err line # {"result":"Err"}
        """.trimIndent()

        val parsed = FileUtils.parseFileContent(content)

        val expected = listOf(
            "10 + 20",
            "1/2 + 1/2",
            "10/3",
            "1 trillion",
            "1 lakh",
            "5 km / 2",
            "4500 million in crores",
            "10 to hex",
            "Err line"
        )

        assertEquals(expected, parsed.expressions)
    }

    @Test
    fun `parseFileContent strips structured results and preserves comments`() {
        val content = """
            # @metadata {"version":1}
            1 trillion # {"result":"1,000,000,000,000"}
            a = 5 kilometers per hour/2 # My favorite unit # {"result":"2.5 kmh"}
            4500 million in crores # conversion # {"result":"450 crore"}
            10 + 20 # Result of 30 # {"result":"30"}
            # A manual comment # with a hash
            10 + 20 # 30 # Result of 30
        """.trimIndent()

        val parsed = FileUtils.parseFileContent(content)
        val expected = listOf(
            "1 trillion",
            "a = 5 kilometers per hour/2 # My favorite unit",
            "4500 million in crores # conversion",
            "10 + 20 # Result of 30",
            "# A manual comment # with a hash",
            "10 + 20 # 30 # Result of 30"
        )
        assertEquals(expected, parsed.expressions)
    }

    @Test
    fun `parseFileContent handles legacy result format fallback`() {
        // Test that older files with single hash results still parse correctly
        val content = """
            10 + 20 # 30
            a = 5 # 5
        """.trimIndent()

        val parsed = FileUtils.parseFileContent(content)
        assertEquals(listOf("10 + 20", "a = 5"), parsed.expressions)
    }

    @Test
    fun `formatFileBody uses structured JSON results`() {
        val lines = listOf(
            LineEntity(fileId = 1L, sortOrder = 0, expression = "10 + 20", result = "30"),
            LineEntity(fileId = 1L, sortOrder = 1, expression = "a = 5 # original comment", result = "5")
        )
        val formatted = FileUtils.formatFileBody(lines, 2)
        val expected = """
            10 + 20 # {"result":"30"}
            a = 5 # original comment # {"result":"5"}
        """.trimIndent()
        assertEquals(expected, formatted)
    }
}
