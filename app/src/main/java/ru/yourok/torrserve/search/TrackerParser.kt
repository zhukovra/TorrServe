package ru.yourok.torrserve.search

import org.jsoup.nodes.Document
import java.io.InputStream
import java.lang.Exception

interface TrackerParser {
    fun parseSearchPage(doc: Document): Pair<MutableList<TorrentInfo>, MutableList<Exception>>
    fun parseTorrentPage(input: InputStream): TorrentInfoFull?
}