package tests

import ru.yourok.torrserve.search.Rutor
import ru.yourok.torrserve.search.TorrentInfoFull
import kotlin.test.Test
import kotlin.test.assertEquals

internal class RutorTest {
    @Test
    fun testParse() {
        val input = this::class.java.classLoader?.getResourceAsStream("rutor.torrent.html")
        val actual = Rutor().parseTorrentPage(input!!)

        assertEquals(
            TorrentInfoFull(
                "new-rutor.org :: Острые козырьки / Заточенные кепки / Peaky Blinders [S01] (2013) HDRip | AlexFilm",
                "magnet:?xt=urn:btih:142b6f18c801a72642a991af88f3b130a50477bf&dn=rutor.org_%D0%9E%D1%81%D1%82%D1%80%D1%8B%D0%B5+" +
                        "%D0%BA%D0%BE%D0%B7%D1%8B%D1%80%D1%8C%D0%BA%D0%B8+%2F+%D0%97%D0%B0%D1%82%D0%BE%D1%87%D0%B5%D0%BD%D0%BD%D" +
                        "1%8B%D0%B5+%D0%BA%D0%B5%D0%BF%D0%BA%D0%B8+%2F+Peaky+Blinders+%5BS01%5D+%282013%29+HDRip+%7C+AlexFilm&tr" +
                        "=udp://bt.rutor.org:2710&tr=retracker.local/announce",
                setOf(
                    "http://i59.fastpic.ru/big/2014/0313/44/3d34df5ab9b3b1724447c45d696d7444.jpg",
                    "http://i57.fastpic.ru/big/2014/0313/6a/f0331d69900bc750880c375a7d865f6a.png"
                ),
                1,
                2,
            ),
            actual
        )
    }
}