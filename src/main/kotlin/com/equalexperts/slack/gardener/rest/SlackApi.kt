package com.equalexperts.slack.gardener.rest

import com.equalexperts.slack.gardener.rest.SlackRetrySupport.SlackErrorDecoder
import com.equalexperts.slack.gardener.rest.model.ChannelHistory
import com.equalexperts.slack.gardener.rest.model.ChannelInfo
import com.equalexperts.slack.gardener.rest.model.ChannelInfo.ChannelIdExpander
import com.equalexperts.slack.gardener.rest.model.ChannelList
import com.equalexperts.slack.gardener.rest.model.Timestamp
import feign.Param
import feign.RequestLine
import java.net.URI

interface SlackApi {
    @RequestLine("GET /api/channels.list?exclude_archived=true&exclude_members=true&cursor={cursorValue}")
    fun listChannels(@Param("cursorValue") cursorValue: String = "") : ChannelList

    @RequestLine("GET /api/channels.history?channel={channel}&oldest={oldest}&count=1000")
    fun getChannelHistory(
        @Param("channel", expander = ChannelIdExpander::class) channel: ChannelInfo,
        @Param("oldest") oldest: Timestamp
    ): ChannelHistory

    @RequestLine("GET /api/channels.archive?channel={channel}")
    fun archiveChannel(
        @Param("channel", expander = ChannelIdExpander::class) channel: ChannelInfo
    )

    companion object {
        fun factory(uri: URI, token: String, sleeper: (Long) -> Unit) : SlackApi {
            return feignBuilder()
                .requestInterceptor{ it.query("token", token) }
                .errorDecoder(SlackErrorDecoder())
                .retryer(SlackRetrySupport(sleeper))
                .target(SlackApi::class.java, uri.toString())
        }
    }
}