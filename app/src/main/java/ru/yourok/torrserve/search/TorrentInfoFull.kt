package ru.yourok.torrserve.search

data class TorrentInfoFull(
    val title: String,
    val magnet: String,
    val pictures: Set<String> = emptySet(),
    val seeds: Int? = null,
    val leeches: Int? = null,
)
