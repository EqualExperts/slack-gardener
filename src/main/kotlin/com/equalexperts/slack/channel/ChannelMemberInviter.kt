package com.equalexperts.slack.channel

import com.amazonaws.services.simplesystemsmanagement.AWSSimpleSystemsManagementClientBuilder
import com.amazonaws.services.simplesystemsmanagement.model.GetParametersRequest
import com.equalexperts.slack.api.conversations.ConversationsSlackApi
import com.equalexperts.slack.api.users.UsersSlackApi
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

    val peopleToImport = emptyList<String>()

    ChannelMemberImporter(conversationsSlackApi, usersSlackApi).process("uks", peopleToImport)
}


class ChannelMemberImporter(private val conversationApi: ConversationsSlackApi, private val userApi: UsersSlackApi) {

    private val logger = LoggerFactory.getLogger(this::class.java.name)

    fun process(channelName: String, userEmails: List<String>) {
        logger.info("Finding users")
        val users = UsersSlackApi.listAll(userApi)

        val channelId = ConversationsSlackApi.listAll(conversationApi).first { it.name == channelName }.id

        val userIds = users.filter { it.profile.email in userEmails }
                .map { Pair(it, it.id) }
        logger.info("User Ids for emails: $userIds")
        logger.info("Inviting users to channel")

        userIds.map { it.second }
                .chunked(30)
                .map {
                    logger.info("Inviting users to channel $it")

                    conversationApi.invite(channelId, it)
                }
        logger.info("Invited users to channel")

    }

}
