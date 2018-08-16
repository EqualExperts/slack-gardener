package com.equalexperts.slack.gardener

import com.equalexperts.slack.gardener.rest.SlackApi
import com.equalexperts.slack.gardener.rest.SlackBotApi
import com.equalexperts.slack.gardener.rest.model.*
import com.nhaarman.mockito_kotlin.*
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.TestInstance
import org.junit.jupiter.api.TestInstance.Lifecycle.PER_CLASS

import java.time.*
import java.time.format.DateTimeFormatter
import java.util.*

@TestInstance(PER_CLASS)
class GardenerTest {
    private lateinit var mockSlackApi : SlackApi
    private lateinit var mockSlackBotApi : SlackBotApi
    private lateinit var clock : Clock

    private val defaultIdlePeriod = Period.ofDays(5)
    private val longIdlePeriod = Period.ofWeeks(2)
    private val warningPeriod = Period.ofDays(1)

    private val warningMessageContent = "WARNING MESSAGE"

    private val botUser = User("TEST_BOT_USER", UserProfile("TEST_BOT_ID"))
    private val nonBotUser = User("TEST_USER", UserProfile("TEST_USER_ID"))

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

    private val nonWhitelistedChannel = ChannelInfo("TEST_ID", "CHANNEL_NAME", Instant.EPOCH.epochSecond, 1)
    private val whitelistedChannel = ChannelInfo("TEST_ID", whitelistedChannelName, Instant.EPOCH.epochSecond, 1)
    private val longIdlePeriodChannel = ChannelInfo("TEST_ID", longIdlePeriodChannelName, Instant.EPOCH.epochSecond, 1)

    @BeforeEach
    fun setup() {
        mockSlackApi = mock()
        mockSlackBotApi = mock()
        clock = mock()

        whenever(mockSlackBotApi.authenticate()).thenReturn(AuthInfo(botUser.profile.botId.toString()))
        whenever(mockSlackBotApi.getUserInfo(any())).thenReturn(UserInfo(botUser))

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

        val channelList = ChannelList(listOf(whitelistedChannel))
        whenever(mockSlackApi.listChannels()).doReturn(channelList)

        val channelMessages = listOf(botMessageAfterWarningThreshold)
        val channelHistory = ChannelHistory(false, channelMessages)
        whenever(mockSlackApi.getChannelHistory(any(), any())).doReturn(channelHistory)

        gardener.process()

        verify(mockSlackApi, never()).archiveChannel(whitelistedChannel)
        verify(mockSlackBotApi, never()).postMessage(whitelistedChannel, botUser, warningMessageContent)
    }

    @Test
    fun shouldNotArchiveOrWarnLongIdlePeriodChannelsDuringLongIdlePeriod() {
        val gardener = getGardener(whitelistedChannels, longIdlePeriodChannels)

        val channelList = ChannelList(listOf(longIdlePeriodChannel))
        whenever(mockSlackApi.listChannels()).doReturn(channelList)

        val channelMessages = listOf(nonBotMessageDuringLongPeriodThreshold)
        val channelHistory = ChannelHistory(false, channelMessages)
        whenever(mockSlackApi.getChannelHistory(any(), any())).doReturn(channelHistory)

        gardener.process()

        verify(mockSlackApi, never()).archiveChannel(longIdlePeriodChannel)
        verify(mockSlackBotApi, never()).postMessage(longIdlePeriodChannel, botUser, warningMessageContent)
    }

    @Test
    fun shouldArchiveWhenStaleAndAfterWarning() {
        val gardener = getGardener(whitelistedChannels, longIdlePeriodChannels)

        val channelList = ChannelList(listOf(nonWhitelistedChannel))
        whenever(mockSlackApi.listChannels()).doReturn(channelList)

        val channelMessages = listOf(botMessageAfterWarningThreshold)
        val channelHistory = ChannelHistory(false, channelMessages)
        whenever(mockSlackApi.getChannelHistory(any(), any())).doReturn(channelHistory)

        gardener.process()

        verify(mockSlackApi, times(1)).archiveChannel(nonWhitelistedChannel)
    }

    @Test
    fun shouldNotArchiveWhenStaleAndBeforeWarning() {
        val gardener = getGardener(whitelistedChannels, longIdlePeriodChannels)

        val channelList = ChannelList(listOf(nonWhitelistedChannel))
        whenever(mockSlackApi.listChannels()).doReturn(channelList)

        val channelMessages = listOf(botMessageBeforeWarningThreshold)
        val channelHistory = ChannelHistory(false, channelMessages)
        whenever(mockSlackApi.getChannelHistory(any(), any())).doReturn(channelHistory)

        gardener.process()

        verify(mockSlackApi, never()).archiveChannel(nonWhitelistedChannel)
    }

    @Test
    fun shouldWarnWhenStale() {
        val gardener = getGardener(whitelistedChannels, longIdlePeriodChannels)

        val channelList = ChannelList(listOf(nonWhitelistedChannel))
        whenever(mockSlackApi.listChannels()).doReturn(channelList)

        val channelMessages = emptyList<Message>()
        val channelHistory = ChannelHistory(false, channelMessages)
        whenever(mockSlackApi.getChannelHistory(any(), any())).doReturn(channelHistory)

        gardener.process()

        verify(mockSlackBotApi, times(1)).postMessage(nonWhitelistedChannel, botUser, warningMessageContent)
    }

    @Test
    fun shouldNotArchiveOrWarnWhenActive() {
        val gardener = getGardener(whitelistedChannels, longIdlePeriodChannels)

        val channelList = ChannelList(listOf(nonWhitelistedChannel))
        whenever(mockSlackApi.listChannels()).doReturn(channelList)

        val channelMessages = listOf(nonBotMessage)
        val channelHistory = ChannelHistory(false, channelMessages)
        whenever(mockSlackApi.getChannelHistory(any(), any())).doReturn(channelHistory)

        gardener.process()

        verify(mockSlackApi, never()).archiveChannel(nonWhitelistedChannel)
        verify(mockSlackBotApi, never()).postMessage(nonWhitelistedChannel, botUser, warningMessageContent)
    }


    private fun getGardener(whitelistedChannels: Set<String>, longIdlePeriodChannels: Set<String>): Gardener {
        return Gardener(mockSlackApi, mockSlackBotApi, clock, defaultIdlePeriod, warningPeriod, whitelistedChannels, longIdlePeriodChannels, longIdlePeriod,  warningMessageContent)
    }

    private fun getNow(): Instant? {
        val localDateTime = LocalDateTime.parse("04:30 PM, Sat 4/4/1992", DateTimeFormatter.ofPattern("hh:mm a, EEE M/d/uuuu", Locale.ENGLISH))
        val zonedDateTime = localDateTime.atZone(ZoneId.of("Europe/London"))
        val instant = zonedDateTime.toInstant()
        return instant
    }
}