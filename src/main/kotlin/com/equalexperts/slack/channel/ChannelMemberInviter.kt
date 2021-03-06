package com.equalexperts.slack.channel

import com.amazonaws.services.simplesystemsmanagement.AWSSimpleSystemsManagementClientBuilder
import com.amazonaws.services.simplesystemsmanagement.model.GetParametersRequest
import com.equalexperts.slack.api.conversations.ConversationsSlackApi
import com.equalexperts.slack.api.conversations.listAll
import com.equalexperts.slack.api.users.UsersSlackApi
import com.equalexperts.slack.api.users.listAll
import org.slf4j.LoggerFactory
import java.net.URI


fun main() {
    val slackUri = URI("https://api.slack.com")

    val gardenerOauthAccessTokenParamName = "slack.gardener.oauth.access_token"
    val gardenerBotOauthAccessTokenParamName = "slack.gardener.bot.oauth.access_token"

    val client = AWSSimpleSystemsManagementClientBuilder.defaultClient()
    val request = GetParametersRequest()
    request.withNames(gardenerOauthAccessTokenParamName,
        gardenerBotOauthAccessTokenParamName
    ).withDecryption = true
    val parameterResults = client.getParameters(request)

    val slackOauthAccessToken = parameterResults.parameters.find { it.name == gardenerOauthAccessTokenParamName }?.value!!
    val slackBotOauthAccessToken = parameterResults.parameters.find { it.name == gardenerBotOauthAccessTokenParamName }?.value!!

    val conversationsSlackApi = ConversationsSlackApi.factory(slackUri, slackOauthAccessToken, Thread::sleep)

    val usersSlackApi = UsersSlackApi.factory(slackUri, slackBotOauthAccessToken, Thread::sleep)

    val peopleToImport = listOf("test@example.com")

    ChannelMemberImporter(conversationsSlackApi, usersSlackApi).process("announcements", peopleToImport)
}


class ChannelMemberImporter(private val conversationApi: ConversationsSlackApi, private val userApi: UsersSlackApi) {
    //Needs bot/incoming-webhook oauth permissions

    private val logger = LoggerFactory.getLogger(this::class.java.name)

    fun process(channelName: String, userEmails: List<String>) {
        logger.info("Finding users")
        val users = userApi.listAll()

        val channelId = conversationApi.listAll().first { it.name == channelName }.id

        val userIds = users.filter { it.profile.email in userEmails }
            .map { Pair(it, it.id) }
        logger.info("User Ids for emails: $userIds")
        logger.info("Inviting users to channel $channelName")

        userIds.map { it.second }
            .map {
                logger.info("Inviting user ${it} to channel $channelName")
                conversationApi.invite(channelId, listOf(it))
            }
        logger.info("Invited users to channel")

    }

}
