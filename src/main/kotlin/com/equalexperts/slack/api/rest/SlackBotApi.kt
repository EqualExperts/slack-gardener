package com.equalexperts.slack.api.rest

import com.equalexperts.slack.api.conversations.model.Conversation
import com.equalexperts.slack.api.users.model.User
import feign.Param
import feign.RequestLine
import java.net.URI

interface SlackBotApi {

    @RequestLine("GET /api/chat.postMessage?channel={channel}&username={user}&text={text}")
    fun postMessage(
            @Param("channel", expander = Conversation.ChannelIdExpander::class) channel: Conversation,
            @Param("user", expander = User.UsernameExpander::class) user: User,
            @Param("text") text: String
    )

    companion object {
        fun factory(uri: URI, token: String, sleeper: (Long) -> Unit) : SlackBotApi {
            return feignBuilder()
                .requestInterceptor{ it.query("token", token) }
                .errorDecoder(SlackRetrySupport.SlackErrorDecoder())
                .retryer(SlackRetrySupport(sleeper))
                .target(SlackBotApi::class.java, uri.toString())
        }
    }
}