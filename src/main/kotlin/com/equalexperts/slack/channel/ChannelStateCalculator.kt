package com.equalexperts.slack.channel

import com.equalexperts.slack.api.conversations.ConversationsSlackApi
import com.equalexperts.slack.api.conversations.model.Conversation
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


class ChannelStateCalculator (
        private val conversationSlackApi: ConversationsSlackApi,
        private val clock: Clock,
        private val defaultIdlePeriod: Period,
        private val longIdlePeriodChannels: Collection<String>,
        private val longIdlePeriod: Period,
        private val warningMessage: String){

    private val logger = LoggerFactory.getLogger(this::class.java.name)

    private fun determineIdlePeriod(channel: Conversation): Period {
        if (longIdlePeriodChannels.contains(channel.name)) {
            return longIdlePeriod
        }
        return defaultIdlePeriod
    }


    fun determineChannelState(channel: Conversation, botUser: User): ChannelState {
        val idlePeriod = determineIdlePeriod(channel)
        val timeLimit = ZonedDateTime.now(clock) - idlePeriod

        val channelCreatedAfterTimeLimitThreshold = channel.created >= timeLimit
        if (channelCreatedAfterTimeLimitThreshold) {
            return ChannelState.Active //new channels count as active
        }

        //TODO: we start at the oldest time and page forward. Paging backward instead will be faster when a channel has already been warned.


        var lastWarning: ZonedDateTime? = null
        var timestamp = Timestamp(timeLimit)

        do {
            logger.debug("Searching for human message in channel ${channel.name} from $timestamp")
            val history = conversationSlackApi.channelHistory(channel, timestamp)
            val messages = history.messages

            val noMessagesSinceThreshold = messages.isEmpty()
            if (noMessagesSinceThreshold) {
                break
            }

            val messageSentFromHumanBeingOrBotBeforeThreshold = !messages.none {
                val humanMessage = it.type == "message" && it.subtype == null
                val nonGardenerBotMessage = it.type == "message" && it.subtype == "bot_message" && it.bot_id != botUser.profile.bot_id
                val matchingMessageContent = it.type == "message" && it.subtype == "bot_message" && it.bot_id == botUser.profile.bot_id && it.text != warningMessage
                humanMessage || nonGardenerBotMessage || matchingMessageContent
            }

            if (messageSentFromHumanBeingOrBotBeforeThreshold) {
                return ChannelState.Active //found a message typed by an actual human being or a non-gardener bot
            }

            val lastGardenerMessage = messages.findLast {
                it.bot_id == botUser.profile.bot_id && it.subtype == "bot_message"
            }
            lastWarning = lastGardenerMessage?.timestamp?.toZonedDateTime() ?: lastWarning
            timestamp = messages.last().timestamp
        } while (history.has_more)

        if (lastWarning != null) {
            return ChannelState.StaleAndWarned(lastWarning)
        }
        return ChannelState.Stale
    }

}