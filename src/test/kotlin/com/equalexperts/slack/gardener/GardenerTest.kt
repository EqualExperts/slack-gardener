package com.equalexperts.slack.gardener

import com.equalexperts.slack.api.conversations.model.ConversationHistory
import com.equalexperts.slack.api.conversations.model.Conversation
import com.equalexperts.slack.api.chat.ChatSlackApi
import com.equalexperts.slack.api.conversations.ConversationsSlackApi
import com.equalexperts.slack.api.conversations.model.ConversationList
import com.equalexperts.slack.api.rest.model.Message
import com.equalexperts.slack.api.users.UsersForTesting
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
class GardenerTest {

    private lateinit var mockConversationsApi: ConversationsSlackApi
    private lateinit var mockChatSlackApi: ChatSlackApi
    private lateinit var clock: Clock

    private val defaultIdlePeriod = Period.ofDays(5)
    private val longIdlePeriod = Period.ofWeeks(2)
    private val warningPeriod = Period.ofDays(1)

    private val warningMessageContent = "WARNING MESSAGE"

    private val botUser = UsersForTesting.testBot(profile=UserProfilesForTesting.testBot())
    private val nonBotUser = UsersForTesting.testUser(profile= UserProfilesForTesting.testBot())

    private val whitelistedChannelName = "WHITELISTED_CHANNEL_NAME"
    private val whitelistedChannels = setOf(whitelistedChannelName)
    private val longIdlePeriodChannelName = "CHECK_YEARLY_CHANNEL_NAME"
    private val longIdlePeriodChannels = setOf(longIdlePeriodChannelName)

    private lateinit var botMessageAfterWarningThreshold: Message
    private lateinit var botMessageBeforeWarningThreshold: Message

    private lateinit var nonBotMessageDuringLongPeriodThreshold: Message

    private lateinit var nonBotMessage: Message

    private lateinit var warningThreshold: ZonedDateTime
    private lateinit var afterWarningThreshold: ZonedDateTime
    private lateinit var beforeWarningThreshold: ZonedDateTime

    private lateinit var longIdlePeriodThreshold: ZonedDateTime
    private lateinit var duringLongIdlePeriod: ZonedDateTime

    private val nonWhitelistedChannel = Conversation("TEST_ID", "CHANNEL_NAME", Instant.EPOCH.epochSecond, 1)
    private val whitelistedChannel = Conversation("TEST_ID", whitelistedChannelName, Instant.EPOCH.epochSecond, 1)
    private val longIdlePeriodChannel = Conversation("TEST_ID", longIdlePeriodChannelName, Instant.EPOCH.epochSecond, 1)


    @BeforeEach
    fun setup() {
        mockConversationsApi = mock()
        mockConversationsApi = mock()
        mockChatSlackApi = mock()
        clock = mock()

        whenever(clock.instant()).thenReturn(getNow())
        whenever(clock.zone).thenReturn(ZoneOffset.UTC)

        warningThreshold = ZonedDateTime.now(clock) - warningPeriod
        afterWarningThreshold = warningThreshold - warningPeriod
        beforeWarningThreshold = warningThreshold + Period.ofDays(3)

        longIdlePeriodThreshold = ZonedDateTime.now(clock) - longIdlePeriod
        duringLongIdlePeriod = longIdlePeriodThreshold + Period.ofDays(1)

        botMessageBeforeWarningThreshold = Message("BOT_MESSAGE", "bot_message", botUser.name, botUser.profile.bot_id, beforeWarningThreshold.toEpochSecond().toString())
        botMessageAfterWarningThreshold = Message("BOT_MESSAGE", "bot_message", botUser.name, botUser.profile.bot_id, afterWarningThreshold.toEpochSecond().toString())

        nonBotMessageDuringLongPeriodThreshold = Message("message", null, nonBotUser.name, nonBotUser.profile.bot_id, duringLongIdlePeriod.toEpochSecond().toString())


        nonBotMessage = Message("message", null, nonBotUser.name, nonBotUser.profile.bot_id, afterWarningThreshold.toEpochSecond().toString())
    }

    @Test
    fun shouldNotArchiveOrWarnWhitelistedChannels() {
        val gardener = getGardener(whitelistedChannels, longIdlePeriodChannels)

        val channelList = ConversationList.withEmptyCursorToken(whitelistedChannel)
        whenever(mockConversationsApi.list()).doReturn(channelList)

        val channelMessages = listOf(botMessageAfterWarningThreshold)
        val history = ConversationHistory(false, channelMessages)
        whenever(mockConversationsApi.channelHistory(any(), any())).doReturn(history)

        gardener.process()

        verify(mockConversationsApi, never()).channelsArchive(whitelistedChannel)
        verify(mockChatSlackApi, never()).postMessage(whitelistedChannel, botUser, warningMessageContent)
    }

