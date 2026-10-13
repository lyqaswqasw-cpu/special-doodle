package com.example.data.matches

import android.util.Log
import com.example.data.model.ChannelServer
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.withContext
import okhttp3.ConnectionPool
import okhttp3.Dispatcher
import okhttp3.OkHttpClient
import okhttp3.Request
import org.json.JSONArray
import org.json.JSONObject
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.TimeZone
import java.util.concurrent.TimeUnit

class MatchRepository {

    companion object {
        private const val TAG = "MatchRepository"
        const val FIREBASE_RTDB_URL = "https://foul-ddbb8-default-rtdb.firebaseio.com"

        // Covered Leagues metadata
        val SUPPORTED_LEAGUES = listOf(
            LeagueInfo("eng.1", "الدوري الإنجليزي الممتاز", "https://a.espncdn.com/i/leaguelogos/soccer/500/23.png"),
            LeagueInfo("esp.1", "الدوري الإسباني", "https://a.espncdn.com/i/leaguelogos/soccer/500/15.png"),
            LeagueInfo("uefa.champions", "دوري أبطال أوروبا", "https://a.espncdn.com/i/leaguelogos/soccer/500/2.png"),
            LeagueInfo("ksa.1", "دوري روشن السعودي", "https://a.espncdn.com/i/leaguelogos/soccer/500/2660.png"),
            LeagueInfo("ita.1", "الدوري الإيطالي", "https://a.espncdn.com/i/leaguelogos/soccer/500/12.png"),
            LeagueInfo("ger.1", "الدوري الألماني", "https://a.espncdn.com/i/leaguelogos/soccer/500/10.png"),
            LeagueInfo("fra.1", "الدوري الفرنسي", "https://a.espncdn.com/i/leaguelogos/soccer/500/9.png"),
            LeagueInfo("afc.champions", "دوري أبطال آسيا", "https://a.espncdn.com/i/leaguelogos/soccer/500/2678.png")
        )
    }

    data class LeagueInfo(
        val id: String,
        val arabicName: String,
        val defaultLogo: String,
        val isGulfCup: Boolean = false
    )

    private val httpClient: OkHttpClient by lazy {
        val dispatcher = Dispatcher().apply {
            maxRequests = 32
            maxRequestsPerHost = 16
        }
        val pool = ConnectionPool(16, 5, TimeUnit.MINUTES)
        OkHttpClient.Builder()
            .dispatcher(dispatcher)
            .connectionPool(pool)
            .connectTimeout(15, TimeUnit.SECONDS)
            .readTimeout(20, TimeUnit.SECONDS)
            .followRedirects(true)
            .build()
    }

    private val database: FirebaseDatabase by lazy {
        try {
            FirebaseDatabase.getInstance(FIREBASE_RTDB_URL)
        } catch (_: Exception) {
            FirebaseDatabase.getInstance()
        }
    }

    private val eventsRef by lazy {
        database.getReference("v4_events").apply {
            try { keepSynced(true) } catch (_: Exception) {}
        }
    }

    /**
     * Listens to live real-time events published in Firebase (Events Engine).
     */
    fun getFirebaseEventsFlow(): Flow<List<MatchItem>> = callbackFlow {
        val listener = object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val list = mutableListOf<MatchItem>()
                for (child in snapshot.children) {
                    val key = child.key ?: continue
                    val value = child.value
                    if (value is Map<*, *>) {
                        @Suppress("UNCHECKED_CAST")
                        val match = parseFirebaseEvent(key, value as Map<String, Any?>)
                        if (match != null) {
                            if (com.example.data.security.AppSecurityGuard.isMaliciousText(match.homeTeamName) ||
                                com.example.data.security.AppSecurityGuard.isMaliciousText(match.awayTeamName) ||
                                com.example.data.security.AppSecurityGuard.isMaliciousText(match.leagueName)) {
                                Log.w(TAG, "Shield Action: Dropping and purging malicious match ($key)")
                                try { eventsRef.child(key).removeValue() } catch (_: Exception) {}
                                continue
                            }
                            list.add(match)
                        }
                    }
                }
                list.sortBy { it.startTimeMs }
                trySend(list)
            }

