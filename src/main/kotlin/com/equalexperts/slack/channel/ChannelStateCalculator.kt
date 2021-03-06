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


class ChannelStateCalculator(
        private val conversationSlackApi: ConversationsSlackApi,
        private val clock: Clock,
        private val defaultIdlePeriod: Period,
        private val longIdlePeriodChannels: Collection<String>,
        private val longIdlePeriod: Period,
        private val warningMessage: String) {

    private val logger = LoggerFactory.getLogger(this::class.java.name)

    private fun determineIdlePeriod(channel: Conversation): Period {
        if (longIdlePeriodChannels.contains(channel.name)) {
            return longIdlePeriod
        }
        return defaultIdlePeriod
    }


    fun determineChannelState(channel: Conversation, botUser: User): ChannelState {
        val idlePeriod = determineIdlePeriod(channel)
        val now = ZonedDateTime.now(clock)
        val timeLimit = now - idlePeriod

        val channelCreatedAfterTimeLimitThreshold = channel.created >= timeLimit
        if (channelCreatedAfterTimeLimitThreshold) {
            logger.debug("Channel ${channel.name} created before threshold $timeLimit, marking channel as active. Current Members Count ${channel.members}")
            return ChannelState.Active
        }

        var lastWarning: ZonedDateTime? = null
        var thresholdTimestamp = Timestamp(timeLimit)
        val nowTimestamp = Timestamp(now)

        do {
            logger.debug("Searching for human message in channel ${channel.name} from now $nowTimestamp to $thresholdTimestamp ")
            val history = conversationSlackApi.channelHistory(channel, thresholdTimestamp, nowTimestamp)
            val messages = history.messages

            val noMessagesSinceThreshold = messages.isEmpty()
            if (noMessagesSinceThreshold) {
                logger.debug("No messages since timestamp $thresholdTimestamp in ${channel.name}")
                break
            }

            val messageSentFromHumanBeingOrBotBeforeThreshold = !messages.none {
                val humanMessage = it.type == "message" && it.subtype == null
                val nonGardenerBotMessage = it.type == "message" && it.subtype == "bot_message" && it.bot_id != botUser.profile.bot_id
                val matchingMessageContent = it.type == "message" && it.subtype == "bot_message" && it.bot_id == botUser.profile.bot_id && it.text != warningMessage

                if (humanMessage || nonGardenerBotMessage || matchingMessageContent){
                    if (humanMessage){
                        logger.debug("Found a message on ${it.timestamp} since $thresholdTimestamp from a human being in ${channel.name}")
                    }
                    if (nonGardenerBotMessage){
                        logger.debug("Found a message on ${it.timestamp} since $thresholdTimestamp from a non-gardener bot in ${channel.name}")
                    }
                    if (matchingMessageContent){
                        logger.debug("Found a message on ${it.timestamp} since $thresholdTimestamp from the gardener bot but it wasn't the current warning message, in ${channel.name}")
                    }
                }
                humanMessage || nonGardenerBotMessage || matchingMessageContent
            }

            if (messageSentFromHumanBeingOrBotBeforeThreshold) {
                logger.debug("Found a message since $thresholdTimestamp in ${channel.name} that is valid to mark channel as active.  Members Count ${channel.members}")
                return ChannelState.Active //found a message typed by an actual human being or a non-gardener bot
            }

            val lastGardenerMessage = messages.findLast {
                it.bot_id == botUser.profile.bot_id && it.subtype == "bot_message"
            }
            lastWarning = lastGardenerMessage?.timestamp?.toZonedDateTime() ?: lastWarning
            thresholdTimestamp = messages.last().timestamp
        } while (history.has_more)

        if (lastWarning != null) {
            logger.debug("Channel ${channel.name} is stale and warned ${lastWarning}")
            return ChannelState.StaleAndWarned(lastWarning)
        }
        logger.debug("Channel ${channel.name} is stale and not warned")
        return ChannelState.Stale
    }

}
