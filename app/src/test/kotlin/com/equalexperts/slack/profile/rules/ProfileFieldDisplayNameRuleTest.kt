package com.equalexperts.slack.profile.rules

import com.equalexperts.slack.api.users.SlackTestUsers
import com.equalexperts.slack.profile.SlackTestProfiles
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test

internal class ProfileFieldDisplayNameRuleTest {

    @Test
    fun `should return true for display name field presence`() {
        val userProfile = SlackTestProfiles.botProfile()
        val testUser = SlackTestUsers.testBot(userProfile)

        val rule = ProfileFieldDisplayNameRule()

        val result = rule.checkProfile(testUser)
        assertTrue(result.result)
    }

    @Test
    fun `should return false for display name field absence`() {
        val userProfile = SlackTestProfiles.botProfile().copy(display_name = null)
        val testUser = SlackTestUsers.testBot(userProfile)

        val rule = ProfileFieldDisplayNameRule()

        val result = rule.checkProfile(testUser)
        assertFalse(result.result)
    }

}
