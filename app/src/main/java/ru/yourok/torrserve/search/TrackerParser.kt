package ru.yourok.torrserve.search

import java.io.InputStream
import java.lang.Exception

interface TrackerParser {
    fun parseSearchPage(input: InputStream): Pair<MutableList<TorrentInfo>, MutableList<Exception>>
    fun parseTorrentPage(input: InputStream): TorrentInfoFull?
}