package com.equalexperts.slack.api.conversations.model

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import java.time.Instant

internal class ConversationListTest {
    @Test
    fun `should create list with empty cursor`() {
        val testChannel = Conversation("TEST_ID", "CHANNEL_NAME", Instant.EPOCH.epochSecond, 1)

        val conversationList = ConversationList.withEmptyCursorToken(testChannel)

        assertTrue(conversationList.response_metadata.next_cursor.isBlank())
    }

    @Test
    fun `should create list with cursor`() {
        val testChannel = Conversation("TEST_ID", "CHANNEL_NAME", Instant.EPOCH.epochSecond, 1)

        val cursor_token = "TOKEN"

        val conversationList = ConversationList.withCursorToken(testChannel, cursor_token)

        assertEquals(conversationList.response_metadata.next_cursor, cursor_token)
    }
}