package com.equalexperts.slack.gardener

import com.equalexperts.slack.api.auth.AuthSlackApi
import com.equalexperts.slack.api.channels.ChannelsSlackApi
import com.equalexperts.slack.api.conversations.ConversationApi
import com.equalexperts.slack.api.conversations.ConversationsSlackApi
import com.equalexperts.slack.api.rest.SlackBotApi
import com.equalexperts.slack.api.users.UsersSlackApi
import java.net.URI
import java.time.Clock
import java.time.Period

class GardenerFactory {


    fun build(slackUri: URI, slackOauthAccessToken: String, slackBotOauthAccessToken: String): Gardener {

        val channelsSlackApi = ChannelsSlackApi.factory(slackUri, slackOauthAccessToken, Thread::sleep)
        val authSlackApi = AuthSlackApi.factory(slackUri, slackBotOauthAccessToken, Thread::sleep)

        val usersSlackApi = UsersSlackApi.factory(slackUri, slackBotOauthAccessToken, Thread::sleep)

        val conversationsSlackApi = ConversationsSlackApi.factory(slackUri, slackOauthAccessToken, Thread::sleep)
        val conversationApi = ConversationApi(conversationsSlackApi)

        val slackBotApi = SlackBotApi.factory(slackUri, slackBotOauthAccessToken, Thread::sleep)

        val botUserId = authSlackApi.authenticate().id
        val botUser = usersSlackApi.getUserInfo(botUserId).user

        val clock = Clock.systemUTC()
        val defaultIdlePeriod = Period.ofMonths(3)
        val warningPeriod = Period.ofWeeks(1)
        val longIdlePeriod = Period.ofYears(1)

        val channelWhitelist = setOf("announcements", "ask-aws", "meta-Slack", "ee-alumni", "feedback-to-ee", "remembering_torben", "ber-flynn")
        val longIdlePeriodChannels = setOf("coderetreat", "pt-global-coderetreat", "sk-ee-trip")

        val warningMessage = """Hi <!channel>.
                |This channel hasn't been used in a while, so I’d like to archive it.
                |This will keep the list of channels smaller and help users find things more easily.
                |If you _don't_ want this channel to be archived, just post a message and I'll leave it alone for a while.
                |You can archive the channel now using the `/archive` command.
                |If nobody posts in a few days I will come back and archive the channel for you.""".trimMargin().replace('\n', ' ')

        return Gardener(channelsSlackApi, conversationApi , slackBotApi, botUser,  clock, defaultIdlePeriod, warningPeriod, channelWhitelist, longIdlePeriodChannels, longIdlePeriod, warningMessage)
    }
}