            override fun onCancelled(error: DatabaseError) {
                Log.e(TAG, "Firebase events listener cancelled: ${error.message}")
                trySend(emptyList())
            }
        }

        eventsRef.addValueEventListener(listener)
        awaitClose { eventsRef.removeEventListener(listener) }
    }

    /**
     * Parses a Firebase event node into a MatchItem.
     */
    private fun parseFirebaseEvent(id: String, map: Map<String, Any?>): MatchItem? {
        val name = map["name"] as? String ?: return null
        val eventType = map["eventType"] as? String ?: "match"
        val startTimeMs = (map["startTimeMs"] as? Number)?.toLong() ?: System.currentTimeMillis()
        val targetChannelId = map["targetChannelId"] as? String
        
        @Suppress("UNCHECKED_CAST")
        val targetChannelIds = (map["targetChannelIds"] as? List<String>) ?: emptyList()
        val imageUrl = map["imageUrl"] as? String ?: ""
        val broadcaster = map["broadcaster"] as? String ?: map["channelName"] as? String ?: ""
        val tournamentName = map["tournament"] as? String ?: map["league"] as? String ?: ""
        
        // Parse servers if available
        val serversList = mutableListOf<ChannelServer>()
        val rawServers = map["servers"]
        if (rawServers is List<*>) {
            for (item in rawServers) {
                if (item is Map<*, *>) {
                    @Suppress("UNCHECKED_CAST")
                    val sMap = item as Map<String, Any?>
                    val sId = sMap["id"] as? String ?: java.util.UUID.randomUUID().toString()
                    val sName = sMap["name"] as? String ?: "سيرفر البث"
                    val sUrl = sMap["streamUrl"] as? String ?: sMap["url"] as? String ?: ""
                    val sType = sMap["streamType"] as? String ?: "m3u8"
                    if (sUrl.isNotBlank()) {
                        serversList.add(ChannelServer(id = sId, name = sName, streamUrl = sUrl, streamType = sType))
                    }
                }
            }
        }

        // Split "Team A vs Team B" or "Team A ضد Team B"
        val splitDelimiters = listOf(" ضد ", " vs ", " VS ", " - ", " v ")
        var homeTeam = name
        var awayTeam = ""
        for (delim in splitDelimiters) {
            if (name.contains(delim)) {
                val parts = name.split(delim, limit = 2)
                homeTeam = parts[0].trim()
                awayTeam = parts.getOrNull(1)?.trim() ?: ""
                break
            }
        }

        val currentTime = System.currentTimeMillis()
        val status = when {
            currentTime >= startTimeMs && currentTime < startTimeMs + (105 * 60 * 1000) -> MatchStatus.LIVE
            currentTime >= startTimeMs + (105 * 60 * 1000) -> MatchStatus.FINISHED
            else -> MatchStatus.SCHEDULED
        }

        val statusText = when (status) {
            MatchStatus.LIVE -> "مباشر الآن"
            MatchStatus.FINISHED -> "انتهت"
            MatchStatus.SCHEDULED -> formatTime(startTimeMs)
        }

        val leagueDisplay = if (tournamentName.isNotBlank()) {
            tournamentName
        } else {
            "أحداث ومباريات البث المباشر"
        }

        return MatchItem(
            id = "firebase_$id",
            leagueId = "events",
            leagueName = leagueDisplay,
            leagueLogo = imageUrl.ifBlank { "https://a.espncdn.com/i/teamlogos/soccer/500/default-team-logo-500.png" },
            homeTeamName = homeTeam,
            homeTeamArabicName = homeTeam,
            homeTeamLogo = imageUrl,
            awayTeamName = awayTeam,
            awayTeamArabicName = awayTeam,
            awayTeamLogo = "",
            status = status,
            statusText = statusText,
            startTimeMs = startTimeMs,
            formattedTime = formatTime(startTimeMs),
            broadcaster = broadcaster,
            targetChannelId = targetChannelId,
            targetChannelIds = targetChannelIds,
            servers = serversList,
            isGulfCup = false
        )
    }

    /**
     * Fetches matches from ESPN for all covered leagues for a specific date (YYYYMMDD).
     */
    suspend fun fetchAllMatchesForDate(dateYyyyMmDd: String): List<MatchItem> = withContext(Dispatchers.IO) {
        coroutineScope {
            val deferreds = SUPPORTED_LEAGUES.map { league ->
                async {
                    try {
                        fetchLeagueScoreboard(league, dateYyyyMmDd)
                    } catch (e: Exception) {
                        Log.w(TAG, "Failed to fetch matches for ${league.id}: ${e.message}")
                        emptyList()
                    }
                }
            }
            deferreds.awaitAll().flatten()
        }
    }

    /**
     * Fetches scoreboard for a single league from ESPN Soccer API.
     */
    private fun fetchLeagueScoreboard(league: LeagueInfo, dateYyyyMmDd: String): List<MatchItem> {
        val url = "https://site.api.espn.com/apis/site/v2/sports/soccer/${league.id}/scoreboard?dates=$dateYyyyMmDd"
        val request = Request.Builder()
            .url(url)
            .header("User-Agent", "Mozilla/5.0 (Android; Mobile; rv:120.0)")
            .header("Accept", "application/json")
            .build()

        val response = httpClient.newCall(request).execute()
        if (!response.isSuccessful) {
            response.close()
            return emptyList()
        }

        val jsonString = response.body?.string() ?: return emptyList()
        return parseEspnScoreboard(jsonString, league)
    }

    /**
     * Parses ESPN scoreboard JSON.
     */
    private fun parseEspnScoreboard(jsonString: String, league: LeagueInfo): List<MatchItem> {
        val result = mutableListOf<MatchItem>()
        try {
            val root = JSONObject(jsonString)
            val eventsArray = root.optJSONArray("events") ?: return emptyList()

            // League logo from API if present
            var leagueLogo = league.defaultLogo
            val leaguesArr = root.optJSONArray("leagues")
            if (leaguesArr != null && leaguesArr.length() > 0) {
                val lObj = leaguesArr.getJSONObject(0)
                val logos = lObj.optJSONArray("logos")
                if (logos != null && logos.length() > 0) {
                    leagueLogo = logos.getJSONObject(0).optString("href", league.defaultLogo)
                }
            }

            for (i in 0 until eventsArray.length()) {
                val event = eventsArray.getJSONObject(i)
                val eventId = event.optString("id", "")
                val eventDate = event.optString("date", "")
                val competitions = event.optJSONArray("competitions") ?: continue
                if (competitions.length() == 0) continue

                val comp = competitions.getJSONObject(0)
                val venueObj = comp.optJSONObject("venue")
                val venueName = venueObj?.optString("fullName", "") ?: ""

                // Status
                val statusObj = comp.optJSONObject("status")
                val statusType = statusObj?.optJSONObject("type")
                val state = statusType?.optString("state", "pre") ?: "pre"
                val statusDetail = statusType?.optString("shortDetail", "") ?: ""
                val clock = statusObj?.optString("displayClock", "") ?: ""

                val matchStatus = when (state.lowercase()) {
                    "in" -> MatchStatus.LIVE
                    "post" -> MatchStatus.FINISHED
                    else -> MatchStatus.SCHEDULED
                }

                val statusText = when (matchStatus) {
                    MatchStatus.LIVE -> if (clock.isNotBlank()) clock else (if (statusDetail.isNotBlank()) statusDetail else "مباشر")
                    MatchStatus.FINISHED -> if (statusDetail.isNotBlank()) statusDetail else "انتهت"
                    MatchStatus.SCHEDULED -> formatIsoDateToTime(eventDate)
                }

                // Competitors (Teams)
                val competitors = comp.optJSONArray("competitors") ?: continue
                var homeName = ""
                var homeArabic = ""
                var homeLogo = ""
                var homeScore: String? = null

                var awayName = ""
                var awayArabic = ""
                var awayLogo = ""
                var awayScore: String? = null

                for (j in 0 until competitors.length()) {
                    val c = competitors.getJSONObject(j)
                    val homeAway = c.optString("homeAway", "home")
                    val score = c.optString("score", "")
                    val teamObj = c.optJSONObject("team")
                    val name = teamObj?.optString("displayName", "") ?: teamObj?.optString("name", "") ?: ""
                    val logo = teamObj?.optString("logo", "") ?: ""
                    val arabic = translateTeamName(name)

                    if (homeAway.equals("home", ignoreCase = true)) {
                        homeName = name
                        homeArabic = arabic
                        homeLogo = logo
                        homeScore = if (matchStatus != MatchStatus.SCHEDULED && score.isNotBlank()) score else null
                    } else {
                        awayName = name
                        awayArabic = arabic
                        awayLogo = logo
                        awayScore = if (matchStatus != MatchStatus.SCHEDULED && score.isNotBlank()) score else null
                    }
                }

                // Broadcaster / TV
                var broadcaster = ""
                val geoBroadcasts = comp.optJSONArray("geoBroadcasts")
                if (geoBroadcasts != null && geoBroadcasts.length() > 0) {
                    val b = geoBroadcasts.getJSONObject(0).optJSONObject("media")
                    broadcaster = b?.optString("shortName", "") ?: ""
                }
                if (broadcaster.isBlank()) {
                    val broadcasts = comp.optJSONArray("broadcasts")
                    if (broadcasts != null && broadcasts.length() > 0) {
                        val names = broadcasts.getJSONObject(0).optJSONArray("names")
                        if (names != null && names.length() > 0) {
                            broadcaster = names.optString(0, "")
                        }
                    }
                }

                val startMs = parseIsoDateToMillis(eventDate)

                val item = MatchItem(
                    id = "espn_$eventId",
                    leagueId = league.id,
                    leagueName = league.arabicName,
                    leagueLogo = leagueLogo,
                    homeTeamName = homeName,
                    homeTeamArabicName = homeArabic,
                    homeTeamLogo = homeLogo,
                    homeScore = homeScore,
                    awayTeamName = awayName,
                    awayTeamArabicName = awayArabic,
                    awayTeamLogo = awayLogo,
                    awayScore = awayScore,
                    status = matchStatus,
                    statusText = statusText,
                    minute = clock,
                    startTimeIso = eventDate,
                    startTimeMs = startMs,
                    formattedTime = formatIsoDateToTime(eventDate),
                    venue = venueName,
                    broadcaster = broadcaster,
                    isGulfCup = false,
                    summaryEventId = eventId
                )
                result.add(item)
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error parsing ESPN scoreboard: ${e.message}", e)
        }
        return result
    }

    /**
     * Checks if team is one of the Arabian Gulf Cup nations.
     */
    private fun isGulfNation(name: String): Boolean {
        val lower = name.lowercase()
        return lower.contains("iraq") || lower.contains("عراق") ||
                lower.contains("saudi") || lower.contains("سعودي") ||
                lower.contains("oman") || lower.contains("عمان") ||
                lower.contains("qatar") || lower.contains("قطر") ||
                lower.contains("kuwait") || lower.contains("كويت") ||
                lower.contains("bahrain") || lower.contains("بحرين") ||
                lower.contains("uae") || lower.contains("emirates") || lower.contains("إمارات") ||
                lower.contains("yemen") || lower.contains("يمن")
    }

    /**
     * Translates famous international and Arab team names into Arabic.
     */
    private fun translateTeamName(name: String): String {
        return TEAM_TRANSLATIONS[name]
            ?: ARAB_COUNTRY_TRANSLATIONS[name]
            ?: name
    }

    private fun formatTime(millis: Long): String {
        return try {
            val sdf = SimpleDateFormat("h:mm a", Locale("ar"))
            sdf.format(Date(millis))
        } catch (_: Exception) {
            ""
        }
    }

    private fun parseIsoDateToMillis(isoString: String): Long {
        if (isoString.isBlank()) return 0L
        return try {
            val sdf = SimpleDateFormat("yyyy-MM-dd'T'HH:mm'Z'", Locale.US).apply {
                timeZone = TimeZone.getTimeZone("UTC")
            }
            sdf.parse(isoString)?.time ?: 0L
        } catch (_: Exception) {
            0L
        }
    }

    private fun formatIsoDateToTime(isoString: String): String {
        val ms = parseIsoDateToMillis(isoString)
        if (ms == 0L) return ""
        return try {
            val sdf = SimpleDateFormat("h:mm a", Locale("ar"))
            sdf.format(Date(ms))
        } catch (_: Exception) {
            ""
        }
    }

    private val ARAB_COUNTRY_TRANSLATIONS = mapOf(
        "Iraq" to "العراق",
        "Saudi Arabia" to "السعودية",
        "Oman" to "عمان",
        "Qatar" to "قطر",
        "Kuwait" to "الكويت",
        "Bahrain" to "البحرين",
        "United Arab Emirates" to "الإمارات",
        "UAE" to "الإمارات",
        "Yemen" to "اليمن",
        "Egypt" to "مصر",
        "Morocco" to "المغرب",
        "Algeria" to "الجزائر",
        "Tunisia" to "تونس",
        "Jordan" to "الأردن",
        "Syria" to "سوريا",
        "Palestine" to "فلسطين",
        "Lebanon" to "لبنان"
    )

    private val TEAM_TRANSLATIONS = mapOf(
        // English Premier League
        "Arsenal" to "أرسنال",
        "Aston Villa" to "أستون فيلا",
        "Bournemouth" to "بورنموث",
        "AFC Bournemouth" to "بورنموث",
        "Brentford" to "برينتفورد",
        "Brighton & Hove Albion" to "برايتون",
        "Chelsea" to "تشيلسي",
        "Crystal Palace" to "كريستال بالاس",
        "Everton" to "إيفرتون",
        "Fulham" to "فولهام",
        "Ipswich Town" to "إيبسويتش تاون",
        "Leicester City" to "ليستر سيتي",
        "Liverpool" to "ليفربول",
        "Manchester City" to "مانشستر سيتي",
        "Manchester United" to "مانشستر يونايتد",
        "Newcastle United" to "نيوكاسل يونايتد",
        "Nottingham Forest" to "نوتينغهام فورست",
        "Southampton" to "ساوثهامبتون",
        "Tottenham Hotspur" to "توتنهام",
        "West Ham United" to "وست هام",
        "Wolverhampton Wanderers" to "وولفرهامبتون",

        // LaLiga
        "Real Madrid" to "ريال مدريد",
        "Barcelona" to "برشلونة",
        "Atletico Madrid" to "أتلتيكو مدريد",
        "Atlético Madrid" to "أتلتيكو مدريد",
        "Sevilla" to "إشبيلية",
        "Real Betis" to "ريال بيتيس",
        "Real Sociedad" to "ريال سوسيداد",
        "Villarreal" to "فياريال",
        "Athletic Club" to "أتلتيك بلباو",
        "Valencia" to "فالنسيا",
        "Girona" to "جيرونا",

        // Saudi Pro League
        "Al Hilal" to "الهلال",
        "Al-Hilal" to "الهلال",
        "Al Nassr" to "النصر",
        "Al-Nassr" to "النصر",
        "Al Ittihad" to "الاتحاد",
        "Al-Ittihad" to "الاتحاد",
        "Al Ahli" to "الأهلي السعودي",
        "Al-Ahli" to "الأهلي السعودي",
        "Al Shabab" to "الشباب",
        "Al-Shabab" to "الشباب",
        "Al Ettifaq" to "الاتفاق",
        "Al Taawoun" to "التعاون",

        // Serie A
        "Juventus" to "يوفنتوس",
        "AC Milan" to "ميلان",
        "Inter Milan" to "إنتر ميلان",
        "Napoli" to "نابولي",
        "AS Roma" to "روما",
        "Lazio" to "لاتسيو",
        "Atalanta" to "أتالانتا",

        // Bundesliga
        "Bayern Munich" to "بايرن ميونخ",
        "Borussia Dortmund" to "بوروسيا دورتموند",
        "Bayer Leverkusen" to "باير ليفركوزن",
        "RB Leipzig" to "لايبزيغ",

        // Ligue 1
        "Paris Saint-Germain" to "باريس سان جيرمان",
        "PSG" to "باريس سان جيرمان",
        "Marseille" to "مارسيليا",
        "Monaco" to "موناكو",
        "Lyon" to "ليون"
    )

    /**
     * Fetches real match summary details (lineups, events, statistics) from ESPN Soccer API:
     * https://site.api.espn.com/apis/site/v2/sports/soccer/{leagueId}/summary?event={eventId}
     */
    suspend fun fetchMatchSummary(leagueId: String, eventId: String): MatchSummaryDetail? = withContext(Dispatchers.IO) {
        val cleanEventId = eventId.removePrefix("espn_").removePrefix("firebase_").trim()
        if (cleanEventId.isBlank()) return@withContext null

        val targetLeague = if (leagueId.isNotBlank() && leagueId != "events" && leagueId != "gulf.cup") leagueId else "all"

        val url = "https://site.api.espn.com/apis/site/v2/sports/soccer/$targetLeague/summary?event=$cleanEventId"
        var json = executeGet(url)
        if (json.isNullOrBlank() && targetLeague != "all") {
            json = executeGet("https://site.api.espn.com/apis/site/v2/sports/soccer/all/summary?event=$cleanEventId")
        }
        if (json.isNullOrBlank()) return@withContext null

        parseEspnSummary(cleanEventId, json)
    }

    private fun executeGet(url: String): String? {
        return try {
            val request = Request.Builder()
                .url(url)
                .header("User-Agent", "Mozilla/5.0 (Android; Mobile; rv:120.0)")
                .header("Accept", "application/json")
                .build()
            val response = httpClient.newCall(request).execute()
            if (response.isSuccessful) {
                response.body?.string()
            } else {
                response.close()
                null
            }
        } catch (e: Exception) {
            Log.w(TAG, "GET error for $url: ${e.message}")
            null
        }
    }

    private fun parseEspnSummary(eventId: String, jsonString: String): MatchSummaryDetail? {
        try {
            val root = JSONObject(jsonString)
            val boxscore = root.optJSONObject("boxscore")
            val teamsArr = boxscore?.optJSONArray("teams")

            var homeTeamId = ""
            var homeTeamName = ""
            var awayTeamId = ""
            var awayTeamName = ""

            if (teamsArr != null && teamsArr.length() >= 2) {
                val t0 = teamsArr.getJSONObject(0).optJSONObject("team")
                val t1 = teamsArr.getJSONObject(1).optJSONObject("team")
                homeTeamId = t0?.optString("id", "") ?: ""
                homeTeamName = t0?.optString("displayName", "") ?: ""
                awayTeamId = t1?.optString("id", "") ?: ""
                awayTeamName = t1?.optString("displayName", "") ?: ""
            }

            // 1. Events (Goals, Cards, Substitutions)
            val eventsList = mutableListOf<MatchEvent>()
            val keyEvents = root.optJSONArray("keyEvents")
            if (keyEvents != null) {
                for (i in 0 until keyEvents.length()) {
                    val e = keyEvents.getJSONObject(i)
                    val id = e.optString("id", i.toString())
                    val typeObj = e.optJSONObject("type")
                    val typeText = typeObj?.optString("text", "") ?: ""
                    val typeType = (typeObj?.optString("type", "") ?: "").lowercase()
                    val clock = e.optJSONObject("clock")?.optString("displayValue", "") ?: ""
                    val fullText = e.optString("text", "")
                    val shortText = e.optString("shortText", "")
                    val scoringPlay = e.optBoolean("scoringPlay", false)

                    val eventTeamObj = e.optJSONObject("team")
                    val eventTeamId = eventTeamObj?.optString("id", "") ?: ""
                    val eventTeamName = eventTeamObj?.optString("displayName", "") ?: ""

                    val isHome = when {
                        eventTeamId.isNotBlank() && eventTeamId == homeTeamId -> true
                        eventTeamId.isNotBlank() && eventTeamId == awayTeamId -> false
                        homeTeamName.isNotBlank() && eventTeamName.contains(homeTeamName, ignoreCase = true) -> true
                        awayTeamName.isNotBlank() && eventTeamName.contains(awayTeamName, ignoreCase = true) -> false
                        else -> true
                    }

                    var playerName = ""
                    var assistName = ""
                    val participants = e.optJSONArray("participants")
                    if (participants != null && participants.length() > 0) {
                        val p0 = participants.getJSONObject(0).optJSONObject("athlete")
                        playerName = p0?.optString("displayName", "") ?: ""
                        if (participants.length() > 1) {
                            val p1 = participants.getJSONObject(1).optJSONObject("athlete")
                            assistName = p1?.optString("displayName", "") ?: ""
                        }
                    }

                    if (playerName.isBlank() && shortText.isNotBlank()) {
                        playerName = shortText
                            .replace(" Yellow Card", "", ignoreCase = true)
                            .replace(" Red Card", "", ignoreCase = true)
                            .replace(" Goal", "", ignoreCase = true)
                            .replace(" Substitution", "", ignoreCase = true)
                            .trim()
                    }

                    var playerIn = ""
                    var playerOut = ""
                    val isSub = typeType.contains("sub") || typeText.contains("sub", ignoreCase = true) || fullText.contains("Substitution", ignoreCase = true)
                    if (isSub && fullText.contains("replaces", ignoreCase = true)) {
                        val repParts = fullText.split("replaces", limit = 2)
                        playerIn = repParts[0].trim().substringAfterLast(".").trim()
                        playerOut = repParts[1].trim().substringBefore(".").trim()
                    }

                    val normalizedType = when {
                        scoringPlay || typeType.contains("goal") || typeText.contains("Goal", ignoreCase = true) -> "goal"
                        typeType.contains("red-card") || typeText.contains("Red Card", ignoreCase = true) || fullText.contains("red card", ignoreCase = true) -> "red-card"
                        typeType.contains("yellow-card") || typeText.contains("Yellow Card", ignoreCase = true) || fullText.contains("yellow card", ignoreCase = true) -> "yellow-card"
                        isSub -> "substitution"
                        typeType.contains("penalty") || typeText.contains("Penalty", ignoreCase = true) -> "penalty"
                        else -> ""
                    }

                    if (normalizedType.isNotBlank()) {
                        eventsList.add(
                            MatchEvent(
                                id = id,
                                type = normalizedType,
                                minute = clock,
                                text = fullText.ifBlank { shortText },
                                playerName = playerName,
                                assistName = assistName,
                                playerIn = playerIn,
                                playerOut = playerOut,
                                isHomeTeam = isHome
                            )
                        )
                    }
                }
            }

            // 2. Real Statistics (Possession, Shots, Offsides, Fouls, Corners, Passes, Cards, Saves)
            val statsList = mutableListOf<MatchStatRow>()
            if (teamsArr != null && teamsArr.length() >= 2) {
                val t0StatsMap = parseStatsMap(teamsArr.getJSONObject(0).optJSONArray("statistics"))
                val t1StatsMap = parseStatsMap(teamsArr.getJSONObject(1).optJSONArray("statistics"))

                fun addStat(statKey: String, arabicLabel: String, isPct: Boolean = false) {
                    val raw0 = t0StatsMap[statKey]
                    val raw1 = t1StatsMap[statKey]
                    if (raw0 != null && raw1 != null) {
                        val n0 = raw0.replace("%", "").trim().toFloatOrNull() ?: 0f
                        val n1 = raw1.replace("%", "").trim().toFloatOrNull() ?: 0f
                        val total = n0 + n1
                        val progress = if (total > 0f) n0 / total else 0.5f
                        val display0 = if (isPct && !raw0.endsWith("%")) "$raw0%" else raw0
                        val display1 = if (isPct && !raw1.endsWith("%")) "$raw1%" else raw1
                        statsList.add(
                            MatchStatRow(
                                name = arabicLabel,
                                homeValue = display0,
                                awayValue = display1,
                                homeProgress = progress
                            )
                        )
                    }
                }

                addStat("possessionPct", "نسبة الاستحواذ", isPct = true)
                addStat("totalShots", "إجمالي التسديدات")
                addStat("shotsOnTarget", "التسديدات على المرمى")
                addStat("offsides", "حالات التسلل")
                addStat("wonCorners", "الضربات الركنية")
                addStat("foulsCommitted", "الأخطاء المرتكبة")
                addStat("yellowCards", "البطاقات الصفراء")
                addStat("redCards", "البطاقات الحمراء")
                addStat("saves", "التصديات")
                addStat("accuratePasses", "التمريرات الصحيحة")
                addStat("passPct", "دقة التمرير", isPct = true)
            }

            // 3. Lineups & Substitutions (Official Starters & Bench)
            var homeRoster: MatchTeamRoster? = null
            var awayRoster: MatchTeamRoster? = null
            val rostersArr = root.optJSONArray("rosters")
            if (rostersArr != null && rostersArr.length() >= 2) {
                homeRoster = parseTeamRoster(rostersArr.getJSONObject(0))
                awayRoster = parseTeamRoster(rostersArr.getJSONObject(1))
            }

            // 4. Game Info (Venue, Attendance, Referee)
            val gameInfo = root.optJSONObject("gameInfo")
            val venue = gameInfo?.optJSONObject("venue")?.optString("fullName", "") ?: ""
            val attendance = gameInfo?.optString("attendance", "") ?: ""
            var referee = ""
            val officials = gameInfo?.optJSONArray("officials")
            if (officials != null && officials.length() > 0) {
                referee = officials.getJSONObject(0).optString("displayName", "")
            }

            return MatchSummaryDetail(
                eventId = eventId,
                events = eventsList,
                statistics = statsList,
                homeRoster = homeRoster,
                awayRoster = awayRoster,
                venue = venue,
                attendance = attendance,
                referee = referee
            )
        } catch (e: Exception) {
            Log.e(TAG, "Error parsing ESPN summary for event $eventId: ${e.message}", e)
            return null
        }
    }

    private fun parseStatsMap(arr: JSONArray?): Map<String, String> {
        val map = mutableMapOf<String, String>()
        if (arr == null) return map
        for (i in 0 until arr.length()) {
            val obj = arr.getJSONObject(i)
            val name = obj.optString("name", "")
            val value = obj.optString("displayValue", "")
            if (name.isNotBlank()) {
                map[name] = value
            }
        }
        return map
    }

    private fun parseTeamRoster(obj: JSONObject): MatchTeamRoster {
        val teamObj = obj.optJSONObject("team")
        val teamName = teamObj?.optString("displayName", "") ?: ""
        val teamLogo = teamObj?.optString("logo", "") ?: ""
        val formation = obj.optString("formation", "")

        val starters = mutableListOf<MatchPlayer>()
        val subs = mutableListOf<MatchPlayer>()

        val rosterArr = obj.optJSONArray("roster")
        if (rosterArr != null) {
            for (i in 0 until rosterArr.length()) {
                val p = rosterArr.getJSONObject(i)
                val athlete = p.optJSONObject("athlete")
                val id = athlete?.optString("id", i.toString()) ?: i.toString()
                val name = athlete?.optString("displayName", "") ?: ""
                val jersey = p.optString("jersey", "")
                val posObj = p.optJSONObject("position")
                val posName = posObj?.optString("displayName", "") ?: ""
                val arabicPos = translatePosition(posName)
                val isStarter = p.optBoolean("starter", false)
                val subbedIn = p.optBoolean("subbedIn", false)
                val subbedOut = p.optBoolean("subbedOut", false)
                val headshot = athlete?.optJSONObject("headshot")?.optString("href", "") ?: ""

                val player = MatchPlayer(
                    id = id,
                    name = name,
                    jersey = jersey,
                    position = arabicPos,
                    isStarter = isStarter,
                    subbedIn = subbedIn,
                    subbedOut = subbedOut,
                    headshotUrl = headshot
                )

                if (isStarter) {
                    starters.add(player)
                } else {
                    subs.add(player)
                }
            }
        }

        return MatchTeamRoster(
            teamName = teamName,
            teamLogo = teamLogo,
            formation = formation,
            starters = starters,
            substitutes = subs
        )
    }

    private fun translatePosition(pos: String): String {
        val lower = pos.lowercase()
        return when {
            lower.contains("goalkeeper") || lower.contains("goal keeper") || lower == "gk" -> "حارس مرمى"
            lower.contains("centre back") || lower.contains("center back") || lower == "cb" -> "قلب دفاع"
            lower.contains("left back") || lower == "lb" -> "ظهير أيسر"
            lower.contains("right back") || lower == "rb" -> "ظهير أيمن"
            lower.contains("defender") || lower.contains("back") -> "مدافع"
            lower.contains("defensive midfield") || lower == "dm" -> "وسط مدافع"
            lower.contains("attacking midfield") || lower == "am" -> "وسط هجومي"
            lower.contains("left midfield") || lower == "lm" -> "وسط أيسر"
            lower.contains("right midfield") || lower == "rm" -> "وسط أيمن"
            lower.contains("midfielder") || lower.contains("midfield") -> "لاعب وسط"
            lower.contains("left wing") || lower == "lw" -> "جناح أيسر"
            lower.contains("right wing") || lower == "rw" -> "جناح أيمن"
            lower.contains("centre forward") || lower.contains("center forward") || lower == "cf" -> "مهاجم صريح"
            lower.contains("striker") || lower == "st" -> "رأس حربة"
            lower.contains("forward") || lower.contains("attacker") -> "مهاجم"
            else -> pos
        }
    }
}
