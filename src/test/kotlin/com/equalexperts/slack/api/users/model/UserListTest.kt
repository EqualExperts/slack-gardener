package com.equalexperts.slack.api.users.model

import com.equalexperts.slack.profile.UserProfilesForTesting
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test

internal class UserListTest {
    @Test
    fun `should create list with empty cursor`() {
        val testUser = User(name = "TEST_BOT_USER",
                profile = UserProfilesForTesting.testBotProfile(),
                id = "id",
                team_id = "team_id",
                deleted = false,
                is_admin = false,
                is_owner = false,
                is_primary_owner = false,
                is_restricted = false,
                is_ultra_restricted = false,
                is_bot = true,
                is_app_user = false)

        val userList = UserListsForTesting.withEmptyCursorToken(testUser)


        assertTrue(userList.response_metadata.next_cursor.isBlank())
    }

    @Test
    fun `should create list with cursor`() {
        val testUser = User(name = "TEST_BOT_USER",
                profile = UserProfilesForTesting.testBotProfile(),
                id = "id",
                team_id = "team_id",
                deleted = false,
                is_admin = false,
                is_owner = false,
                is_primary_owner = false,
                is_restricted = false,
                is_ultra_restricted = false,
                is_bot = true,
                is_app_user = false)

        val cursor_token = "TOKEN"
        val userList = UserListsForTesting.withCursorToken(testUser, cursor_token)

        assertEquals(userList.response_metadata.next_cursor, cursor_token)
    }

}
