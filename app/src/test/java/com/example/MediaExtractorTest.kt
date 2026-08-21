package com.example

import com.example.data.service.MediaExtractorService
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Test

class MediaExtractorTest {

    @Test
    fun testYouTubeIdExtraction() {
        val url1 = "https://www.youtube.com/watch?v=dQw4w9WgXcQ"
        val id1 = MediaExtractorService.extractYouTubeId(url1)
        assertEquals("dQw4w9WgXcQ", id1)

        val url2 = "https://youtu.be/dQw4w9WgXcQ"
        val id2 = MediaExtractorService.extractYouTubeId(url2)
        assertEquals("dQw4w9WgXcQ", id2)
    }

    @Test
    fun testBanglaAudioTrackIsDefaultPriority() = runBlocking {
        val details = MediaExtractorService.extractMediaDetails("https://youtu.be/dQw4w9WgXcQ")
        assertNotNull(details)
        assertTrue(details.audioTracks.isNotEmpty())

        val defaultTrack = details.audioTracks.find { it.isDefaultSelected }
        assertNotNull(defaultTrack)
        assertTrue(defaultTrack!!.languageCode.startsWith("bn"))
        assertTrue(defaultTrack.languageName.contains("বাংলা"))
    }
}
