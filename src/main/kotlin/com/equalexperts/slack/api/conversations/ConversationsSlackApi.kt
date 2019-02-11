package com.equalexperts.slack.api.conversations

import com.equalexperts.slack.api.conversations.model.Conversation
import com.equalexperts.slack.api.conversations.model.ConversationHistory
import com.equalexperts.slack.api.conversations.model.ConversationList
import com.equalexperts.slack.api.conversations.model.ConversationMembers
import com.equalexperts.slack.api.rest.SlackRetrySupport
import com.equalexperts.slack.api.rest.SlackRetrySupport.SlackErrorDecoder
import com.equalexperts.slack.api.rest.feignBuilder
import com.equalexperts.slack.api.rest.model.Timestamp
import feign.Param
import feign.RequestLine
import org.slf4j.LoggerFactory
import java.net.URI

interface ConversationsSlackApi {
    @RequestLine("GET /api/conversations.list?exclude_archived=true&exclude_members=true&cursor={cursorValue}")
    fun list(@Param("cursorValue") cursorValue: String = ""): ConversationList

    @RequestLine("GET /api/conversations.members?channel={channel}&limit=1000&cursor={cursorValue}")
    fun members(@Param("channel") channelId: String,
                @Param("cursorValue") cursorValue: String = ""): ConversationMembers

    @RequestLine("GET /api/conversations.archive?channel={channel}")
    fun channelsArchive(
            @Param("channel", expander = Conversation.ChannelIdExpander::class) channel: Conversation
    )

    @RequestLine("GET /api/conversations.history?channel={channel}&oldest={oldest}&count=1000")
    fun channelHistory(
            @Param("channel", expander = Conversation.ChannelIdExpander::class) channel: Conversation,
            @Param("oldest") oldest: Timestamp
    ): ConversationHistory


    companion object {
        private val logger = LoggerFactory.getLogger(this::class.java.name)

        fun factory(uri: URI, token: String, sleeper: (Long) -> Unit): ConversationsSlackApi {
            return feignBuilder()
                    .requestInterceptor { it.query("token", token) }
                    .errorDecoder(SlackErrorDecoder())
                    .retryer(SlackRetrySupport(sleeper))
                    .target(ConversationsSlackApi::class.java, uri.toString())
        }

        fun listAll(conversationsSlackApi: ConversationsSlackApi): Set<Conversation> {

            logger.info("Retrieving Channels")

            val channels = mutableSetOf<Conversation>()

            var moreChannelsToList: Boolean
            var cursorValue = ""
            do {
                val channelList = conversationsSlackApi.list(cursorValue)
                val nextCursor = channelList.response_metadata.next_cursor

                logger.debug("Channels found, adding to list ${channelList.channels}")
                channels += channelList.channels

                if (!nextCursor.isBlank()) {
                    logger.debug("Found new cursor token to retrieve more channels from, using cursor token $nextCursor")
                    moreChannelsToList = true
                    cursorValue = nextCursor

                } else {
                    logger.debug("No new cursor token, all channels found")
                    moreChannelsToList = false
                }

            } while (moreChannelsToList)
            logger.info("${channels.size} channels found")
            return channels
        }
    }
}