    @Test
    fun shouldNotArchiveOrWarnLongIdlePeriodChannelsDuringLongIdlePeriod() {
        val gardener = getGardener(whitelistedChannels, longIdlePeriodChannels)

        val channelList = ConversationList.withEmptyCursorToken(longIdlePeriodChannel)
        whenever(mockConversationsApi.list()).doReturn(channelList)

        val channelMessages = listOf(nonBotMessageDuringLongPeriodThreshold)
        val history = ConversationHistory(false, channelMessages)
        whenever(mockConversationsApi.channelHistory(any(), any())).doReturn(history)

        gardener.process()

        verify(mockConversationsApi, never()).channelsArchive(longIdlePeriodChannel)
        verify(mockChatSlackApi, never()).postMessage(longIdlePeriodChannel, botUser, warningMessageContent)
    }

    @Test
    fun shouldArchiveWhenStaleAndAfterWarning() {
        val gardener = getGardener(whitelistedChannels, longIdlePeriodChannels)

        val channelList = ConversationList.withEmptyCursorToken(nonWhitelistedChannel)
        whenever(mockConversationsApi.list()).doReturn(channelList)

        val channelMessages = listOf(botMessageAfterWarningThreshold)
        val history = ConversationHistory(false, channelMessages)
        whenever(mockConversationsApi.channelHistory(any(), any())).doReturn(history)

        gardener.process()

        verify(mockConversationsApi, times(1)).channelsArchive(nonWhitelistedChannel)
    }

    @Test
    fun shouldNotArchiveWhenStaleAndBeforeWarning() {
        val gardener = getGardener(whitelistedChannels, longIdlePeriodChannels)

        val channelList = ConversationList.withEmptyCursorToken(nonWhitelistedChannel)
        whenever(mockConversationsApi.list()).doReturn(channelList)

        val channelMessages = listOf(botMessageBeforeWarningThreshold)
        val history = ConversationHistory(false, channelMessages)
        whenever(mockConversationsApi.channelHistory(any(), any())).doReturn(history)

        gardener.process()

        verify(mockConversationsApi, never()).channelsArchive(nonWhitelistedChannel)
    }

    @Test
    fun shouldWarnWhenStale() {
        val gardener = getGardener(whitelistedChannels, longIdlePeriodChannels)

        val channelList = ConversationList.withEmptyCursorToken(nonWhitelistedChannel)
        whenever(mockConversationsApi.list()).doReturn(channelList)

        val channelMessages = emptyList<Message>()
        val history = ConversationHistory(false, channelMessages)
        whenever(mockConversationsApi.channelHistory(any(), any())).doReturn(history)

        gardener.process()

        verify(mockChatSlackApi, times(1)).postMessage(nonWhitelistedChannel, botUser, warningMessageContent)
    }

    @Test
    fun shouldNotArchiveOrWarnWhenActive() {
        val gardener = getGardener(whitelistedChannels, longIdlePeriodChannels)

        val channelList = ConversationList.withEmptyCursorToken(nonWhitelistedChannel)
        whenever(mockConversationsApi.list()).doReturn(channelList)

        val channelMessages = listOf(nonBotMessage)
        val history = ConversationHistory(false, channelMessages)
        whenever(mockConversationsApi.channelHistory(any(), any())).doReturn(history)

        gardener.process()

        verify(mockConversationsApi, never()).channelsArchive(nonWhitelistedChannel)
        verify(mockChatSlackApi, never()).postMessage(nonWhitelistedChannel, botUser, warningMessageContent)
    }

    private fun getGardener(whitelistedChannels: Set<String>, longIdlePeriodChannels: Set<String>): Gardener {
        return Gardener(mockConversationsApi, mockChatSlackApi, botUser, clock, defaultIdlePeriod, warningPeriod, whitelistedChannels, longIdlePeriodChannels, longIdlePeriod, warningMessageContent)
    }

    private fun getNow(): Instant? {
        val localDateTime = LocalDateTime.parse("04:30 PM, Sat 4/4/1992", DateTimeFormatter.ofPattern("hh:mm a, EEE M/d/uuuu", Locale.ENGLISH))
        val zonedDateTime = localDateTime.atZone(ZoneId.of("Europe/London"))
        val instant = zonedDateTime.toInstant()
        return instant
    }
}