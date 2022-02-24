package com.equalexperts.slack.channel

import com.equalexperts.slack.api.conversations.ConversationsSlackApi
import com.equalexperts.slack.api.conversations.model.Conversation
import com.equalexperts.slack.api.rest.model.Message
import com.equalexperts.slack.api.rest.model.Timestamp
import com.equalexperts.slack.api.users.model.User
import org.slf4j.LoggerFactory
import java.time.Clock
import java.time.Period
import java.time.ZonedDateTime

sealed class ChannelState {
    /*
        A channel with at least one message from a human during the idle period
     */
    object Active : ChannelState()

    /*
        A channel without any messages from humans during the idle period
     */
    object Stale : ChannelState()

    /*
        A channel without no messages from humans and a warning (message) from this bot during the idle period
     */
    class StaleAndWarned(val oldestWarning: ZonedDateTime) : ChannelState()
}


class ChannelStateCalculator(
    private val conversationSlackApi: ConversationsSlackApi,
    private val clock: Clock,
    private val defaultIdlePeriod: Period,
    private val longIdlePeriodChannels: Collection<String>,
    private val longIdlePeriod: Period,
    private val warningMessage: String
) {
    private val logger = LoggerFactory.getLogger(this::class.java.name)

    private fun getIdlePeriod(channel: Conversation): Period {
        if (longIdlePeriodChannels.contains(channel.name)) {
            logger.debug("Long idle period needed for ${channel.name}")
            return longIdlePeriod
        }
        return defaultIdlePeriod
    }

    fun calculate(channel: Conversation, botUser: User): ChannelState {
        logger.debug("Checking state for ${channel.name}")
        val idlePeriod = getIdlePeriod(channel)

        val now = ZonedDateTime.now(clock)
        val timeLimit = now - idlePeriod

        val channelCreatedAfterTimeLimitThreshold = channel.created >= timeLimit
        if (channelCreatedAfterTimeLimitThreshold) {
            logger.debug("Channel ${channel.name} created before threshold $timeLimit, marking channel as active. Current Members Count ${channel.members}")
            return ChannelState.Active
        }

        val oldestTimestamp = Timestamp(timeLimit)
        val latestTimestamp = Timestamp(now)

        return findChannelState(channel, botUser, oldestTimestamp, latestTimestamp)
    }

    private fun findChannelState(
        channel: Conversation,
        botUser: User,
        oldestTimestamp: Timestamp,
        latestTimestamp: Timestamp
    ): ChannelState {
        logger.debug("Searching for valid message in channel ${channel.name} between $oldestTimestamp & $latestTimestamp ")
        val history = conversationSlackApi.channelHistory(channel, oldestTimestamp, latestTimestamp)
        val messages = history.messages

        // mark stale if we've reached the end of messages for this channel during the time period requested, or none returned from slack api
        if (!messagesPresent(messages, oldestTimestamp, channel)) {
            logger.debug("Channel ${channel.name} is stale (no messages found) and has not been warned")
            return ChannelState.Stale
        }

        val validMessage = findValidMessage(messages, botUser, oldestTimestamp, channel)

        if (!validMessage) {
            if (history.has_more) {
                // keep paging for more messages, starting with the timestamp of the oldest message from the current page
                return findChannelState(channel, botUser, oldestTimestamp, messages.last().timestamp)
            } else {
                // if we've run out of messages, then we need to check if we've seen a warning in the current page
                val lastWarning = findWarning(messages, botUser)
                if (lastWarning != null) {
                    logger.debug("Channel ${channel.name} is stale and was warned at ${lastWarning}")
                    return ChannelState.StaleAndWarned(lastWarning)
                }
                logger.debug("Channel ${channel.name} is stale (no valid messages found) and not warned")
                return ChannelState.Stale
            }
        }
        return ChannelState.Active
    }

    private fun findWarning(
        messages: List<Message>,
        botUser: User
    ): ZonedDateTime? {
        val lastGardenerMessage = messages.findLast {
            it.bot_id == botUser.profile.bot_id && it.subtype == "bot_message"
            //&& it.text != warningMessage
        }
        return lastGardenerMessage?.timestamp?.toZonedDateTime()
    }

    private fun findValidMessage(
        messages: List<Message>,
        botUser: User,
        oldestTimestamp: Timestamp,
        channel: Conversation
    ): Boolean {
        val validMessageSent = messages.any {
            val humanMessage = it.type == "message" && it.subtype == null
            if (humanMessage) logger.debug("Found a message at ${it.timestamp} since $oldestTimestamp from a human being in ${channel.name}")

            val nonGardenerBotMessage =
                it.type == "message" && it.subtype == "bot_message" && it.bot_id != botUser.profile.bot_id
            if (nonGardenerBotMessage) logger.debug("Found a message at ${it.timestamp} since $oldestTimestamp from a non-gardener bot in ${channel.name}")

            val oldGardenerWarning =
                it.type == "message" && it.subtype == "bot_message" && it.bot_id == botUser.profile.bot_id && it.text != warningMessage
            if (oldGardenerWarning) logger.debug("Found a message at ${it.timestamp} since $oldestTimestamp from the gardener bot but it wasn't the current warning message, in ${channel.name}")

            humanMessage || nonGardenerBotMessage || oldGardenerWarning
        }

        if (validMessageSent) logger.debug("Found a message since $oldestTimestamp in ${channel.name} that is valid to mark channel as active.  Members Count ${channel.members}")

        return validMessageSent
    }

    private fun messagesPresent(
        messages: List<Message>,
        oldestTimestamp: Timestamp,
        channel: Conversation
    ): Boolean {
        val messagesSinceThreshold = messages.isNotEmpty()
        if (!messagesSinceThreshold) logger.debug("No messages since timestamp $oldestTimestamp in ${channel.name}")
        return messagesSinceThreshold
    }

}
