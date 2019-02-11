package com.equalexperts.slack.api.users

import com.equalexperts.slack.api.profile.model.UserProfile
import com.equalexperts.slack.api.users.model.User

object UsersForTesting {
    fun testBot(profile: UserProfile) = User(name = "TEST_BOT_USER",
            profile = profile,
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

    fun testUser(profile: UserProfile) = User("TEST_USER",
            profile = profile,
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
}