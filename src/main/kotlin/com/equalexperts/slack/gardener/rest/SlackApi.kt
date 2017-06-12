package com.equalexperts.slack.gardener.rest

import com.equalexperts.slack.gardener.rest.decoders.SlackDecoder
import com.equalexperts.slack.gardener.rest.model.ChannelHistory
import com.equalexperts.slack.gardener.rest.model.ChannelInfo
import com.equalexperts.slack.gardener.rest.model.ChannelInfo.ChannelIdExpander
import com.equalexperts.slack.gardener.rest.model.ChannelList
import com.equalexperts.slack.gardener.rest.model.Timestamp
import feign.Feign
import feign.Param
import feign.RequestLine
import feign.jackson.JacksonEncoder
import feign.okhttp.OkHttpClient
import java.net.URI

interface SlackApi {
    @RequestLine("GET /api/channels.list?exclude_archived=true&exclude_members=true")
    fun listChannels() : ChannelList

//    @RequestLine("GET /api/auth.test")
//    fun authenticate() : AuthInfo

//    @RequestLine("GET /api/users.info?user={user}")
//    fun getUserInfo(@Param("user") userId: UserId) : UserInfo

    @RequestLine("GET /api/channels.history?channel={channel}&oldest={oldest}&count=1000")
    fun getChannelHistory(
        @Param("channel", expander = ChannelIdExpander::class) channel: ChannelInfo,
        @Param("oldest") oldest: Timestamp
    ): ChannelHistory

//    @RequestLine("GET /api/chat.postMessage?channel={channel}&username={user}&text={text}")
//    fun postMessage(
//        @Param("channel", expander = ChannelIdExpander::class) channel: ChannelInfo,
//        @Param("user", expander = User.UsernameExpander::class) user: User,
//        @Param("text") text: String
//    ): Unit

    @RequestLine("GET /api/channels.archive?channel={channel}")
    fun archiveChannel(
        @Param("channel", expander = ChannelIdExpander::class) channel: ChannelInfo
    ): Unit

    companion object {
        fun factory(uri: URI, token: String) : SlackApi {
            return feignBuilder()
                .requestInterceptor{ it.query("token", token) }
                .target(SlackApi::class.java, uri.toString())
//            return Feign.builder()
//                .client(OkHttpClient())
//                .encoder(JacksonEncoder(SlackDecoder.jackson))
//                .decoder(SlackDecoder(SlackDecoder.jackson))
//                .requestInterceptor{ it.query("token", token) }
//                .target(SlackApi::class.java, uri.toString())
        }
    }
}