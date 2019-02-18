package com.equalexperts.slack.channel

import com.equalexperts.slack.api.conversations.model.Conversation
import com.equalexperts.slack.api.conversations.model.ConversationList
import com.equalexperts.slack.api.rest.model.ResponseMetadata

object ConversationListsForTesting {

    fun withEmptyCursorToken(conversation: Conversation) = ConversationList(listOf(conversation), ResponseMetadata(""))
    fun withCursorToken(conversation: Conversation, cursor_token: String) = ConversationList(listOf(conversation), ResponseMetadata(cursor_token))


}