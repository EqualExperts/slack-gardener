package com.equalexperts.slack.api.users

import com.equalexperts.slack.api.users.model.User
import com.equalexperts.slack.api.users.model.UserListsForTesting
import com.equalexperts.slack.profile.UserProfilesForTesting
import com.nhaarman.mockitokotlin2.atMost
import com.nhaarman.mockitokotlin2.mock
import com.nhaarman.mockitokotlin2.verify
import com.nhaarman.mockitokotlin2.whenever
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test


internal class UserRetrieverTest {

    @Test
    fun `should retrieve channels`() {
        val mockUsersSlackApi: UsersSlackApi = mock()

        val testUser = User(name = "TEST_BOT_USER",
                profile = UserProfilesForTesting.testBotProfile(),
                id = "id",
                team_id = "team_id",
                is_deleted = false,
                is_admin = false,
                is_owner = false,
                is_primary_owner = false,
                is_restricted = false,
                is_ultra_restricted = false,
                is_bot = true,
                is_app_user = false)

        val userList = UserListsForTesting.withEmptyCursorToken(testUser)
        whenever(mockUsersSlackApi.list()).thenReturn(userList)

        val channels = UsersSlackApi.listAll(mockUsersSlackApi)

        verify(mockUsersSlackApi, atMost(1)).list()

        assertEquals(setOf(testUser), channels)
    }

    @Test
    fun `should use cursor token to get next page of channels if non-blank`() {
        val mockUsersSlackApi: UsersSlackApi = mock()

        val testUser = User(name = "TEST_BOT_USER",
                profile = UserProfilesForTesting.testBotProfile(),
                id = "id",
                team_id = "team_id",
                is_deleted = false,
                is_admin = false,
                is_owner = false,
                is_primary_owner = false,
                is_restricted = false,
                is_ultra_restricted = false,
                is_bot = true,
                is_app_user = false)


        val cursorToken = "CURSOR TOKEN"
        val firstResponse = UserListsForTesting.withCursorToken(testUser, cursorToken)

        whenever(mockUsersSlackApi.list()).thenReturn(firstResponse)

        val testUserTwo = User("TEST_USER",
                profile = UserProfilesForTesting.testBotProfile(),
                id = "id",
                team_id = "team_id",
                is_deleted = false,
                is_admin = false,
                is_owner = false,
                is_primary_owner = false,
                is_restricted = false,
                is_ultra_restricted = false,
                is_bot = false,
                is_app_user = false)

        val secondResponse = UserListsForTesting.withEmptyCursorToken(testUserTwo)

        whenever(mockUsersSlackApi.list(cursorToken)).thenReturn(secondResponse)

        val channels = UsersSlackApi.listAll(mockUsersSlackApi)

        verify(mockUsersSlackApi, atMost(1)).list()
        verify(mockUsersSlackApi, atMost(1)).list(cursorToken)

        assertEquals(setOf(testUser, testUserTwo), channels)
    }
}