package com.example

import com.example.data.model.XtremeContentType
import com.example.data.model.XtremeItem
import com.example.data.util.LibraryDeduplicator
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class ExampleUnitTest {
    @Test
    fun addition_isCorrect() {
        assertEquals(4, 2 + 2)
    }

    @Test
    fun deduplicator_ensuresMovieAppearsOnlyOnceAcrossAccounts() {
        val items = listOf(
            XtremeItem(
                id = "1",
                title = "Inception (2010)",
                streamId = "101",
                streamIcon = "",
                type = XtremeContentType.MOVIE,
                streamUrl = "http://server1/1.mp4",
                accountName = "Account A"
            ),
            XtremeItem(
                id = "2",
                title = "Inception 2010",
                streamId = "202",
                streamIcon = "",
                type = XtremeContentType.MOVIE,
                streamUrl = "http://server2/2.mp4",
                accountName = "Account B"
            )
        )

        val result = LibraryDeduplicator.deduplicate(items)
        assertEquals(1, result.size)
        assertEquals("Account A", result[0].accountName)
    }

    @Test
    fun deduplicator_strictlyDisplaysEachTitleOnce() {
        val items = listOf(
            XtremeItem(
                id = "1",
                title = "Interstellar",
                streamId = "101",
                streamIcon = "",
                type = XtremeContentType.MOVIE,
                streamUrl = "http://server1/1.mp4",
                accountName = "Account A"
            ),
            XtremeItem(
                id = "2",
                title = "Interstellar [4K]",
                streamId = "202",
                streamIcon = "",
                type = XtremeContentType.MOVIE,
                streamUrl = "http://server2/2.mp4",
                accountName = "Account B"
            )
        )

        val result = LibraryDeduplicator.deduplicate(items)
        // Strictly only 1 entry displayed for Interstellar
        assertEquals(1, result.size)
    }

    @Test
    fun deduplicator_handlesArabicTitlesAndFiltersLiveStreams() {
        val items = listOf(
            XtremeItem(
                id = "1",
                title = "فيلم كيرة والجن 2022",
                streamId = "101",
                streamIcon = "",
                type = XtremeContentType.MOVIE,
                streamUrl = "http://server1/1.mp4"
            ),
            XtremeItem(
                id = "2",
                title = "فيلم كيرة والجن 2022",
                streamId = "202",
                streamIcon = "",
                type = XtremeContentType.MOVIE,
                streamUrl = "http://server2/2.mp4"
            ),
            XtremeItem(
                id = "3",
                title = "قناة بي ان سبورت 1",
                streamId = "303",
                streamIcon = "",
                type = XtremeContentType.LIVE,
                streamUrl = "http://server/live.m3u8"
            )
        )

        val result = LibraryDeduplicator.deduplicate(items)
        assertEquals(1, result.size)
        assertTrue(result.none { it.type == XtremeContentType.LIVE })
    }

    @Test
    fun chatMessage_serializesAndDeserializesChannelId() {
        val message = com.example.data.model.ChatMessage(
            id = "msg123",
            channelId = "bein_sports_1",
            text = "هجمة خطيرة!",
            authorFullName = "أحمد علي",
            authorUsername = "ahmed123"
        )
        val map = message.toMap()
        assertEquals("bein_sports_1", map["channelId"])

        val parsed = com.example.data.model.ChatMessage.fromMap("msg123", map)
        assertEquals("bein_sports_1", parsed.channelId)
        assertEquals("msg123", parsed.id)
        assertEquals("هجمة خطيرة!", parsed.text)
    }

    @Test
    fun version_isConfiguredCorrectlyForDeveloperMode() {
        assertEquals("4.0.0", com.example.ui.viewmodel.FootballViewModel.CURRENT_APP_VERSION)
        assertEquals(false, com.example.ui.viewmodel.FootballViewModel.IS_USER_RELEASE_BUILD)
        assertTrue(com.example.ui.viewmodel.FootballViewModel.isAcceptedVersion("4.0.0"))
    }

    @Test
    fun adminAuth_requiresAuthenticationBeforeWriting() {
        if (!com.example.data.security.AdminAuthManager.isAuthenticated) {
            val check = com.example.data.security.AdminAuthManager.requireAdminAuthentication()
            assertTrue(check.isFailure)
        }
    }
}

