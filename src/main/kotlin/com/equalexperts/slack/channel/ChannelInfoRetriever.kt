package com.equalexperts.slack.channel

import com.equalexperts.slack.gardener.rest.SlackApi
import com.equalexperts.slack.gardener.rest.model.ChannelInfo
import org.slf4j.LoggerFactory

class ChannelInfoRetriever(val slackApi: SlackApi) {

    private val logger = LoggerFactory.getLogger(this::class.java.name)

    fun getChannels(): Set<ChannelInfo> {

        logger.info("Retrieving Channels")

        var channels = setOf<ChannelInfo>()

        var moreChannelsToList = false
        var cursorValue : String = ""
        do {
            val channelList = slackApi.listChannels(cursorValue)
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


}