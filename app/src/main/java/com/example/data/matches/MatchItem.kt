package com.example.data.matches

import com.example.data.model.ChannelServer

enum class MatchStatus {
    LIVE,       // مباشر الآن
    SCHEDULED,  // قادمة / لم تبدأ
    FINISHED    // انتهت
}

data class MatchItem(
    val id: String,
    val leagueId: String,
    val leagueName: String,
    val leagueLogo: String = "",
    val homeTeamName: String,
    val homeTeamArabicName: String = "",
    val homeTeamLogo: String = "",
    val homeScore: String? = null,
    val awayTeamName: String,
    val awayTeamArabicName: String = "",
    val awayTeamLogo: String = "",
    val awayScore: String? = null,
    val status: MatchStatus = MatchStatus.SCHEDULED,
    val statusText: String = "",
    val minute: String = "",
    val startTimeIso: String = "",
    val startTimeMs: Long = 0L,
    val formattedTime: String = "",
    val venue: String = "",
    val broadcaster: String = "",
    val targetChannelId: String? = null,
    val targetChannelIds: List<String> = emptyList(),
    val servers: List<ChannelServer> = emptyList(),
    val isGulfCup: Boolean = false,
    val summaryEventId: String = "",
    val round: String = ""
) {
    val displayHomeName: String
        get() = homeTeamArabicName.ifBlank { homeTeamName }

    val displayAwayName: String
        get() = awayTeamArabicName.ifBlank { awayTeamName }

    val isLive: Boolean
        get() = status == MatchStatus.LIVE

    val hasBroadcast: Boolean
        get() = !targetChannelId.isNullOrBlank() || targetChannelIds.isNotEmpty() || servers.isNotEmpty()
}

data class MatchLeagueHeader(
    val id: String,
    val name: String,
    val logo: String = "",
    val isGulfCup: Boolean = false,
    val matches: List<MatchItem>
)

data class MatchEvent(
    val id: String,
    val type: String, // "goal", "yellow-card", "red-card", "substitution"
    val minute: String, // e.g. "57'"
    val text: String,
    val playerName: String = "",
    val assistName: String = "",
    val playerIn: String = "",
    val playerOut: String = "",
    val isHomeTeam: Boolean = true
)

data class MatchStatRow(
    val name: String,
    val homeValue: String,
    val awayValue: String,
    val homeProgress: Float = 0.5f // 0f to 1f for visual comparison bar
)

data class MatchPlayer(
    val id: String,
    val name: String,
    val jersey: String,
    val position: String,
    val isStarter: Boolean,
    val subbedIn: Boolean = false,
    val subbedOut: Boolean = false,
    val headshotUrl: String = ""
)

data class MatchTeamRoster(
    val teamName: String,
    val teamLogo: String,
    val formation: String,
    val starters: List<MatchPlayer>,
    val substitutes: List<MatchPlayer>
)

data class MatchSummaryDetail(
    val eventId: String,
    val events: List<MatchEvent>,
    val statistics: List<MatchStatRow>,
    val homeRoster: MatchTeamRoster?,
    val awayRoster: MatchTeamRoster?,
    val venue: String = "",
    val attendance: String = "",
    val referee: String = ""
)

