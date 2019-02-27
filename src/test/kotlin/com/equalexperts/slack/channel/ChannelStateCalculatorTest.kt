package com.equalexperts.slack.channel

import com.equalexperts.slack.api.conversations.ConversationHistoriesForTesting
import com.equalexperts.slack.api.conversations.ConversationsSlackApi
import com.equalexperts.slack.api.conversations.model.Conversation
import com.equalexperts.slack.api.rest.model.Message
import com.equalexperts.slack.api.rest.model.MessagesForTesting
import com.equalexperts.slack.api.users.UsersForTesting
import com.equalexperts.slack.profile.UserProfilesForTesting
import com.nhaarman.mockitokotlin2.any
import com.nhaarman.mockitokotlin2.doReturn
import com.nhaarman.mockitokotlin2.mock
import com.nhaarman.mockitokotlin2.whenever
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.TestInstance
import org.junit.jupiter.api.TestInstance.Lifecycle.PER_CLASS
import java.time.*
import java.time.format.DateTimeFormatter
import java.util.*

@TestInstance(PER_CLASS)
class ChannelStateCalculatorTest {

    private lateinit var mockConversationsApi: ConversationsSlackApi
    private lateinit var clock: Clock
    private lateinit var mockChannelStateCalculator: ChannelStateCalculator

    private val defaultIdlePeriod = Period.ofDays(5)
    private val longIdlePeriod = Period.ofWeeks(2)
    private val warningPeriod = Period.ofDays(1)

    private val warningMessageContent = "WARNING MESSAGE"

    private val gardenerUser = UsersForTesting.testBot(profile = UserProfilesForTesting.testBotProfile().copy(bot_id = "GARDENER_BOT_ID")).copy(name = "GARDENER", id = "GARDENER_BOT_ID")
    private val botUser = UsersForTesting.testBot(profile = UserProfilesForTesting.testBotProfile().copy(bot_id = "NON_GARDENER_BOT_ID")).copy(id = "NON_GARDENER_BOT_ID")
    private val humanUser = UsersForTesting.testUser(profile = UserProfilesForTesting.testUserProfile())

    private val longIdlePeriodChannelName = "CHECK_YEARLY_CHANNEL_NAME"
    private val longIdlePeriodChannels = setOf(longIdlePeriodChannelName)

    private lateinit var gardenerMessageAfterWarningThreshold: Message
    private lateinit var gardenerMessageBeforeWarningThreshold: Message
    private lateinit var gardenerNonWarningMessage: Message
    private lateinit var botMessage: Message

    private lateinit var humanMessageDuringLongPeriodThreshold: Message
    private lateinit var humanMessage: Message

    private lateinit var warningThreshold: ZonedDateTime
    private lateinit var afterWarningThreshold: ZonedDateTime
    private lateinit var beforeWarningThreshold: ZonedDateTime

    private lateinit var longIdlePeriodThreshold: ZonedDateTime
    private lateinit var duringLongIdlePeriod: ZonedDateTime

    private val channel = Conversation("TEST_ID", "CHANNEL_NAME", Instant.EPOCH.epochSecond, 1)
    private val longIdlePeriodChannel = Conversation("TEST_ID", longIdlePeriodChannelName, Instant.EPOCH.epochSecond, 1)

    private lateinit var freshlyCreatedChannel: Conversation


    @BeforeEach
    fun setup() {
        mockConversationsApi = mock()
        clock = mock()
        mockChannelStateCalculator = mock()

        whenever(clock.instant()).thenReturn(getNow())
        whenever(clock.zone).thenReturn(ZoneOffset.UTC)

        freshlyCreatedChannel = Conversation("TEST_ID", "CHANNEL_NAME", getNow().minus(defaultIdlePeriod.minusDays(2)).epochSecond, 1)
        warningThreshold = ZonedDateTime.now(clock) - warningPeriod
        afterWarningThreshold = warningThreshold - warningPeriod
        beforeWarningThreshold = warningThreshold + Period.ofDays(3)

        longIdlePeriodThreshold = ZonedDateTime.now(clock) - longIdlePeriod
        duringLongIdlePeriod = longIdlePeriodThreshold + Period.ofDays(1)

        gardenerMessageBeforeWarningThreshold = MessagesForTesting.botMessage(gardenerUser.id, gardenerUser.profile.bot_id!!, beforeWarningThreshold, warningMessageContent)
        gardenerMessageAfterWarningThreshold = MessagesForTesting.botMessage(gardenerUser.id, gardenerUser.profile.bot_id!!, afterWarningThreshold, warningMessageContent)
        gardenerNonWarningMessage = MessagesForTesting.botMessage(gardenerUser.id, gardenerUser.profile.bot_id!!, afterWarningThreshold, "NON_WARNING_MESSAGE")

        botMessage = MessagesForTesting.botMessage(botUser.id, botUser.profile.bot_id!!, beforeWarningThreshold, "TEST_MESSAGE")

        humanMessageDuringLongPeriodThreshold = MessagesForTesting.userMessage(humanUser.name, humanUser.profile.bot_id, duringLongIdlePeriod, "TEST_MESSAGE")
        humanMessage = MessagesForTesting.userMessage(humanUser.name, humanUser.profile.bot_id, afterWarningThreshold, "TEST_MESSAGE")
    }

