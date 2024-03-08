package com.equalexperts.slack.channel

import com.amazonaws.services.simplesystemsmanagement.AWSSimpleSystemsManagementClientBuilder
import com.amazonaws.services.simplesystemsmanagement.model.GetParametersRequest
import com.equalexperts.slack.api.auth.AuthSlackApi
import com.equalexperts.slack.api.chat.ChatSlackApi
import com.equalexperts.slack.api.conversations.ConversationsSlackApi
import com.equalexperts.slack.api.conversations.listAll
import com.equalexperts.slack.api.conversations.model.Conversation
import com.equalexperts.slack.api.users.UsersSlackApi
import com.equalexperts.slack.api.users.model.User
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


    val authSlackApi = AuthSlackApi.factory(slackUri, slackBotOauthAccessToken, Thread::sleep)

    val usersSlackApi = UsersSlackApi.factory(slackUri, slackBotOauthAccessToken, Thread::sleep)

    val conversationsSlackApi = ConversationsSlackApi.factory(slackUri, slackOauthAccessToken, Thread::sleep)

    val chatSlackApi = ChatSlackApi.factory(slackUri, slackBotOauthAccessToken, Thread::sleep)

    val botUserId = authSlackApi.authenticate().id
    val botUser = usersSlackApi.getUserInfo(botUserId).user

    val message = "Hello, I'm renaming this to improve the discoverability, in line with our guidelines and prefixes :male-farmer:"

    val channelsToRename = mapOf(("rename_me" to "new_name"))

    ChannelRenamer(conversationsSlackApi, chatSlackApi, botUser, message).process(channelsToRename)
}


class ChannelRenamer(private val conversationApi: ConversationsSlackApi,
                     private val chatSlackApi: ChatSlackApi,
                     private val botUser: User,
                     private val message: String) {

    private val logger = LoggerFactory.getLogger(this::class.java.name)

    fun process(channelsToRename: Map<String, String>) {
        val channels = conversationApi.listAll()

        val channelsToBeRenamed = channels
                .filter { channelsToRename.keys.contains(it.name) }
                .map { messageChannel(it) }
                .map { renameChannel(it, channelsToRename) }
        logger.info("$channelsToBeRenamed")
    }

    private fun renameChannel(it: Conversation, channelsToRename: Map<String, String>): Pair<Conversation, String?> {
        logger.info("Renaming channel ${it.name}")
        conversationApi.channelRename(it, channelsToRename.getValue(it.name))
        return Pair(it, channelsToRename[it.name])
    }

    private fun messageChannel(it: Conversation): Conversation {

        logger.info("Messaging channel ${it.name}")
        chatSlackApi.postMessage(it, botUser, message)
        return it
    }

}
