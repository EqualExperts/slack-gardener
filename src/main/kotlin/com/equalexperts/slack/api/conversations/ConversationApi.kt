package com.equalexperts.slack.api.conversations

import com.equalexperts.slack.api.conversations.model.Conversation
import com.equalexperts.slack.api.conversations.model.ConversationMembers
import org.slf4j.LoggerFactory

class ConversationApi(private val conversationsSlackApi: ConversationsSlackApi) {

    private val logger = LoggerFactory.getLogger(this::class.java.name)

    fun list(): Set<Conversation> {

        logger.info("Retrieving Channels")

        var channels = setOf<Conversation>()

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

        }  while (moreChannelsToList)
        logger.info("${channels.size} channels found")
        return channels
    }

    fun members(conversation: Conversation): ConversationMembers {
        return conversationsSlackApi.members(conversation.id)
    }


}