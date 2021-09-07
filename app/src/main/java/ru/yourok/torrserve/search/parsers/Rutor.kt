package ru.yourok.torrserve.search.parsers

import org.jsoup.Jsoup
import org.jsoup.nodes.Document
import ru.yourok.torrserve.search.TorrentInfo
import ru.yourok.torrserve.search.TorrentInfoFull
import ru.yourok.torrserve.search.TrackerParser
import java.io.InputStream
import java.lang.Exception
import java.text.SimpleDateFormat
import java.util.*

/**
 * Parse RuTor.org
 */
class Rutor : TrackerParser {
    override fun parseSearchPage(doc: Document): Pair<MutableList<TorrentInfo>, MutableList<Exception>> {
        val results = mutableListOf<TorrentInfo>()
        val exceptions = mutableListOf<Exception>()
        val dateParser = SimpleDateFormat("dd MMM yy", Locale("ru"))

        doc.body().getElementById("index")?.getElementsByTag("tr")?.forEach first@{
            // header
            if (it.hasClass("backgr")) return@first

            it.getElementsByTag("td")?.let { ti ->
                try {
                    val link = ti[1].getElementsByTag("a").last()
                    results.add(
                        TorrentInfo(
                            link.text(),
                            it.getElementsByTag("td").last { i -> i.attr("align") == "right" }.text(),
                            link.attr("href"),
                            it.getElementsByTag("td").last().getElementsByTag("span").first().text().toInt(),
                            it.getElementsByTag("td").last().getElementsByTag("span").last().text().toInt(),
                            it.getElementsByTag("a").first { t -> t.attr("href").startsWith("magnet:") }?.attr("href"),
                            dateParser.parse(ti[0].text().prepareMonth().lowercase()),
                        )
                    )
                } catch (ex: Exception) {
                    exceptions.add(ex)
                }
            }
        }

        return Pair(results, exceptions)
    }

    override fun parseTorrentPage(input: InputStream): TorrentInfoFull? {
        val doc = parseInput(input)

        val title = doc.head().getElementsByTag("title")?.text()
        val magnet = doc.body().getElementsByTag("a")?.first { it.attr("href").startsWith("magnet:") }?.attr("href")
        val seeds = doc.body().getElementsByClass("header")?.first { it.text() == "Раздают" }?.nextElementSibling()?.text()?.toInt()
        val leeches = doc.body().getElementsByClass("header")?.first { it.text() == "Качают" }?.nextElementSibling()?.text()?.toInt()
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

    private fun parseInput(input: InputStream) = Jsoup.parse(input.bufferedReader().use { it.readText() })

    // Rewrite Rutor date "20 Сен 12" into "20 сент. 12"
    private fun String.prepareMonth(): String {
        return this.lowercase().split(" ").joinToString(" ") {
            when (it) {
                "янв" -> "янв."
                "фев" -> "февр."
                "мар" -> "мар."
                "апр" -> "апр."
                "май" -> "мая"
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