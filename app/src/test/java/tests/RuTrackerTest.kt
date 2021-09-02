package tests

import ru.yourok.torrserve.search.RuTracker
import ru.yourok.torrserve.search.TorrentInfo
import ru.yourok.torrserve.search.TorrentInfoFull
import java.text.SimpleDateFormat
import kotlin.test.Test
import kotlin.test.assertEquals

internal class RuTrackerTest {
    @Test
    fun testParseSearch() {
        val input = this::class.java.classLoader?.getResourceAsStream("rutracker.search.html")
        val actual = RuTracker().parseSearchPage(input!!)

        // check correct parser
        assertEquals(
            TorrentInfo(
                "Остров фантазий / Fantasy Island / Сезон: 1 / Серии: 1-4 из 10 (Адам Кэйн) [2021, США, Фэнтези, драма, детектив, приключения, WEBRip] MVO (HDRezka Studio)",
                "2.06 GB",
                "viewtopic.php?t=6094532",
                SimpleDateFormat("dd-MM-yyyy").parse("02-09-2021")
            ),
            actual.first[0]
        )

        // check empty exceptions
        assertEquals(0, actual.second.size)
    }

    @Test
    fun testParsePage() {
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