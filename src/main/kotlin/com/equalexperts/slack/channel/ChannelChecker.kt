package com.equalexperts.slack.channel

import com.equalexperts.slack.api.chat.ChatSlackApi
import com.equalexperts.slack.api.conversations.ConversationsSlackApi
import com.equalexperts.slack.api.conversations.model.Conversation
import com.equalexperts.slack.api.users.model.UserInfo
import org.slf4j.LoggerFactory
import java.util.stream.Collectors
import kotlin.system.measureNanoTime

class ChannelChecker(private val conversationApi: ConversationsSlackApi,
                     private val slackBotApi: ChatSlackApi,
                     private val userInfo: UserInfo,
                     private val channelWhiteList: Set<String>,
                     private val channelNamingRules: Set<ChannelNamingRule>,
                     private val warningMessage: String) {

    private val logger = LoggerFactory.getLogger(this::class.java.name)

    class ChannelNamingRule(val prefix: String, val ruleDescription: String) {

        fun isChannelValid(channel: Conversation): Boolean {
            return channel.name.startsWith(this.prefix)
        }

        override fun toString(): String {
            return ruleDescription
        }
    }

    companion object {
        val HELP_RULE = ChannelNamingRule("help-", "#help- for channels where people can go to ask for help on a topic")
        val COMM_RULE = ChannelNamingRule("comm-", "#comm- for communities to talk about specific interests")
    }

    fun process() {
        val nanoTime = measureNanoTime {
            val channels = ConversationsSlackApi.listAll(conversationApi)
            logger.info("${channels.size} channels found")

            val channelsToBeWarned = channels.parallelStream()
                    .filter { this.isEligibleChannel(it) }
                    .filter { this.doesNotFollowAnyOfTheRules(it) }
                    .peek { logger.info("Does not follow rules ${it.name}") }
                    .collect(Collectors.toList())

            channelsToBeWarned.parallelStream().forEach { postWarning(it) }
        }

        logger.info("done in ${nanoTime / 1_000_000} ms")
    }

    private fun postWarning(channel: Conversation) {
        logger.info("Warning channel: ${channel.name}")
        slackBotApi.postMessage(channel, userInfo.user, this.warningMessage)
    }

    private fun doesNotFollowAnyOfTheRules(channel: Conversation): Boolean {
        if (channelNamingRules.isEmpty()) {
            return false
        }
        return channelNamingRules.none { rule -> channel.name.startsWith(rule.prefix) }
    }

    private fun isEligibleChannel(channel: Conversation): Boolean {
        if (channelWhiteList.contains(channel.name)) {
            return false
        }
        return true
    }
}