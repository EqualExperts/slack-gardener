package com.equalexperts.slack.gardener.rest

import com.equalexperts.slack.gardener.rest.decoders.SlackDecoder
import com.equalexperts.slack.gardener.rest.model.*
import feign.Feign
import feign.Param
import feign.RequestLine
import feign.jackson.JacksonEncoder
import feign.okhttp.OkHttpClient
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
    ): Unit

    companion object {
        fun factory(uri: URI, token: String) : SlackBotApi {
            return feignBuilder()
                .requestInterceptor{ it.query("token", token) }
                .target(SlackBotApi::class.java, uri.toString())
        }
    }
}