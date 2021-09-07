package ru.yourok.torrserve.search.parsers

import org.jsoup.Jsoup
import org.jsoup.nodes.Document
import ru.yourok.torrserve.search.TorrentInfo
import ru.yourok.torrserve.search.TorrentInfoFull
import ru.yourok.torrserve.search.TrackerParser
import java.io.InputStream
import java.lang.Exception
import java.nio.charset.Charset
import java.text.SimpleDateFormat
import java.util.*

/**
 * Parse Rutracker.org
 */
class RuTracker : TrackerParser {
    override fun parseSearchPage(doc: Document): Pair<MutableList<TorrentInfo>, MutableList<Exception>> {
        val results = mutableListOf<TorrentInfo>()
        val exceptions = mutableListOf<Exception>()
        val dateParser = SimpleDateFormat("dd MMM yy", Locale("ru"))

        doc.getElementsByTag("tbody")?.last()?.getElementsByTag("tr")?.forEach {
            try {
                val link = it.getElementsByTag("a").first { l -> l.hasAttr("data-topic_id") }
                val date = dateParser.parse(it.getElementsByTag("td").last().getElementsByTag("p").first().text().prepareMonth())
                val seeds = it.getElementsByClass("seedmed")?.first()?.text() ?: ""
                val leeches = it.getElementsByClass("leechmed")?.first()?.text() ?: ""
                results.add(
                    TorrentInfo(
                        link.text(),
                        it.getElementsByTag("a").first { l -> l.hasClass("dl-stub") }.text().dropLast(2),
                        link.attr("href"),
                        if (seeds.matches(Regex("^\\d+$"))) seeds.toInt() else 0,
                        if (leeches.matches(Regex("^\\d+$"))) leeches.toInt() else 0,
                        null,
                        date,
                    )
                )
            } catch (e: Exception) {
                exceptions.add(e)
            }
        }

        return Pair(results, exceptions)
    }

    override fun parseTorrentPage(input: InputStream): TorrentInfoFull? {
        val doc = parseInput(input)

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

    private fun parseInput(input: InputStream) = Jsoup.parse(input.bufferedReader(Charset.forName("Windows-1251")).use { it.readText() })

    // Rewrite Rutracker date "20-Сен-12" into "20 сент. 12"
    private fun String.prepareMonth(): String {
        return this.lowercase().split("-").joinToString(" ") {
            when (it) {
                "янв" -> "янв."
                "фев" -> "фев."
                "мар" -> "мар."
                "апр" -> "апр."
                "мая" -> "мая"
                "июн" -> "июн."
                "июл" -> "июл."
                "авг" -> "авг."
                "сен" -> "сент."
                "окт" -> "окт."
                "ноя" -> "нояб."
                "дек" -> "дек."
                else -> it
            }
        }
    }
}