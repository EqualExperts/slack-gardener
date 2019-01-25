package com.equalexperts.slack.gardener

import com.equalexperts.slack.api.channels.ChannelHistory
import com.equalexperts.slack.api.channels.ChannelsSlackApi
import com.equalexperts.slack.api.conversations.ConversationApi
import com.equalexperts.slack.api.conversations.model.Conversation
import com.equalexperts.slack.api.chat.ChatSlackApi
import com.equalexperts.slack.api.rest.model.Message
import com.equalexperts.slack.api.users.model.User
import com.equalexperts.slack.api.users.model.UserProfile
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

    private lateinit var mockChannelsSlackApi: ChannelsSlackApi
    private lateinit var mockConversationsApi: ConversationApi
    private lateinit var mockChatSlackApi: ChatSlackApi
    private lateinit var clock: Clock

    private val defaultIdlePeriod = Period.ofDays(5)
    private val longIdlePeriod = Period.ofWeeks(2)
    private val warningPeriod = Period.ofDays(1)

    private val warningMessageContent = "WARNING MESSAGE"

    private val botUser = User(name="TEST_BOT_USER",
            profile=UserProfile("TEST_BOT_ID"),
            id="id",
            team_id="team_id",
            deleted=false,
            is_admin=false,
            is_owner=false,
            is_primary_owner=false,
            is_restricted=false,
            is_ultra_restricted=false,
            is_bot=true,
            is_app_user=false)
    private val nonBotUser = User("TEST_USER",
            UserProfile("TEST_USER_ID"),
            id="id",
            team_id="team_id",
            deleted=false,
            is_admin=false,
            is_owner=false,
            is_primary_owner=false,
            is_restricted=false,
            is_ultra_restricted=false,
            is_bot=false,
            is_app_user=false)

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


    private fun getWhitelistedChannels() = setOf(whitelistedChannel)

    private fun getNonWhitelistedChannels() = setOf(nonWhitelistedChannel)

    private fun getLongIdlePeriodChannels() = setOf(longIdlePeriodChannel)


    @BeforeEach
    fun setup() {
        mockChannelsSlackApi = mock()
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

        botMessageBeforeWarningThreshold = Message("BOT_MESSAGE", "bot_message", botUser.name, botUser.profile.botId.toString(), beforeWarningThreshold.toEpochSecond().toString())
        botMessageAfterWarningThreshold = Message("BOT_MESSAGE", "bot_message", botUser.name, botUser.profile.botId.toString(), afterWarningThreshold.toEpochSecond().toString())

        nonBotMessageDuringLongPeriodThreshold = Message("message", null, nonBotUser.name, nonBotUser.profile.botId.toString(), duringLongIdlePeriod.toEpochSecond().toString())


        nonBotMessage = Message("message", null, nonBotUser.name, nonBotUser.profile.botId.toString(), afterWarningThreshold.toEpochSecond().toString())
    }

    @Test
    fun shouldNotArchiveOrWarnWhitelistedChannels() {
        val gardener = getGardener(whitelistedChannels, longIdlePeriodChannels)

        val channelList = getWhitelistedChannels()
        whenever(mockConversationsApi.list()).doReturn(channelList)

        val channelMessages = listOf(botMessageAfterWarningThreshold)
        val channelHistory = ChannelHistory(false, channelMessages)
        whenever(mockChannelsSlackApi.channelHistory(any(), any())).doReturn(channelHistory)

        gardener.process()

        verify(mockChannelsSlackApi, never()).channelsArchive(whitelistedChannel)
        verify(mockChatSlackApi, never()).postMessage(whitelistedChannel, botUser, warningMessageContent)
    }

    @Test
    fun shouldNotArchiveOrWarnLongIdlePeriodChannelsDuringLongIdlePeriod() {
        val gardener = getGardener(whitelistedChannels, longIdlePeriodChannels)

        val channelList = getLongIdlePeriodChannels()
        whenever(mockConversationsApi.list()).doReturn(channelList)

        val channelMessages = listOf(nonBotMessageDuringLongPeriodThreshold)
        val channelHistory = ChannelHistory(false, channelMessages)
        whenever(mockChannelsSlackApi.channelHistory(any(), any())).doReturn(channelHistory)

        gardener.process()

        verify(mockChannelsSlackApi, never()).channelsArchive(longIdlePeriodChannel)
        verify(mockChatSlackApi, never()).postMessage(longIdlePeriodChannel, botUser, warningMessageContent)
    }

    @Test
    fun shouldArchiveWhenStaleAndAfterWarning() {
        val gardener = getGardener(whitelistedChannels, longIdlePeriodChannels)

        val channelList = getNonWhitelistedChannels()
        whenever(mockConversationsApi.list()).doReturn(channelList)

        val channelMessages = listOf(botMessageAfterWarningThreshold)
        val channelHistory = ChannelHistory(false, channelMessages)
        whenever(mockChannelsSlackApi.channelHistory(any(), any())).doReturn(channelHistory)

        gardener.process()

        verify(mockChannelsSlackApi, times(1)).channelsArchive(nonWhitelistedChannel)
    }

    @Test
    fun shouldNotArchiveWhenStaleAndBeforeWarning() {
        val gardener = getGardener(whitelistedChannels, longIdlePeriodChannels)

        val channelList = getNonWhitelistedChannels()
        whenever(mockConversationsApi.list()).doReturn(channelList)

        val channelMessages = listOf(botMessageBeforeWarningThreshold)
        val channelHistory = ChannelHistory(false, channelMessages)
        whenever(mockChannelsSlackApi.channelHistory(any(), any())).doReturn(channelHistory)

        gardener.process()

        verify(mockChannelsSlackApi, never()).channelsArchive(nonWhitelistedChannel)
    }

    @Test
    fun shouldWarnWhenStale() {
        val gardener = getGardener(whitelistedChannels, longIdlePeriodChannels)

        val channelList = getNonWhitelistedChannels()
        whenever(mockConversationsApi.list()).doReturn(channelList)

        val channelMessages = emptyList<Message>()
        val channelHistory = ChannelHistory(false, channelMessages)
        whenever(mockChannelsSlackApi.channelHistory(any(), any())).doReturn(channelHistory)

        gardener.process()

        verify(mockChatSlackApi, times(1)).postMessage(nonWhitelistedChannel, botUser, warningMessageContent)
    }

    @Test
    fun shouldNotArchiveOrWarnWhenActive() {
        val gardener = getGardener(whitelistedChannels, longIdlePeriodChannels)

        val channelList = getNonWhitelistedChannels()
        whenever(mockConversationsApi.list()).doReturn(channelList)

        val channelMessages = listOf(nonBotMessage)
        val channelHistory = ChannelHistory(false, channelMessages)
        whenever(mockChannelsSlackApi.channelHistory(any(), any())).doReturn(channelHistory)

        gardener.process()

        verify(mockChannelsSlackApi, never()).channelsArchive(nonWhitelistedChannel)
        verify(mockChatSlackApi, never()).postMessage(nonWhitelistedChannel, botUser, warningMessageContent)
    }

    private fun getGardener(whitelistedChannels: Set<String>, longIdlePeriodChannels: Set<String>): Gardener {
        return Gardener(mockChannelsSlackApi, mockConversationsApi, mockChatSlackApi, botUser, clock, defaultIdlePeriod, warningPeriod, whitelistedChannels, longIdlePeriodChannels, longIdlePeriod, warningMessageContent)
    }

    private fun getNow(): Instant? {
        val localDateTime = LocalDateTime.parse("04:30 PM, Sat 4/4/1992", DateTimeFormatter.ofPattern("hh:mm a, EEE M/d/uuuu", Locale.ENGLISH))
        val zonedDateTime = localDateTime.atZone(ZoneId.of("Europe/London"))
        val instant = zonedDateTime.toInstant()
        return instant
    }
}