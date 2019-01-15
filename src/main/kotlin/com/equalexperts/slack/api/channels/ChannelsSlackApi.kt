package com.equalexperts.slack.api.channels

import com.equalexperts.slack.api.rest.SlackRetrySupport
import com.equalexperts.slack.api.rest.SlackRetrySupport.SlackErrorDecoder
import com.equalexperts.slack.api.rest.feignBuilder
import com.equalexperts.slack.api.conversations.model.Conversation
import com.equalexperts.slack.api.conversations.model.Conversation.ChannelIdExpander
import com.equalexperts.slack.api.rest.model.Timestamp
import feign.Param
import feign.RequestLine
import java.net.URI

interface ChannelsSlackApi {

    @RequestLine("GET /api/channels.history?channel={channel}&oldest={oldest}&count=1000")
    fun channelHistory(
            @Param("channel", expander = ChannelIdExpander::class) channel: Conversation,
            @Param("oldest") oldest: Timestamp
    ): ChannelHistory

    @RequestLine("GET /api/channels.archive?channel={channel}")
    fun channelsArchive(
            @Param("channel", expander = ChannelIdExpander::class) channel: Conversation
    )

    companion object {
        fun factory(uri: URI, token: String, sleeper: (Long) -> Unit): ChannelsSlackApi {
            return feignBuilder()
                    .requestInterceptor { it.query("token", token) }
                    .errorDecoder(SlackErrorDecoder())
                    .retryer(SlackRetrySupport(sleeper))
                    .target(ChannelsSlackApi::class.java, uri.toString())
        }
    }
}