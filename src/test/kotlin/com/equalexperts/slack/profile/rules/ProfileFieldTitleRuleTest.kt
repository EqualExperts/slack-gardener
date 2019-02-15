package com.equalexperts.slack.profile.rules

import com.equalexperts.slack.api.users.UsersForTesting
import com.equalexperts.slack.profile.UserProfilesForTesting
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test

internal class ProfileFieldTitleRuleTest {

    @Test
    fun `should return true for title field presence`() {
        val userProfile = UserProfilesForTesting.testBotProfile()
        val testUser = UsersForTesting.testBot(userProfile)

        val rule = ProfileFieldTitleRule()

        val result = rule.checkProfile(testUser)
        assertTrue(result.result)
    }

    @Test
    fun `should return false for title field absence`() {
        val userProfile = UserProfilesForTesting.testBotProfile().copy(title = null)
        val testUser = UsersForTesting.testBot(userProfile)

        val rule = ProfileFieldTitleRule()

        val result = rule.checkProfile(testUser)
        assertFalse(result.result)
    }
}