package com.equalexperts.slack.gardener

import com.equalexperts.slack.api.auth.AuthSlackApi
import com.equalexperts.slack.api.chat.ChatSlackApi
import com.equalexperts.slack.api.conversations.ConversationsSlackApi
import com.equalexperts.slack.api.conversations.model.Conversation
import com.equalexperts.slack.api.rest.model.Timestamp
import com.equalexperts.slack.api.users.UsersSlackApi
import com.equalexperts.slack.api.users.model.User
import com.equalexperts.slack.gardener.ChannelState.*
import org.slf4j.LoggerFactory
import java.net.URI
import java.time.Clock
import java.time.Period
import java.time.ZonedDateTime
import java.time.temporal.ChronoUnit.DAYS
import java.util.stream.Collectors
import kotlin.system.measureNanoTime

class Gardener(private val conversationSlackApi: ConversationsSlackApi,
               private val chatSlackApi: ChatSlackApi,
               private val botUser: User,
               private val clock: Clock,
               private val channelStateCalculator: ChannelStateCalculator,
               private val warningPeriod: Period,
               private val warningMessage: String) {

    private val logger = LoggerFactory.getLogger(this::class.java.name)

    fun process() {
        val nanoTime = measureNanoTime {
            val channels = ConversationsSlackApi.listAll(conversationSlackApi)
            logger.info("${channels.size} channels found")

            val data = channels.parallelStream()
                    .map { Pair(it, channelStateCalculator.determineChannelState(it, botUser)) }
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


    private fun postWarning(it: Pair<Conversation, ChannelState>) {
        if (it.second != Stale) {
            return
        }

        chatSlackApi.postMessage(it.first, botUser, warningMessage)
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


    companion object {
        fun build(slackUri: URI,
                  slackOauthAccessToken: String,
                  slackBotOauthAccessToken: String,
                  idleMonths: Int,
                  warningWeeks: Int,
                  longIdleYears: Int,
                  longIdlePeriodChannels: Collection<String>,
                  warningMessage: String): Gardener {

            val authSlackApi = AuthSlackApi.factory(slackUri, slackBotOauthAccessToken, Thread::sleep)

            val usersSlackApi = UsersSlackApi.factory(slackUri, slackBotOauthAccessToken, Thread::sleep)

            val conversationsSlackApi = ConversationsSlackApi.factory(slackUri, slackOauthAccessToken, Thread::sleep)


            val chatSlackApi = ChatSlackApi.factory(slackUri, slackBotOauthAccessToken, Thread::sleep)

            val botUserId = authSlackApi.authenticate().id
            val botUser = usersSlackApi.getUserInfo(botUserId).user

            val clock = Clock.systemUTC()
            val defaultIdlePeriod = Period.ofMonths(idleMonths)
            val warningPeriod = Period.ofWeeks(warningWeeks)
            val longIdlePeriod = Period.ofYears(longIdleYears)

            val channelStateCalculator = ChannelStateCalculator(conversationsSlackApi, clock, defaultIdlePeriod, longIdlePeriodChannels, longIdlePeriod, warningMessage)

            return Gardener(conversationsSlackApi, chatSlackApi, botUser, clock, channelStateCalculator, warningPeriod, warningMessage)
        }
    }
}
