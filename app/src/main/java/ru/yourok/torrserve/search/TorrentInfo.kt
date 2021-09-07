package ru.yourok.torrserve.search

import java.util.*

data class TorrentInfo(
    val title: String,
    val size: String,
    val url: String,
    val seeds: Int = 0,
    val leeches: Int = 0,
    val magnet: String? = null,
    val added: Date? = null,
)