package com.equalexperts.slack.api.conversations.model

import com.equalexperts.slack.api.rest.model.ResponseMetadata

data class ConversationList(val channels: List<Conversation>,
                            val response_metadata: ResponseMetadata) {
    companion object {
        fun withEmptyCursorToken(conversation: Conversation) = ConversationList(listOf(conversation), ResponseMetadata(""))
        fun withCursorToken(conversation: Conversation, cursor_token: String) = ConversationList(listOf(conversation), ResponseMetadata(cursor_token))
    }

}

