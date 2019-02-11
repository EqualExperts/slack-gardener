package com.equalexperts.slack.api.conversations.model

import com.equalexperts.slack.api.rest.model.Expander
import com.equalexperts.slack.api.rest.model.ResponseMetadata
import java.time.Instant
import java.time.ZoneOffset.UTC
import java.time.ZonedDateTime

class Conversation(val id: String, val name: String, created: Long, num_members: Int) {

    val created = ZonedDateTime.ofInstant(Instant.ofEpochSecond(created), UTC)

    val members = num_members

    class ChannelIdExpander : Expander<Conversation>() {
        override fun expandParameter(value: Conversation) = value.id
    }

    override fun toString(): String {
        return "Conversation(id='$id', name='$name', created=$created, members=$members)"
    }
}


data class ConversationList(val channels: List<Conversation>,
                            val response_metadata: ResponseMetadata) {
    companion object {
        fun withEmptyCursorToken(conversation: Conversation) = ConversationList(listOf(conversation), ResponseMetadata(""))
        fun withCursorToken(conversation: Conversation, cursor_token: String) = ConversationList(listOf(conversation), ResponseMetadata(cursor_token))
    }

}