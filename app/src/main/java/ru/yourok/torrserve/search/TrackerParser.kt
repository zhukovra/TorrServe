package ru.yourok.torrserve.search

import java.io.InputStream

interface TrackerParser {
    fun parseTorrentPage(input: InputStream): TorrentInfoFull?
}