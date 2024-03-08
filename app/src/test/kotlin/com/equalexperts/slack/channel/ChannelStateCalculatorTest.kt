package com.equalexperts.slack.channel

import com.equalexperts.slack.api.conversations.ConversationHistoriesForTesting
import com.equalexperts.slack.api.conversations.ConversationsSlackApi
import com.equalexperts.slack.api.conversations.model.Conversation
import com.equalexperts.slack.api.rest.model.Message
import com.equalexperts.slack.api.rest.model.SlackTestMessages
import com.equalexperts.slack.api.users.SlackTestUsers
import org.mockito.kotlin.any
import org.mockito.kotlin.doReturn
import org.mockito.kotlin.mock
import org.mockito.kotlin.whenever
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

    private val warningMessageContent = "WARNING MESSAGE"

    private val longIdlePeriodChannelName = "CHECK_YEARLY_CHANNEL_NAME"
    private val longIdlePeriodChannels = setOf(longIdlePeriodChannelName)

    private val channel = Conversation("TEST_ID", "CHANNEL_NAME", Instant.EPOCH.epochSecond, 1)
    private val longIdlePeriodChannel = Conversation("TEST_ID", longIdlePeriodChannelName, Instant.EPOCH.epochSecond, 1)

    // We have to mock time/clocks/etc, so all of these are initalised after we've put the relevant things in place
    private lateinit var testTime: GardenerRelativeTimeGenerator

    @BeforeEach
    fun setup() {
        mockConversationsApi = mock()
        clock = mock()
        mockChannelStateCalculator = mock()

        fun now(): Instant {
            val localDateTime = LocalDateTime.parse(
                "00:01 AM, Sat 1/1/2000",
                DateTimeFormatter.ofPattern("hh:mm a, EEE M/d/uuuu", Locale.ENGLISH)
            )
            val zonedDateTime = localDateTime.atZone(ZoneId.of("Europe/London"))
            return zonedDateTime.toInstant()
        }

        val nowInstant = now()

        whenever(clock.instant()).thenReturn(nowInstant)
        whenever(clock.zone).thenReturn(ZoneOffset.UTC)

        testTime = GardenerRelativeTimeGenerator(clock)
    }

    @Test
    fun `long idle channels with valid messages are active`() {
        val gardener = getChannelStateCalculator(longIdlePeriodChannels)

        val channelList = ConversationListsForTesting.withEmptyCursorToken(longIdlePeriodChannel)
        whenever(mockConversationsApi.list()).doReturn(channelList)

        val timeSent = testTime.afterLongIdleThreshold()
        val user = SlackTestUsers.humanUser()
        val message = SlackTestMessages.userMessage(
            user.name,
            user.profile.bot_id,
            timeSent,
            "TEST_MESSAGE"
        )

        val channelMessages = listOf(message)
        val history = ConversationHistoriesForTesting.withEmptyCursorToken(channelMessages)
        whenever(mockConversationsApi.channelHistory(any(), any(), any())).doReturn(history)

        val state = gardener.calculate(longIdlePeriodChannel, SlackTestUsers.gardenerUser())
        assertEquals(ChannelState.Active, state)
    }

    @Test
    fun `long idle channels with no valid messages are stale`() {
        val gardener = getChannelStateCalculator(longIdlePeriodChannels)

        val channelList = ConversationListsForTesting.withEmptyCursorToken(longIdlePeriodChannel)
        whenever(mockConversationsApi.list()).doReturn(channelList)

        val channelMessages = emptyList<Message>()
        val history = ConversationHistoriesForTesting.withEmptyCursorToken(channelMessages)
        whenever(mockConversationsApi.channelHistory(any(), any(), any())).doReturn(history)

        val state = gardener.calculate(longIdlePeriodChannel, SlackTestUsers.gardenerUser())
        assertEquals(ChannelState.Stale, state)
    }

    @Test
    fun `channels are StaleAndWarned when they have been warned for inactivity and have had no valid messages since being warned`() {
        val gardener = getChannelStateCalculator(longIdlePeriodChannels)

        val channelList = ConversationListsForTesting.withEmptyCursorToken(listOf(channel, longIdlePeriodChannel))
        whenever(mockConversationsApi.list()).doReturn(channelList)

        val timeSent = testTime.warningThreshold()
        val user = SlackTestUsers.gardenerUser()
        val warningMessage = SlackTestMessages.botMessage(
            user.id,
            user.profile.bot_id!!,
            timeSent,
            warningMessageContent
        )

        val channelMessages = listOf(warningMessage)
        val history = ConversationHistoriesForTesting.withEmptyCursorToken(channelMessages)
        whenever(mockConversationsApi.channelHistory(any(), any(), any())).doReturn(history)

        val state = gardener.calculate(channel, user)
        assertEquals(ChannelState.StaleAndWarned(timeSent)::class, state::class)
    }

    @Test
    fun `channels are Stale when no messages since the idle period`() {
        val gardener = getChannelStateCalculator(longIdlePeriodChannels)

        val channelList = ConversationListsForTesting.withEmptyCursorToken(channel)
        whenever(mockConversationsApi.list()).doReturn(channelList)

        val channelMessages = emptyList<Message>()
        val history = ConversationHistoriesForTesting.withEmptyCursorToken(channelMessages)
        whenever(mockConversationsApi.channelHistory(any(), any(), any())).doReturn(history)

        val state = gardener.calculate(channel, SlackTestUsers.gardenerUser())
        assertEquals(ChannelState.Stale, state)
    }

    @Test
    fun `channels are Active when a message from a non-gardener bot is sent`() {
        val gardener = getChannelStateCalculator(longIdlePeriodChannels)

        val channelList = ConversationListsForTesting.withEmptyCursorToken(channel)
        whenever(mockConversationsApi.list()).doReturn(channelList)

        val timeSent = testTime.afterIdleThreshold()
        val user = SlackTestUsers.nonGardenerBotUser()
        val message = SlackTestMessages.botMessage(
            user.id,
            user.profile.bot_id!!,
            timeSent,
            "TEST_MESSAGE"
        )

        val channelMessages = listOf(message)
        val history = ConversationHistoriesForTesting.withEmptyCursorToken(channelMessages)
        whenever(mockConversationsApi.channelHistory(any(), any(), any())).doReturn(history)

        val state = gardener.calculate(channel, SlackTestUsers.gardenerUser())
        assertEquals(ChannelState.Active, state)
    }

    @Test
    fun `channels are Active when a message from a human is sent`() {
        val gardener = getChannelStateCalculator(longIdlePeriodChannels)

        val timeSent = testTime.afterIdleThreshold()
        val user = SlackTestUsers.humanUser()
        val message = SlackTestMessages.userMessage(
            user.name,
            user.profile.bot_id,
            timeSent,
            "TEST_MESSAGE"
        )

        val channelMessages = listOf(message)
        val history = ConversationHistoriesForTesting.withEmptyCursorToken(channelMessages)
        whenever(mockConversationsApi.channelHistory(any(), any(), any())).doReturn(history)

        val state = gardener.calculate(channel, SlackTestUsers.gardenerUser())
        assertEquals(ChannelState.Active, state)
    }


    @Test
    fun `channels are Active when a message from the gardener that isn't the warning text has been sent`() {
        val gardener = getChannelStateCalculator(longIdlePeriodChannels)

        val timeSent = testTime.afterIdleThreshold()
        val user = SlackTestUsers.gardenerUser()
        val message = SlackTestMessages.botMessage(
            user.id,
            user.profile.bot_id!!,
            timeSent,
            "NON_WARNING_MESSAGE"
        )

        val channelMessages = listOf(message)
        val history = ConversationHistoriesForTesting.withEmptyCursorToken(channelMessages)
        whenever(mockConversationsApi.channelHistory(any(), any(), any())).doReturn(history)

        val state = gardener.calculate(channel, user)
        assertEquals(ChannelState.Active, state)
    }

    @Test
    fun `channels are Active when created before idle period and no messages`() {
        val gardener = getChannelStateCalculator(longIdlePeriodChannels)

        val channelMessages = emptyList<Message>()
        val history = ConversationHistoriesForTesting.withEmptyCursorToken(channelMessages)
        whenever(mockConversationsApi.channelHistory(any(), any(), any())).doReturn(history)

        val channelCreatedBeforeIdleThreshold =
            Conversation("TEST_ID", "CHANNEL_NAME", testTime.beforeNow().toInstant().epochSecond, 1)

        val state = gardener.calculate(channelCreatedBeforeIdleThreshold, SlackTestUsers.gardenerUser())
        assertEquals(ChannelState.Active, state)
    }

    @Test
    fun `calculator pages back in history when no valid messages in current page but more messages are available`() {
        val gardener = getChannelStateCalculator(longIdlePeriodChannels)

        val user = SlackTestUsers.humanUser()

        val firstMessageTimeSent = testTime.afterIdleThreshold()

        val firstMessages = listOf(
            SlackTestMessages.joinerMessage(user.name, firstMessageTimeSent),
            SlackTestMessages.leaverMessage(user.name, firstMessageTimeSent)
        )
        val firstHistoryPage = ConversationHistoriesForTesting.withCursorToken(firstMessages, "CURSOR_TOKEN")

        val secondMessageTimeSent = testTime.afterIdleThreshold()
        val secondMessage = SlackTestMessages.userMessage(
            user.name,
            user.profile.bot_id,
            secondMessageTimeSent,
            "TEST_MESSAGE"
        )
        val secondHistoryPage = ConversationHistoriesForTesting.withEmptyCursorToken(listOf(secondMessage))

        whenever(mockConversationsApi.channelHistory(any(), any(), any())).doReturn(
            firstHistoryPage,
            secondHistoryPage
        )
        // TODO assert that we actually page
        val state = gardener.calculate(channel, SlackTestUsers.gardenerUser())
        assertEquals(ChannelState.Active, state)
    }


    private fun getChannelStateCalculator(longIdlePeriodChannels: Set<String>): ChannelStateCalculator {
        return ChannelStateCalculator(
            mockConversationsApi,
            clock,
            testTime.idlePeriod,
            longIdlePeriodChannels,
            testTime.longIdlePeriod,
            warningMessageContent
        )
    }


}
