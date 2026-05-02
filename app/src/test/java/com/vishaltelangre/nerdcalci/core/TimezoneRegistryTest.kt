package com.vishaltelangre.nerdcalci.core

import org.junit.Assert.*
import org.junit.Test
import java.time.ZoneId

class TimezoneRegistryTest {

    @Test
    fun `resolves common aliases`() {
        assertEquals(ZoneId.of("UTC"), TimezoneRegistry.resolve("UTC"))
        assertEquals(ZoneId.of("GMT"), TimezoneRegistry.resolve("GMT"))
        assertEquals(ZoneId.of("America/Los_Angeles"), TimezoneRegistry.resolve("PST"))
        assertEquals(ZoneId.of("America/Los_Angeles"), TimezoneRegistry.resolve("PDT"))
        assertEquals(ZoneId.of("Asia/Kolkata"), TimezoneRegistry.resolve("IST"))
    }

    @Test
    fun `resolves full IANA IDs`() {
        assertEquals(ZoneId.of("America/Chicago"), TimezoneRegistry.resolve("America/Chicago"))
        assertEquals(ZoneId.of("Europe/London"), TimezoneRegistry.resolve("Europe/London"))
        assertEquals(ZoneId.of("Asia/Tokyo"), TimezoneRegistry.resolve("Asia/Tokyo"))
    }

    @Test
    fun `resolves offset strings`() {
        assertEquals(ZoneId.of("+05:30"), TimezoneRegistry.resolve("+05:30"))
        assertEquals(ZoneId.of("-08:00"), TimezoneRegistry.resolve("-08:00"))
        assertEquals(ZoneId.of("+05:30"), TimezoneRegistry.resolve("GMT+530"))
        assertEquals(ZoneId.of("+05:30"), TimezoneRegistry.resolve("UTC+5:30"))
        assertEquals(ZoneId.of("GMT-05:00"), TimezoneRegistry.resolve("GMT-5"))
    }

    @Test
    fun `returns null for unknown zones`() {
        assertNull(TimezoneRegistry.resolve("NoSuch/Zone"))
        assertNull(TimezoneRegistry.resolve("BLORG"))
        assertNull(TimezoneRegistry.resolve("GMT+99:99"))
    }

    @Test
    fun `is case-sensitive for IANA IDs but aliases are mapped as-is`() {
        // ZoneId.of() is case-sensitive
        assertNull(TimezoneRegistry.resolve("america/chicago"))
        
        // Our registry aliases are uppercase
        assertNull(TimezoneRegistry.resolve("pst"))
        assertNotNull(TimezoneRegistry.resolve("PST"))
    }

    @Test
    fun `allSuggestions contains common aliases and IANA IDs`() {
        val suggestions = TimezoneRegistry.allSuggestions
        assertTrue(suggestions.contains("UTC"))
        assertTrue(suggestions.contains("PST"))
        assertTrue(suggestions.contains("America/New_York"))
        assertTrue(suggestions.contains("Asia/Kolkata"))
        
        // Check for alphabetical sorting of non-aliases or consistent order
        // Registry says: Aliases (sorted) + IANA (sorted)
        assertTrue(suggestions.indexOf("PST") < suggestions.indexOf("America/New_York"))
    }

    @Test
    fun `getFriendlyName prefers registry aliases`() {
        val ist = java.time.ZonedDateTime.now(ZoneId.of("Asia/Kolkata"))
        assertEquals("IST", TimezoneRegistry.getFriendlyName(ist))

        val pst = java.time.ZonedDateTime.of(2024, 1, 1, 12, 0, 0, 0, ZoneId.of("America/Los_Angeles"))
        assertEquals("PST", TimezoneRegistry.getFriendlyName(pst))

        val pdt = java.time.ZonedDateTime.of(2024, 7, 1, 12, 0, 0, 0, ZoneId.of("America/Los_Angeles"))
        assertEquals("PDT", TimezoneRegistry.getFriendlyName(pdt))
    }
}
