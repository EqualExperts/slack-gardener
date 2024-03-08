package com.equalexperts.slack.api.users

import com.equalexperts.slack.api.profile.model.UserProfile
import com.equalexperts.slack.api.users.model.User
import com.equalexperts.slack.profile.SlackTestProfiles

object SlackTestUsers {
    fun testBot(profile: UserProfile) = User(name = "TEST_BOT_USER",
            profile = profile,
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

    fun testUser(profile: UserProfile) = User("TEST_USER",
            profile = profile,
            id = "id",
            team_id = "team_id",
            deleted = false,
            is_admin = false,
            is_owner = false,
            is_primary_owner = false,
            is_restricted = false,
            is_ultra_restricted = false,
            is_bot = false,
            is_app_user = false)

    fun gardenerUser() = testBot(profile = SlackTestProfiles.botProfile().copy(bot_id = "GARDENER_BOT_ID")).copy(name = "GARDENER", id = "GARDENER_BOT_ID")

    fun nonGardenerBotUser() = testBot(profile = SlackTestProfiles.botProfile().copy(bot_id = "NON_GARDENER_BOT_ID")).copy(id = "NON_GARDENER_BOT_ID")

    fun humanUser() = testUser(profile = SlackTestProfiles.userProfile())
}
