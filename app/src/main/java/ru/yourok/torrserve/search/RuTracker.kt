package ru.yourok.torrserve.search

import org.jsoup.Jsoup
import java.io.InputStream
import java.nio.charset.Charset

/**
 * Parse Rutracker.org torrent page
 */
class RuTracker : TrackerParser {
    override fun parseTorrentPage(input: InputStream): TorrentInfoFull? {
        val doc = Jsoup.parse(input.bufferedReader(Charset.forName("Windows-1251")).use { it.readText() })

        val title = doc.head().getElementsByTag("title")?.text()
        val magnet = doc.body().getElementsByTag("a")?.first { it.attr("href").startsWith("magnet:") }?.attr("href")

        return if (title == null || magnet == null)
            null
        else
            TorrentInfoFull(
                title,
                magnet,
                doc.body().getElementsByClass("postImg")
                    ?.filter { it -> it.hasClass("img-right") }
                    ?.mapNotNull { it.attr("title") }
                    ?.toSet() ?: emptySet(),
                doc.body().getElementsByClass("seed")?.first()?.getElementsByTag("b")?.first()?.text()?.toInt(),
                doc.body().getElementsByClass("leech")?.first()?.getElementsByTag("b")?.first()?.text()?.toInt(),
            )
    }
}