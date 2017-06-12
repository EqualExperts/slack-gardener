package com.equalexperts.slack.gardener

import com.equalexperts.slack.gardener.ChannelState.*
import com.equalexperts.slack.gardener.rest.model.ChannelInfo
import com.equalexperts.slack.gardener.rest.SlackApi
import com.equalexperts.slack.gardener.rest.SlackBotApi
import com.equalexperts.slack.gardener.rest.model.Timestamp
import java.time.Clock
import java.time.Period
import java.time.ZonedDateTime
import java.time.temporal.ChronoUnit.DAYS
import java.util.stream.Collectors

class Gardener (private val slackApi: SlackApi, private val slackBotApi: SlackBotApi, private val clock: Clock, private val idlePeriod: Period, private val warningPeriod: Period) {
    private companion object {
        val channelWhiteList = setOf("general", "announce", "ask-aws", "meta-slack", "random", "ee-alumni")
    }

    private val botUser by lazy {
        slackBotApi.getUserInfo(slackBotApi.authenticate().id).user
    }

    fun process() {
        val channels = slackApi.listChannels().channels
        println("${channels.size} channels found")

        val data = channels.parallelStream()
            .filter { this.isEligibleForGardening(it) }
            .map { Tuple(it, this.determineChannelState(it)) }
            .peek { (it, state) ->
                val staleMessage = when (state) {
                    Active -> "not stale"
                    Stale -> "stale"
                    is StaleAndWarned -> "stale and warned ${state.oldestWarning.fromNow()}"
                }
                println("\t${it.name}(id: ${it.id}, created ${it.created}, ${staleMessage}, ${it.members} members)")
            }
            .collect(Collectors.toList())

        val active = data.count { it.state == Active }
        val stale = data.count { it.state == Stale }
        val staleAndWarned = data.count { it.state is StaleAndWarned }
        val emptyChannels = data.count { it.channel.members == 0 }

        println()
        println("${data.size}\tchannels")
        println("${active}\tactive channels")
        println("${stale + staleAndWarned}\tstale channels (${staleAndWarned} warned)")
        println("${emptyChannels}\tempty channels")
        println()

        println("Posting warnings:")
        data.parallelStream().forEach { postWarning(it) }
        println()

        println("Archiving:")
        data.parallelStream().forEach { archive(it) }
        println()
    }

    private fun isEligibleForGardening(channel: ChannelInfo): Boolean {
        if (channelWhiteList.contains(channel.name)) {
            return false
        }

        return true
    }

    private fun determineChannelState(channel: ChannelInfo) : ChannelState {
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

            val lastBotMessage = messages.findLast { it.botId == botUser.profile.botId && it.subtype == "bot_message" }
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

        val warning = """Hi <!channel>.
        |This channel hasn't been used in a while, so I’d like to archive it.
        |This will keep the list of channels smaller and help users find things more easily.
        |If you _don't_ want this channel to be archived, just post a message and I'll leave it alone for a while.
        |You can archive the channel now using the `/archive` command.
        |If nobody posts in a few days I will come back and archive the channel for you.""".trimMargin().replace('\n', ' ')

        slackBotApi.postMessage(it.channel, botUser, warning)
        println("\t${it.channel.name}")
    }

    private fun archive(it: Tuple) {
        if (!(it.state is StaleAndWarned)) {
            return //not stale, or no warning issued yet
        }
        if (it.state.oldestWarning >= ZonedDateTime.now(clock) - warningPeriod) {
            return //warning hasn't been issued long enough ago
        }

        slackApi.archiveChannel(it.channel)
        println("\t${it.channel.name}")
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

    private data class Tuple(val channel: ChannelInfo, val state: ChannelState)
}