package com.equalexperts.slack.channel

import com.equalexperts.slack.api.auth.AuthSlackApi
import com.equalexperts.slack.api.chat.ChatSlackApi
import com.equalexperts.slack.api.conversations.ConversationsSlackApi
import com.equalexperts.slack.api.users.UsersSlackApi
import java.net.URI

class ChannelCheckerFactory {

    fun build(slackUri: URI, slackOauthAccessToken: String, slackBotOauthAccessToken: String): ChannelChecker {


        val authSlackApi = AuthSlackApi.factory(slackUri, slackOauthAccessToken, Thread::sleep)
        val usersSlackApi = UsersSlackApi.factory(slackUri, slackOauthAccessToken, Thread::sleep)

        val conversationsSlackApi = ConversationsSlackApi.factory(slackUri, slackOauthAccessToken, Thread::sleep)


        val slackBotApi = ChatSlackApi.factory(slackUri, slackBotOauthAccessToken, Thread::sleep)

        val userId = authSlackApi.authenticate().id
        val botUser = usersSlackApi.getUserInfo(userId)

        val channelWhitelist = setOf("announce", "ask-aws", "meta-Slack", "ee-alumni", "feedback-to-ee", "remembering_torben")

        val rules = setOf(ChannelChecker.HELP_RULE)

        val warningMessage = """Hi <!channel>.
                |The name of this channel doesn't meet our guidelines
                |Please consider a rename, as it helps it make the channel discoverable
                |Our guidelines are:
                |${rules.map { it -> it.toString() }}""".trimMargin().replace('\n', ' ')

        return ChannelChecker(conversationsSlackApi, slackBotApi, botUser, channelWhitelist, rules, warningMessage)
    }
}