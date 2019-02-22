package com.equalexperts.slack.profile

import com.equalexperts.slack.api.users.model.User
import com.equalexperts.slack.profile.rules.ProfileFieldRule
import com.equalexperts.slack.profile.rules.ProfileFieldRuleResult
import com.nhaarman.mockitokotlin2.atMost
import com.nhaarman.mockitokotlin2.mock
import com.nhaarman.mockitokotlin2.verify
import com.nhaarman.mockitokotlin2.whenever
import org.junit.jupiter.api.Test

internal class ProfileRequiredFieldsCheckerTest {

    @Test
    fun checkMissingFields() {
        val rule = mock<ProfileFieldRule>()
        val checker = ProfileRequiredFieldsChecker(listOf(rule))
        val user = mock<User>()

        val result = ProfileFieldRuleResult("TEST_FIELD", false)
        whenever(rule.checkProfile(user)).thenReturn(result)
        checker.checkMissingFields(user)
        verify(rule).checkProfile(user)
    }
}