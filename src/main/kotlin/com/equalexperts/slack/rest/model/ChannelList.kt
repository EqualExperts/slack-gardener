package com.equalexperts.slack.rest.model

data class ChannelList(val channels: List<ChannelInfo>,
                       val response_metadata: ResponseMetadata)

data class ResponseMetadata(val next_cursor: String)
