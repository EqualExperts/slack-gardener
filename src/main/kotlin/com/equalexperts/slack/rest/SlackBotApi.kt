package com.equalexperts.slack.rest

import com.equalexperts.slack.rest.model.*
import feign.Param
import feign.RequestLine
import java.net.URI

interface SlackBotApi {
    @RequestLine("GET /api/auth.test")
    fun authenticate() : AuthInfo

    @RequestLine("GET /api/users.info?user={user}")
    fun getUserInfo(@Param("user") userId: UserId) : UserInfo

    @RequestLine("GET /api/chat.postMessage?channel={channel}&username={user}&text={text}")
    fun postMessage(
        @Param("channel", expander = ChannelInfo.ChannelIdExpander::class) channel: ChannelInfo,
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