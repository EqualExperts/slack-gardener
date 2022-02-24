package com.equalexperts.slack.profile.rules

import com.equalexperts.slack.api.users.SlackTestUsers
import com.equalexperts.slack.profile.SlackTestProfiles
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test

internal class ProfileFieldRealNameRuleTest {

    @Test
    fun `should return true for real name field presence`() {
        val userProfile = SlackTestProfiles.botProfile()
        val testUser = SlackTestUsers.testBot(userProfile)

        val rule = ProfileFieldRealNameRule()

        val result = rule.checkProfile(testUser)
        assertTrue(result.result)
    }

    @Test
    fun `should return false for real name field absence`() {
        val userProfile = SlackTestProfiles.botProfile().copy(real_name = null)
        val testUser = SlackTestUsers.testBot(userProfile)

        val rule = ProfileFieldRealNameRule()

        val result = rule.checkProfile(testUser)
        assertFalse(result.result)
    }
}
