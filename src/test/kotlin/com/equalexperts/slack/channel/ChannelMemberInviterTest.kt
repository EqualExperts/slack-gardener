package com.equalexperts.slack.channel

import com.equalexperts.slack.api.conversations.ConversationsSlackApi
import com.equalexperts.slack.api.conversations.model.Conversation
import com.equalexperts.slack.api.users.SlackTestUsers
import com.equalexperts.slack.api.users.UsersSlackApi
import com.equalexperts.slack.api.users.model.UserListsForTesting
import com.equalexperts.slack.profile.SlackTestProfiles
import com.nhaarman.mockitokotlin2.mock
import com.nhaarman.mockitokotlin2.verify
import com.nhaarman.mockitokotlin2.whenever
import org.junit.jupiter.api.Test
import java.time.Instant

internal class ChannelMemberInviterTest {

    @Test
    fun `should invite emails to channel`() {
        val mockConversationApi = mock<ConversationsSlackApi>()
        val mockUsersApi = mock<UsersSlackApi>()

        val numberOfUsersToCreate = 31
        val users = (0..numberOfUsersToCreate).map {
            val profile = SlackTestProfiles.userProfile().copy(email = "test$it@email.com")
            SlackTestUsers.testUser(profile = profile).copy(id = "id_$it")
        }

        whenever(mockUsersApi.list()).thenReturn(UserListsForTesting.withEmptyCursorToken(users))

        val conversation = Conversation("TEST_CONVERSATION_ID", "TEST_CONVERSATION_NAME", Instant.EPOCH.epochSecond, 1)
        whenever(mockConversationApi.list()).thenReturn(ConversationListsForTesting.withEmptyCursorToken(conversation))

        val channelMemberImporter = ChannelMemberImporter(mockConversationApi, mockUsersApi)
        val usersToInvite = (0..numberOfUsersToCreate).map { "test$it@email.com" }

        channelMemberImporter.process(conversation.name, usersToInvite)

        val userIds = users.map { it.id }

        userIds.map { verify(mockConversationApi).invite(conversation.id, listOf(it)) }

    }
}
