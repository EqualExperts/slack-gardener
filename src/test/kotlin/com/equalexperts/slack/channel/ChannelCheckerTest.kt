package com.equalexperts.slack.channel

import com.equalexperts.slack.api.chat.ChatSlackApi
import com.equalexperts.slack.api.conversations.ConversationsSlackApi
import com.equalexperts.slack.api.conversations.model.Conversation
import com.equalexperts.slack.api.users.UsersForTesting
import com.equalexperts.slack.channel.ChannelChecker
import com.equalexperts.slack.channel.ChannelState
import com.equalexperts.slack.channel.ChannelStateCalculator
import com.equalexperts.slack.channel.ConversationListsForTesting
import com.equalexperts.slack.profile.UserProfilesForTesting
import com.nhaarman.mockitokotlin2.*
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.TestInstance
import org.junit.jupiter.api.TestInstance.Lifecycle.PER_CLASS
import java.time.*
import java.time.format.DateTimeFormatter
import java.util.*

@TestInstance(PER_CLASS)
class ChannelCheckerTest {

    private lateinit var mockConversationsApi: ConversationsSlackApi
    private lateinit var mockChatSlackApi: ChatSlackApi
    private lateinit var clock: Clock
    private lateinit var mockChannelStateCalculator: ChannelStateCalculator

    private val warningPeriod = Period.ofDays(1)

    private val warningMessageContent = "WARNING MESSAGE"

    private val botUser = UsersForTesting.testBot(profile = UserProfilesForTesting.testBotProfile())

    private lateinit var warningThreshold: ZonedDateTime
    private lateinit var afterWarningThreshold: ZonedDateTime
    private lateinit var beforeWarningThreshold: ZonedDateTime

    private val channel = Conversation("TEST_ID", "CHANNEL_NAME", Instant.EPOCH.epochSecond, 1)

    @BeforeEach
    fun setup() {
        mockConversationsApi = mock()
        mockConversationsApi = mock()
        mockChatSlackApi = mock()
        clock = mock()
        mockChannelStateCalculator = mock()

        whenever(clock.instant()).thenReturn(getNow())
        whenever(clock.zone).thenReturn(ZoneOffset.UTC)

        warningThreshold = ZonedDateTime.now(clock) - warningPeriod
        afterWarningThreshold = warningThreshold - warningPeriod
        beforeWarningThreshold = warningThreshold + Period.ofDays(3)


    }

    @Test
    fun `should not archive when stale and warning wasn't sent long enough ago`() {
        val gardener = getGardener()

        val channelList = ConversationListsForTesting.withEmptyCursorToken(channel)
        whenever(mockConversationsApi.list()).doReturn(channelList)

        whenever(mockChannelStateCalculator.determineChannelState(channel, botUser)).thenReturn(ChannelState.StaleAndWarned(beforeWarningThreshold))

        gardener.process()

        verify(mockConversationsApi, never()).channelsArchive(channel)
    }


    @Test
    fun `should archive when stale and warning was sent long enough ago`() {
        val gardener = getGardener()

        val channelList = ConversationListsForTesting.withEmptyCursorToken(channel)
        whenever(mockConversationsApi.list()).doReturn(channelList)

        whenever(mockChannelStateCalculator.determineChannelState(channel, botUser)).thenReturn(ChannelState.StaleAndWarned(afterWarningThreshold))

        gardener.process()

        verify(mockConversationsApi, times(1)).channelsArchive(channel)
    }


    @Test
    fun `should warn and not archive when stale`() {
        val gardener = getGardener()

        val channelList = ConversationListsForTesting.withEmptyCursorToken(channel)
        whenever(mockConversationsApi.list()).doReturn(channelList)

        whenever(mockChannelStateCalculator.determineChannelState(channel, botUser)).thenReturn(ChannelState.Stale)

        gardener.process()

        verify(mockChatSlackApi, times(1)).postMessage(channel, botUser, warningMessageContent)
        verify(mockConversationsApi, never()).channelsArchive(channel)
    }

    @Test
    fun `should not archive or warn when active`() {
        val gardener = getGardener()

        val channelList = ConversationListsForTesting.withEmptyCursorToken(channel)
        whenever(mockConversationsApi.list()).doReturn(channelList)

        whenever(mockChannelStateCalculator.determineChannelState(channel, botUser)).thenReturn(ChannelState.Active)

        gardener.process()

        verify(mockConversationsApi, never()).channelsArchive(channel)
        verify(mockChatSlackApi, never()).postMessage(channel, botUser, warningMessageContent)
    }

    private fun getGardener(): ChannelChecker {
        return ChannelChecker(mockConversationsApi, mockChatSlackApi, botUser, clock, mockChannelStateCalculator, warningPeriod, warningMessageContent)
    }

    private fun getNow(): Instant? {
        val localDateTime = LocalDateTime.parse("04:30 PM, Sat 4/4/1992", DateTimeFormatter.ofPattern("hh:mm a, EEE M/d/uuuu", Locale.ENGLISH))
        val zonedDateTime = localDateTime.atZone(ZoneId.of("Europe/London"))
        return zonedDateTime.toInstant()
    }
}