package com.equalexperts.slack.api.conversations

import com.equalexperts.slack.api.conversations.model.ConversationHistory
import com.equalexperts.slack.api.rest.model.Message
import com.equalexperts.slack.api.rest.model.ResponseMetadata

object ConversationHistoriesForTesting {

    fun withEmptyCursorToken(messages: List<Message>) = ConversationHistory(false, messages, ResponseMetadata(""))
    fun withCursorToken(messages: List<Message>, cursor_token: String) = ConversationHistory(true, messages, ResponseMetadata(cursor_token))

}