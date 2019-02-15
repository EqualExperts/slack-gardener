package com.equalexperts.slack.api.chat

import com.equalexperts.slack.api.conversations.model.Conversation
import com.equalexperts.slack.api.rest.SlackRetrySupport
import com.equalexperts.slack.api.rest.feignBuilder
import com.equalexperts.slack.api.users.model.User
import feign.Param
import feign.RequestLine
import java.net.URI

interface ChatSlackApi {

    @RequestLine("GET /api/chat.postMessage?channel={channel}&username={user}&text={text}")
    fun postMessage(
            @Param("channel", expander = Conversation.ChannelIdExpander::class) channel: Conversation,
            @Param("user", expander = User.UsernameExpander::class) user: User,
            @Param("text") text: String
    )

    @RequestLine("GET /api/chat.postMessage?channel={channel}&username={user}&text={text}")
    fun postMessage(
            @Param("channel") channelId: String,
            @Param("user", expander = User.UsernameExpander::class) user: User,
            @Param("text") text: String
    )

    companion object {
        fun factory(uri: URI, token: String, sleeper: (Long) -> Unit): ChatSlackApi {
            return feignBuilder()
                    .requestInterceptor { it.query("token", token) }
                    .errorDecoder(SlackRetrySupport.SlackErrorDecoder())
                    .retryer(SlackRetrySupport(sleeper))
                    .target(ChatSlackApi::class.java, uri.toString())
        }
    }
}