package com.equalexperts.slack.api.conversations.model

import com.equalexperts.slack.api.rest.model.Message

class ConversationHistory(has_more: Boolean, val messages: List<Message>) {
    val hasMore = has_more
}