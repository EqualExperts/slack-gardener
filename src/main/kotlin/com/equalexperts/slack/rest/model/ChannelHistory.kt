package com.equalexperts.slack.rest.model

class ChannelHistory(has_more: Boolean, val messages: List<Message>) {
    val hasMore = has_more
}