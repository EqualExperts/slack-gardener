package com.equalexperts.slack.profile

import com.amazonaws.services.lambda.runtime.Context
import com.amazonaws.services.lambda.runtime.RequestHandler
import com.amazonaws.services.simplesystemsmanagement.AWSSimpleSystemsManagementClientBuilder
import com.amazonaws.services.simplesystemsmanagement.model.GetParametersRequest
import org.slf4j.LoggerFactory
import java.net.URI

class AwsLambda : RequestHandler<Any, Unit> {

    private val logger = LoggerFactory.getLogger(this::class.java.name)

    override fun handleRequest(input: Any?, context: Context?) {
        process()
    }

    fun process() {
        val client = AWSSimpleSystemsManagementClientBuilder.defaultClient()
        val request = GetParametersRequest()
        request.withNames("slack.profile.oauth.access_token", "slack.profile.bot.oauth.access_token").withDecryption = true
        val parameterResults = client.getParameters(request)
        val slackOauthAccessToken = parameterResults.parameters.find { it.name == "slack.profile.oauth.access_token" }?.value!!
        val slackBotOauthAccessToken = parameterResults.parameters.find { it.name == "slack.profile.bot.oauth.access_token" }?.value!!

        val slackUri = URI("https://api.slack.com")

        val warningMessage = """Hi <@%s>
                            |It looks like you haven't set some basic profile fields that the EE Slack Usage Guide recommends
                            |Please complete your slack profile, it helps people recognise you both in and outside slack.
                            |Your Slack profile should have at least the following fields set:
                            |Full Name - Your name, not your userid (e.g. "Slack Gardener" not "slackgardener")
                            |Display Name - What you’d like people to refer to you in slack
                            |Profile Photo - A picture that accurately represents you, so people can identify you
                            |What I Do
                            |EE Client (if applicable)
                            |Home Base - So people know where you generally are located
                            |""".trimMargin().replace('\n', ' ')

        val profileChecker = ProfileChecker.build(slackUri,
                slackOauthAccessToken,
                slackBotOauthAccessToken,
                warningMessage)


        profileChecker.process()
    }
}
