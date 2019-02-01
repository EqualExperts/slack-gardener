package com.equalexperts.slack.gardener

import com.equalexperts.slack.api.auth.AuthSlackApi
import com.equalexperts.slack.api.conversations.ConversationsSlackApi
import com.equalexperts.slack.gardener.ChannelState.*
import com.equalexperts.slack.api.chat.ChatSlackApi
import com.equalexperts.slack.api.conversations.model.Conversation
import com.equalexperts.slack.api.rest.model.Timestamp
import com.equalexperts.slack.api.users.UsersSlackApi
import com.equalexperts.slack.api.users.model.User
import org.slf4j.LoggerFactory
import java.net.URI
import java.time.Clock
import java.time.Period
import java.time.ZonedDateTime
import java.time.temporal.ChronoUnit.DAYS
import java.util.stream.Collectors
import kotlin.system.measureNanoTime

class Gardener(private val conversationSlackApi: ConversationsSlackApi,
               private val slackBotApi: ChatSlackApi,
               private val botUser: User,
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
            val channels = ConversationsSlackApi.listAll(conversationSlackApi)
            logger.info("${channels.size} channels found")

            val data = channels.parallelStream()
                    .filter { this.isEligibleForGardening(it) }
                    .map { Pair(it, this.determineChannelState(it, botUser)) }
                    .peek { (it, state) ->
                        val staleMessage = when (state) {
                            Active -> "not stale"
                            Stale -> "stale"
                            is StaleAndWarned -> "stale and warned ${state.oldestWarning.fromNow()}"
                        }
                        logger.info("\t${it.name}(id: ${it.id}, created ${it.created}, $staleMessage, ${it.members} members)")
                    }
                    .collect(Collectors.toList())

            val active = data.count { it.second == Active }
            val stale = data.count { it.second == Stale }
            val staleAndWarned = data.count { it.second is StaleAndWarned }
            val emptyChannels = data.count { it.first.members == 0 }


            logger.info("${data.size}\tchannels")
            logger.info("$active\tactive channels")
            logger.info("${stale + staleAndWarned}\tstale channels ($staleAndWarned warned)")
            logger.info("$emptyChannels\tempty channels")


            logger.info("Posting warnings:")
            data.parallelStream().forEach { postWarning(it) }


            logger.info("Archiving:")
            data.parallelStream().forEach { archive(it) }

        }

        logger.info("done in ${nanoTime / 1_000_000} ms")
    }

    private fun isEligibleForGardening(channel: Conversation): Boolean {
        if (channelWhiteList.contains(channel.name)) {
            return false
        }

        return true
    }

    private fun determineChannelState(channel: Conversation, botUser : User): ChannelState {
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
            val history = conversationSlackApi.channelHistory(channel, timestamp)
            val messages = history.messages

            val noMessagesSinceThreshold = messages.isEmpty()
            if (noMessagesSinceThreshold) {
                break
            }

            val messageSentFromHumanBeingOrBotBeforeThreshold = !messages.none {
                val humanMessage = it.type == "message" && it.subtype == null
                val nonGardenerBotMessage = it.type == "message" && it.subtype == "bot_message" &&  it.bot_id != botUser.profile.botId
                humanMessage || nonGardenerBotMessage
            }

            if (messageSentFromHumanBeingOrBotBeforeThreshold) {
                return ChannelState.Active //found a message typed by an actual human being or a non-gardener bot
            }

            val lastGardenerMessage = messages.findLast {
                it.bot_id == botUser.profile.botId && it.subtype == "bot_message"
            }
            lastWarning = lastGardenerMessage?.timestamp?.toZonedDateTime() ?: lastWarning
            timestamp = messages.last().timestamp
        } while (history.hasMore)

        if (lastWarning != null) {
            return ChannelState.StaleAndWarned(lastWarning)
        }
        return ChannelState.Stale
    }

    private fun postWarning(it: Pair<Conversation, ChannelState>) {
        if (it.second != Stale) {
            return
        }

        slackBotApi.postMessage(it.first, botUser, warningMessage)
        logger.info("Warned ${it.first.name}")
    }

    private fun archive(it: Pair<Conversation, ChannelState>) {
        if (it.second !is StaleAndWarned) {
            return //not stale, or no warning issued yet
        }
        val warningThreshold = ZonedDateTime.now(clock) - warningPeriod
        if ((it.second as StaleAndWarned).oldestWarning >= warningThreshold) {
            return //warning hasn't been issued long enough ago
        }

        conversationSlackApi.channelsArchive(it.first)
        logger.info("Archived ${it.first.name}")
    }

    //a-la moment.js
    private fun ZonedDateTime.fromNow(): String {
        val daysAgo = DAYS.between(this, ZonedDateTime.now(clock))
        return when (daysAgo) {
            0L -> "less than a day ago"
            1L -> "1 day ago"
            else -> "$daysAgo days ago"
        }
    }

    private fun determineIdlePeriod(channel: Conversation): Period {
        if (longIdlePeriodChannels.contains(channel.name)) {
            return longIdlePeriod
        }
        return defaultIdlePeriod
    }



    companion object {
        fun build(slackUri: URI,
                  slackOauthAccessToken: String,
                  slackBotOauthAccessToken: String,
                  idleMonths: Int,
                  warningWeeks: Int,
                  longIdleYears: Int,
                  channelWhitelist: Set<String>,
                  longIdlePeriodChannels: Set<String>,
                  warningMessage: String): Gardener {


            val authSlackApi = AuthSlackApi.factory(slackUri, slackBotOauthAccessToken, Thread::sleep)

            val usersSlackApi = UsersSlackApi.factory(slackUri, slackBotOauthAccessToken, Thread::sleep)

            val conversationsSlackApi = ConversationsSlackApi.factory(slackUri, slackOauthAccessToken, Thread::sleep)


            val slackBotApi = ChatSlackApi.factory(slackUri, slackBotOauthAccessToken, Thread::sleep)

            val botUserId = authSlackApi.authenticate().id
            val botUser = usersSlackApi.getUserInfo(botUserId).user

            val clock = Clock.systemUTC()
            val defaultIdlePeriod = Period.ofMonths(idleMonths)
            val warningPeriod = Period.ofWeeks(warningWeeks)
            val longIdlePeriod = Period.ofYears(longIdleYears)

            return Gardener(conversationsSlackApi , slackBotApi, botUser,  clock, defaultIdlePeriod, warningPeriod, channelWhitelist, longIdlePeriodChannels, longIdlePeriod, warningMessage)
        }
    }
}