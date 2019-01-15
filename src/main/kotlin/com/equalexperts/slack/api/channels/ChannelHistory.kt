package com.equalexperts.slack.api.channels

import com.equalexperts.slack.api.rest.model.Message

class ChannelHistory(has_more: Boolean, val messages: List<Message>) {
    val hasMore = has_more
}