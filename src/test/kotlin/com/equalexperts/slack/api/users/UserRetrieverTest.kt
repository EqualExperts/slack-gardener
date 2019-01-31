package com.equalexperts.slack.api.users

import com.equalexperts.slack.api.conversations.model.Conversation
import com.equalexperts.slack.api.conversations.model.ConversationList
import com.equalexperts.slack.api.rest.model.ResponseMetadata
import com.equalexperts.slack.api.users.model.User
import com.equalexperts.slack.api.users.model.UserList
import com.equalexperts.slack.api.users.model.UserProfile
import com.nhaarman.mockitokotlin2.*
import org.junit.jupiter.api.Test

import org.junit.jupiter.api.Assertions.*
import java.time.Instant

internal class UserRetrieverTest {

    @Test
    fun `should retrieve channels`() {
        val mockUsersSlackApi : UsersSlackApi = mock()

        val testUser = User(name="TEST_BOT_USER",
                profile= UserProfile("TEST_BOT_ID"),
                id="id",
                team_id="team_id",
                deleted=false,
                is_admin=false,
                is_owner=false,
                is_primary_owner=false,
                is_restricted=false,
                is_ultra_restricted=false,
                is_bot=true,
                is_app_user=false)

        val userList = UserList(listOf(testUser), ResponseMetadata(""))
        whenever(mockUsersSlackApi.list()).thenReturn(userList)

        val channels = UsersSlackApi.listAll(mockUsersSlackApi)

        verify(mockUsersSlackApi, atMost(1)).list()

        assertEquals(setOf(testUser), channels)
    }

    @Test
    fun `should use cursor token to get next page of channels if non-blank`() {
        val mockUsersSlackApi : UsersSlackApi = mock()

        val testUser = User(name="TEST_BOT_USER",
                profile=UserProfile("TEST_BOT_ID"),
                id="id",
                team_id="team_id",
                deleted=false,
                is_admin=false,
                is_owner=false,
                is_primary_owner=false,
                is_restricted=false,
                is_ultra_restricted=false,
                is_bot=true,
                is_app_user=false)


        val cursorToken = "CURSOR TOKEN"
        val firstResponse = UserList(listOf(testUser), ResponseMetadata(cursorToken))

        whenever(mockUsersSlackApi.list()).thenReturn(firstResponse)

        val testUserTwo = User("TEST_USER",
                UserProfile("TEST_USER_ID"),
                id="id",
                team_id="team_id",
                deleted=false,
                is_admin=false,
                is_owner=false,
                is_primary_owner=false,
                is_restricted=false,
                is_ultra_restricted=false,
                is_bot=false,
                is_app_user=false)

                val emptyCursorToken = ""
        val secondResponse = UserList(listOf(testUserTwo), ResponseMetadata(emptyCursorToken))

        whenever(mockUsersSlackApi.list(cursorToken)).thenReturn(secondResponse)

        val channels = UsersSlackApi.listAll(mockUsersSlackApi)

        verify(mockUsersSlackApi, atMost(1)).list()
        verify(mockUsersSlackApi, atMost(1)).list(cursorToken)

        assertEquals(setOf(testUser, testUserTwo), channels)
    }
}