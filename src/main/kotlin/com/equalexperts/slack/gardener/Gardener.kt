package com.equalexperts.slack.gardener

import com.equalexperts.slack.channel.ChannelRetriever
import com.equalexperts.slack.gardener.ChannelState.*
import com.equalexperts.slack.gardener.rest.SlackApi
import com.equalexperts.slack.gardener.rest.SlackBotApi
import com.equalexperts.slack.gardener.rest.model.ChannelInfo
import com.equalexperts.slack.gardener.rest.model.Timestamp
import com.equalexperts.slack.gardener.rest.model.User
import org.slf4j.LoggerFactory
import java.time.Clock
import java.time.Period
import java.time.ZonedDateTime
import java.time.temporal.ChronoUnit.DAYS
import java.util.stream.Collectors
import kotlin.system.measureNanoTime

class Gardener(private val slackApi: SlackApi,
               private val slackBotApi: SlackBotApi,
               private val clock: Clock,
               private val defaultIdlePeriod: Period,
               private val warningPeriod: Period,
               private val channelWhiteList: Set<String>,
               private val longIdlePeriodChannels: Set<String>,
               private val longIdlePeriod: Period,
               private val warningMessage: String) {
    private val logger = LoggerFactory.getLogger(this::class.java.name)

    fun process() {
        val nanoTime = measureNanoTime {
            val channels = ChannelRetriever(slackApi).getChannels()
            logger.info("${channels.size} channels found")
            val botUser = getBotUser()

            val data = channels.parallelStream()
                    .filter { this.isEligibleForGardening(it) }
                    .map { Tuple(it, this.determineChannelState(it, botUser)) }
                    .peek { (it, state) ->
                        val staleMessage = when (state) {
                            Active -> "not stale"
                            Stale -> "stale"
                            is StaleAndWarned -> "stale and warned ${state.oldestWarning.fromNow()}"
                        }
                        logger.info("\t${it.name}(id: ${it.id}, created ${it.created}, $staleMessage, ${it.members} members)")
                    }
                    .collect(Collectors.toList())

            val active = data.count { it.state == Active }
            val stale = data.count { it.state == Stale }
            val staleAndWarned = data.count { it.state is StaleAndWarned }
            val emptyChannels = data.count { it.channel.members == 0 }


            logger.info("${data.size}\tchannels")
            logger.info("${active}\tactive channels")
            logger.info("${stale + staleAndWarned}\tstale channels ($staleAndWarned warned)")
            logger.info("${emptyChannels}\tempty channels")


            logger.info("Posting warnings:")
            data.parallelStream().forEach { postWarning(it) }


            logger.info("Archiving:")
            data.parallelStream().forEach { archive(it) }

        }

        logger.info("done in ${nanoTime / 1_000_000} ms")
    }

    private fun isEligibleForGardening(channel: ChannelInfo): Boolean {
        if (channelWhiteList.contains(channel.name)) {
            return false
        }

        return true
    }

    private fun determineChannelState(channel: ChannelInfo, botUser : User): ChannelState {
        val idlePeriod = determineIdlePeriod(channel)
        val timeLimit = ZonedDateTime.now(clock) - idlePeriod
        if (channel.created >= timeLimit) {
            return ChannelState.Active //new channels count as active
        }

        //TODO: we start at the oldest time and page forward. Paging backward instead will be faster when a channel has already been warned.

        var lastWarning: ZonedDateTime? = null
        var timestamp = Timestamp(timeLimit)

        do {
            val history = slackApi.getChannelHistory(channel, timestamp)
            val messages = history.messages

            if (messages.isEmpty()) {
                break
            }

            if (!messages.none { it.type == "message" && it.subtype == null }) {
                return ChannelState.Active //found a message typed by an actual human being
            }

            val lastBotMessage = messages.findLast {
                it.botId == botUser.profile.botId && it.subtype == "bot_message"
            }
            lastWarning = lastBotMessage?.timestamp?.toZonedDateTime() ?: lastWarning
            timestamp = messages.last().timestamp
        } while (history.hasMore)

        if (lastWarning != null) {
            return ChannelState.StaleAndWarned(lastWarning)
        }
        return ChannelState.Stale
    }

    private fun postWarning(it: Tuple) {
        if (it.state != Stale) {
            return
        }

        slackBotApi.postMessage(it.channel, getBotUser(), warningMessage)
        logger.info("Warned ${it.channel.name}")
    }

    private fun archive(it: Tuple) {
        if (it.state !is StaleAndWarned) {
            return //not stale, or no warning issued yet
        }
        val warningThreshold = ZonedDateTime.now(clock) - warningPeriod
        if (it.state.oldestWarning >= warningThreshold) {
            return //warning hasn't been issued long enough ago
        }

        slackApi.archiveChannel(it.channel)
        logger.info("Archived ${it.channel.name}")
    }

    private fun getBotUser(): User {
        val userId = slackBotApi.authenticate().id
        return slackBotApi.getUserInfo(userId).user
    }

    //a-la moment.js
    private fun ZonedDateTime.fromNow(): String {
        val daysAgo = DAYS.between(this, ZonedDateTime.now(clock))
        return when (daysAgo) {
            0L -> "less than a day ago"
            1L -> "1 day ago"
            else -> "${daysAgo} days ago"
        }
    }

    private fun determineIdlePeriod(channel: ChannelInfo): Period {
        if (longIdlePeriodChannels.contains(channel.name)) {
            return longIdlePeriod
        }
        return defaultIdlePeriod
    }

    private data class Tuple(val channel: ChannelInfo, val state: ChannelState)
}