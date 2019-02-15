package com.equalexperts.slack.profile.rules

import com.equalexperts.slack.api.users.UsersForTesting
import com.equalexperts.slack.profile.UserProfilesForTesting
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test

internal class ProfileFieldDisplayNameRuleTest {

    @Test
    fun `should return true for display name field presence`() {
        val userProfile = UserProfilesForTesting.testBotProfile()
        val testUser = UsersForTesting.testBot(userProfile)

        val rule = ProfileFieldDisplayNameRule()

        val result = rule.checkProfile(testUser)
        assertTrue(result.result)
    }

    @Test
    fun `should return false for display name field absence`() {
        val userProfile = UserProfilesForTesting.testBotProfile().copy(display_name = null)
        val testUser = UsersForTesting.testBot(userProfile)

        val rule = ProfileFieldDisplayNameRule()

        val result = rule.checkProfile(testUser)
        assertFalse(result.result)
    }

}