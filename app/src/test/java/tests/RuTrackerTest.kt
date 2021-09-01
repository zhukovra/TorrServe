package tests

import ru.yourok.torrserve.search.RuTracker
import ru.yourok.torrserve.search.TorrentInfoFull
import kotlin.test.Test
import kotlin.test.assertEquals

internal class RuTrackerTest {
    @Test
    fun testParse() {
        val input = this::class.java.classLoader?.getResourceAsStream("rutracker.torrent.html")
        val actual = RuTracker().parseTorrentPage(input!!)

        val expected = TorrentInfoFull(
            "Острые козырьки / Peaky Blinders :: RuTracker.org",
            "magnet:?xt=urn:btih:F0F03AFD70F6289D8E6A2D146958A93EB697141C&tr=http%3A%2F%2Fbt4.t-ru.org%2Fann%3Fmagnet",
            setOf("https://d.radikal.ru/d27/1803/1d/d825423de7f2.png"),
            18,
            2,
        )
        assertEquals(expected, actual)
    }
}