    @Test
    fun `channels are Active during long idle period`() {
        val gardener = getChannelStateCalculator(longIdlePeriodChannels)

        val channelList = ConversationListsForTesting.withEmptyCursorToken(longIdlePeriodChannel)
        whenever(mockConversationsApi.list()).doReturn(channelList)

        val channelMessages = listOf(humanMessageDuringLongPeriodThreshold)
        val history = ConversationHistoriesForTesting.withEmptyCursorToken(channelMessages)
        whenever(mockConversationsApi.channelHistory(any<Conversation>(), any())).doReturn(history)

        val state = gardener.determineChannelState(longIdlePeriodChannel, gardenerUser)
        assertEquals(ChannelState.Active, state)

    }

    @Test
    fun `channels are Stale after long idle period`() {
        val gardener = getChannelStateCalculator(longIdlePeriodChannels)

        val channelList = ConversationListsForTesting.withEmptyCursorToken(longIdlePeriodChannel)
        whenever(mockConversationsApi.list()).doReturn(channelList)

        val channelMessages = listOf(humanMessageDuringLongPeriodThreshold)
        val history = ConversationHistoriesForTesting.withEmptyCursorToken(channelMessages)
        whenever(mockConversationsApi.channelHistory(any<Conversation>(), any())).doReturn(history)

        val state = gardener.determineChannelState(longIdlePeriodChannel, gardenerUser)
        assertEquals(ChannelState.Active, state)

    }

    @Test
    fun `channels are StaleAndWarned when they have been warned for inactivity and have had no messages from humans or non-gardener-bots since`() {
        val gardener = getChannelStateCalculator(longIdlePeriodChannels)

        val channelList = ConversationListsForTesting.withEmptyCursorToken(channel)
        whenever(mockConversationsApi.list()).doReturn(channelList)

        val channelMessages = listOf(gardenerMessageAfterWarningThreshold)
        val history = ConversationHistoriesForTesting.withEmptyCursorToken(channelMessages)
        whenever(mockConversationsApi.channelHistory(any<Conversation>(), any())).doReturn(history)

        val state = gardener.determineChannelState(channel, gardenerUser)
        assertEquals(ChannelState.StaleAndWarned(afterWarningThreshold)::class, state::class)
    }

    @Test
    fun `channels are Stale when no messages in idlePeriod and have not been warned previously `() {
        val gardener = getChannelStateCalculator(longIdlePeriodChannels)

        val channelList = ConversationListsForTesting.withEmptyCursorToken(channel)
        whenever(mockConversationsApi.list()).doReturn(channelList)

        val channelMessages = emptyList<Message>()
        val history = ConversationHistoriesForTesting.withEmptyCursorToken(channelMessages)
        whenever(mockConversationsApi.channelHistory(any<Conversation>(), any())).doReturn(history)

        val state = gardener.determineChannelState(channel, gardenerUser)
        assertEquals(ChannelState.Stale, state)
    }

    @Test
    fun `channels are Active when a message from a non-gardener bot is sent`() {
        val gardener = getChannelStateCalculator(longIdlePeriodChannels)

        val channelList = ConversationListsForTesting.withEmptyCursorToken(channel)
        whenever(mockConversationsApi.list()).doReturn(channelList)

        val channelMessages = listOf(botMessage)
        val history = ConversationHistoriesForTesting.withEmptyCursorToken(channelMessages)
        whenever(mockConversationsApi.channelHistory(any<Conversation>(), any())).doReturn(history)

        val state = gardener.determineChannelState(channel, gardenerUser)
        assertEquals(ChannelState.Active, state)
    }

    @Test
    fun `channels are Active when a message from a human is sent`() {
        val gardener = getChannelStateCalculator(longIdlePeriodChannels)

        val channelMessages = listOf(humanMessage)
        val history = ConversationHistoriesForTesting.withEmptyCursorToken(channelMessages)
        whenever(mockConversationsApi.channelHistory(any<Conversation>(), any())).doReturn(history)

        val state = gardener.determineChannelState(channel, gardenerUser)
        assertEquals(ChannelState.Active, state)
    }

    @Test
    fun `channels are Active when a message from the gardener that isn't the warning text has been sent`() {
        val gardener = getChannelStateCalculator(longIdlePeriodChannels)

        val channelMessages = listOf(gardenerNonWarningMessage)
        val history = ConversationHistoriesForTesting.withEmptyCursorToken(channelMessages)
        whenever(mockConversationsApi.channelHistory(any<Conversation>(), any())).doReturn(history)

        val state = gardener.determineChannelState(channel, gardenerUser)
        assertEquals(ChannelState.Active, state)
    }

    @Test
    fun `channels are Active when freshly created`() {
        val gardener = getChannelStateCalculator(longIdlePeriodChannels)

        val channelMessages = listOf(humanMessage)
        val history = ConversationHistoriesForTesting.withEmptyCursorToken(channelMessages)
        whenever(mockConversationsApi.channelHistory(any<Conversation>(), any())).doReturn(history)

        val state = gardener.determineChannelState(freshlyCreatedChannel, gardenerUser)
        assertEquals(ChannelState.Active, state)
    }


    private fun getChannelStateCalculator(longIdlePeriodChannels: Set<String>): ChannelStateCalculator {
        return ChannelStateCalculator(mockConversationsApi, clock, defaultIdlePeriod, longIdlePeriodChannels, longIdlePeriod, warningMessageContent)
    }

    private fun getNow(): Instant {
        val localDateTime = LocalDateTime.parse("04:30 PM, Sat 4/4/1992", DateTimeFormatter.ofPattern("hh:mm a, EEE M/d/uuuu", Locale.ENGLISH))
        val zonedDateTime = localDateTime.atZone(ZoneId.of("Europe/London"))
        return zonedDateTime.toInstant()
    }
}