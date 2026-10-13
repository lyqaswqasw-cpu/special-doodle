package com.example.data.model

data class SeriesInfo(
    val name: String = "",
    val coverUrl: String = "",
    val plot: String = "",
    val cast: String = "",
    val rating: String = "",
    val director: String = "",
    val genre: String = "",
    val releaseDate: String = "",
    val seasons: List<Int> = emptyList(),
    val episodes: Map<Int, List<XtremeEpisode>> = emptyMap() // Maps season number to episodes
)

data class VodInfo(
    val name: String = "",
    val coverUrl: String = "",
    val plot: String = "",
    val cast: String = "",
    val rating: String = "",
    val director: String = "",
    val genre: String = "",
    val releaseDate: String = "",
    val streamUrl: String = "",
    val containerExtension: String = "mp4"
)

data class XtremeEpisode(
    val id: String,
    val title: String,
    val episodeNum: Int,
    val seasonNum: Int,
    val streamId: String,
    val streamIcon: String,
    val streamUrl: String,
    val containerExtension: String
)
