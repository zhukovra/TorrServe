package ru.yourok.torrserve.search

import java.util.*

data class TorrentInfo(
    val title: String,
    val size: String,
    val url: String,
    val added: Date? = null,
)