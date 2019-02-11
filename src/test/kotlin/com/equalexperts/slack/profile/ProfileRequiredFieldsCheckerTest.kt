package com.equalexperts.slack.profile

import com.equalexperts.slack.api.profile.model.TeamProfile
import com.equalexperts.slack.api.profile.model.TeamProfileDetails
import com.equalexperts.slack.api.profile.model.TeamProfileFieldMetadata
import com.equalexperts.slack.api.users.UsersForTesting
import com.equalexperts.slack.api.users.model.User
import com.equalexperts.slack.api.profile.model.UserProfileField
import com.equalexperts.slack.profile.rules.*
import com.fasterxml.jackson.databind.DeserializationFeature
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.module.kotlin.readValue
import com.fasterxml.jackson.module.kotlin.registerKotlinModule
import com.nhaarman.mockitokotlin2.atMost
import com.nhaarman.mockitokotlin2.mock
import com.nhaarman.mockitokotlin2.verify
import com.nhaarman.mockitokotlin2.whenever
import org.junit.jupiter.api.Assertions.assertEquals
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
        verify(rule, atMost(1)).checkProfile(user)
    }
}