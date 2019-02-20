package com.equalexperts.slack.channel

import com.equalexperts.slack.api.chat.ChatSlackApi
import com.equalexperts.slack.api.conversations.ConversationsSlackApi
import com.equalexperts.slack.api.conversations.model.Conversation
import com.equalexperts.slack.api.users.UsersForTesting
import com.equalexperts.slack.profile.UserProfilesForTesting
import com.nhaarman.mockitokotlin2.mock
import com.nhaarman.mockitokotlin2.verify
import com.nhaarman.mockitokotlin2.whenever
import org.junit.jupiter.api.Test

internal class ChannelRenamerTest {

    @Test
    fun process() {
        val mockConversationApi = mock<ConversationsSlackApi>()
        val mockChatSlackApi = mock<ChatSlackApi>()

        val mockBotUser = UsersForTesting.testBot(UserProfilesForTesting.testBotProfile())

        val channelsToRename = mapOf(("rename_me" to "new_name"))

        val conversations = mutableListOf<Conversation>()
        for ((channelName, channelNewName) in channelsToRename) {
            conversations.add(Conversation("ID:$channelNewName", channelName, 1L, 2))
        }

        val conversationList = ConversationListsForTesting.withEmptyCursorToken(conversations)
        whenever(mockConversationApi.list()).thenReturn(conversationList)

        val message = "Hello :wave: This channel is being renamed to improve the discoverability, in line with the guidelines and prefixes in the Slack Usage Guide."
        ChannelRenamer(mockConversationApi, mockChatSlackApi, mockBotUser,
                message).process(channelsToRename)

        for (conversation in conversations) {
            verify(mockChatSlackApi).postMessage(conversation, mockBotUser, message)
            verify(mockConversationApi).channelRename(conversation, channelsToRename.getValue(conversation.name))
        }
    }
}