package ru.yourok.torrserve.search

import org.jsoup.Jsoup
import java.io.InputStream

/**
 * Parse RuTor.org torrent page
 */
class Rutor : TrackerParser {
    override fun parseTorrentPage(input: InputStream): TorrentInfoFull? {
        val doc = Jsoup.parse(input.bufferedReader().use { it.readText() })

        val title = doc.head().getElementsByTag("title")?.text()
        val magnet = doc.body().getElementsByTag("a")?.first { it.attr("href").startsWith("magnet:") }?.attr("href")
        val seeds = doc.body().getElementsByClass("header")?.first{ it.text() == "Раздают" }?.nextElementSibling()?.text()?.toInt()
        val leeches = doc.body().getElementsByClass("header")?.first{ it.text() == "Качают" }?.nextElementSibling()?.text()?.toInt()
        val pictures = doc.body().getElementById("details")?.getElementsByTag("img")
            ?.filter { it.attr("src").startsWith("http") }
            ?.filter { it.attr("src").endsWith(".png") || it.attr("src").endsWith(".jpg") }
            ?.map { it.attr("src") }?.toSet()

        return if (title == null || magnet == null)
            null
        else TorrentInfoFull(
            title,
            magnet,
            pictures ?: emptySet(),
            seeds,
            leeches,
        )
    }
}