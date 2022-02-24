package com.equalexperts.slack.profile.rules

import com.equalexperts.slack.api.users.SlackTestUsers
import com.equalexperts.slack.profile.SlackTestProfiles
import com.github.kittinunf.fuel.core.Client
import com.github.kittinunf.fuel.core.FuelManager
import com.github.kittinunf.fuel.core.Response
import com.nhaarman.mockitokotlin2.any
import com.nhaarman.mockitokotlin2.doReturn
import com.nhaarman.mockitokotlin2.mock
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Test
import java.net.URL


internal class ProfilePictureRuleTest {

    @Test
    fun `should return true for user-uploaded picture`() {
        val testUrl = "https://TEST_URL"
        val userProfile = SlackTestProfiles.botProfile().copy(image_24 = testUrl)
        val testUser = SlackTestUsers.testBot(userProfile)
        val rule = ProfilePictureRule(setOf("TEST_DEFAULT_HASH"))

        val inputStream = getResource("/empty.jpg")
        val client = mock<Client> {
            onGeneric { executeRequest(any()) } doReturn Response(
                    statusCode = 200,
                    responseMessage = "ok",
                    dataStream = inputStream,
                    url = URL(testUrl)
            )
        }
        FuelManager.instance.client = client

        val result = rule.checkProfile(testUser)
        assertTrue(result.result)
    }

    @Test
    fun `should return false for redirected default picture`() {
        val testUrl = "https://secure.gravatar.com/TEST_URL"
        val redirectedUrl = "https://DEFAULT_URL"
        val userProfile = SlackTestProfiles.botProfile().copy(image_24 = testUrl)
        val testUser = SlackTestUsers.testBot(userProfile)
        val rule = ProfilePictureRule(setOf("TEST_DEFAULT_HASH"))

        val inputStream = getResource("/empty.jpg")
        val client = mock<Client> {
            onGeneric { executeRequest(any()) } doReturn Response(
                statusCode = 200,
                responseMessage = "ok",
                dataStream = inputStream,
                url = URL(redirectedUrl)
            )
        }
        FuelManager.instance.client = client

        val result = rule.checkProfile(testUser)
        assertFalse(result.result)
    }

    @Test
    fun `should return false for non user-uploaded picture`() {
        val testUrl = "https://secure.gravatar.com/TEST_URL"
        val userProfile = SlackTestProfiles.botProfile().copy(image_24 = testUrl)
        val testUser = SlackTestUsers.testBot(userProfile)
        val rule = ProfilePictureRule(setOf("0e9c7b8f33e92621323f0a2f4892ff7c"))

        val inputStream = getResource("/empty.jpg")
        val client = mock<Client> {
            onGeneric { executeRequest(any()) } doReturn Response(
                    statusCode = 200,
                    responseMessage = "ok",
                    dataStream = inputStream,
                    url = URL(testUrl)
            )
        }
        FuelManager.instance.client = client

        val result = rule.checkProfile(testUser)
        assertFalse(result.result)
    }


    @Test
    fun `should return false for missing picture`() {

        val userProfile = SlackTestProfiles.botProfile().copy(image_24 = null)
        val testUser = SlackTestUsers.testBot(userProfile)
        val rule = ProfilePictureRule(setOf("TEST_HASH"))

        val result = rule.checkProfile(testUser)
        assertFalse(result.result)
    }


    @Test
    fun `should return false for error picture`() {
        val testUrl = "https://secure.gravatar.com/TEST_URL"
        val userProfile = SlackTestProfiles.botProfile().copy(image_24 = testUrl)
        val testUser = SlackTestUsers.testBot(userProfile)
        val rule = ProfilePictureRule(setOf("TEST_DEFAULT_HASH"))

        val inputStream = getResource("/empty.jpg")
        val client = mock<Client> {
            onGeneric { executeRequest(any()) } doReturn Response(
                    statusCode = 404,
                    responseMessage = "not found",
                    dataStream = inputStream,
                    url = URL(testUrl)
            )
        }
        FuelManager.instance.client = client

        val thrown = assertThrows(Exception::class.java,
                { rule.checkProfile(testUser) },
                "Expected rule.checkProfile(testUser) to throw, but it didn't")

        val expectedMessageThrown = thrown.message!!.contains("Unable to retrieve Profile Picture for user TEST_BOT_USER - https://secure.gravatar.com/TEST_URL {com.github.kittinunf.fuel.core.HttpException: HTTP Exception 404 not found}")
        assertTrue(expectedMessageThrown)
    }


    private fun getResource(resource: String) = javaClass.getResourceAsStream(resource)

}
