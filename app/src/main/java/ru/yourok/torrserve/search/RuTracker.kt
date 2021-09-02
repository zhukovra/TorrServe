package ru.yourok.torrserve.search

import org.jsoup.Jsoup
import java.io.InputStream
import java.lang.Exception
import java.nio.charset.Charset
import java.text.SimpleDateFormat
import java.util.*

// Rewrite Rutracker date "20-Сен-12" into "20 сент. 12"
private fun String.prepareMonth(): String {
    return this.lowercase().split("-").map {
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
    }.joinToString(" ")
}

/**
 * Parse Rutracker.org
 */
class RuTracker : TrackerParser {
    override fun parseSearchPage(input: InputStream): Pair<MutableList<TorrentInfo>, MutableList<Exception>> {
        val doc = parseInput(input)

        val results = mutableListOf<TorrentInfo>()
        val exceptions = mutableListOf<Exception>()
        val dateParser = SimpleDateFormat("dd MMM yy", Locale("ru"))

        doc.getElementsByTag("tbody")?.last()?.getElementsByTag("tr")?.forEach {
            try {
                val link = it.getElementsByTag("a").filter { l -> l.hasAttr("data-topic_id") }.first()
                val date = dateParser.parse(it.getElementsByTag("td").last().getElementsByTag("p").first().text().prepareMonth())
                results.add(
                    TorrentInfo(
                        link.text(),
                        it.getElementsByTag("a").filter { l -> l.hasClass("dl-stub") }.first().text().dropLast(2),
                        link.attr("href"),
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
}