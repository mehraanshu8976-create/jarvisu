package com.example.jarves

import com.example.jarves.core.JarvisCommandEngine
import com.example.jarves.core.ParsedCommand
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class JarvisCommandEngineTest {

    @Test
    fun `parses flashlight commands accurately`() {
        assertEquals(ParsedCommand.SetFlashlight(true), JarvisCommandEngine.parse("turn flashlight on"))
        assertEquals(ParsedCommand.SetFlashlight(false), JarvisCommandEngine.parse("flashlight off"))
        assertEquals(ParsedCommand.ToggleFlashlight, JarvisCommandEngine.parse("toggle torch"))
    }

    @Test
    fun `parses youtube query accurately`() {
        val result = JarvisCommandEngine.parse("youtube kesariya song")
        assertTrue(result is ParsedCommand.SearchYouTube)
        assertEquals("kesariya song", (result as ParsedCommand.SearchYouTube).query)
    }

    @Test
    fun `parses diagnostics and settings`() {
        assertEquals(ParsedCommand.Diagnostics, JarvisCommandEngine.parse("check battery status"))
        assertEquals(ParsedCommand.OpenSettings, JarvisCommandEngine.parse("open settings"))
        assertEquals(ParsedCommand.OpenCamera, JarvisCommandEngine.parse("open camera"))
    }
}
