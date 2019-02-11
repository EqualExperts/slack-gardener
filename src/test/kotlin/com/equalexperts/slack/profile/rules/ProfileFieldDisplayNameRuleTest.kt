package com.equalexperts.slack.profile.rules

import com.equalexperts.slack.api.users.UsersForTesting
import com.equalexperts.slack.profile.UserProfilesForTesting
import org.junit.jupiter.api.Test

import org.junit.jupiter.api.Assertions.*

internal class ProfileFieldDisplayNameRuleTest {

    @Test
    fun `should return true for display name field presence`() {
        val userProfile = UserProfilesForTesting.testBot()
        val testUser = UsersForTesting.testBot(userProfile)

        val rule = ProfileFieldDisplayNameRule()

        val result = rule.checkProfile(testUser)
        assertTrue(result.result)
    }

    @Test
    fun `should return false for display name field absence`() {
        val userProfile = UserProfilesForTesting.testBot().copy(display_name = null)
        val testUser = UsersForTesting.testBot(userProfile)

        val rule = ProfileFieldDisplayNameRule()

        val result = rule.checkProfile(testUser)
        assertFalse(result.result)
    }

}