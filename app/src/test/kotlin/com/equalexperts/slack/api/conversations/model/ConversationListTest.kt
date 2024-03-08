package com.equalexperts.slack.api.conversations.model

import com.equalexperts.slack.channel.ConversationListsForTesting
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import java.time.Instant

internal class ConversationListTest {
    @Test
    fun `should create list with empty cursor`() {
        val testChannel = Conversation("TEST_ID", "CHANNEL_NAME", Instant.EPOCH.epochSecond, 1)

        val conversationList = ConversationListsForTesting.withEmptyCursorToken(testChannel)

        assertTrue(conversationList.response_metadata.next_cursor.isBlank())
    }

    @Test
    fun `should create list with cursor`() {
        val testChannel = Conversation("TEST_ID", "CHANNEL_NAME", Instant.EPOCH.epochSecond, 1)

        val cursor_token = "TOKEN"

        val conversationList = ConversationListsForTesting.withCursorToken(testChannel, cursor_token)

        assertEquals(conversationList.response_metadata.next_cursor, cursor_token)
    }
}