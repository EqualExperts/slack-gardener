package com.equalexperts.slack.gardener

import com.amazonaws.services.lambda.runtime.Context
import com.amazonaws.services.lambda.runtime.RequestHandler
import com.amazonaws.services.simplesystemsmanagement.AWSSimpleSystemsManagementClientBuilder
import com.amazonaws.services.simplesystemsmanagement.model.GetParametersRequest
import java.net.URI


class AwsLambda : RequestHandler<Any, Unit> {

    override fun handleRequest(input: Any?, context: Context?) {

        val client = AWSSimpleSystemsManagementClientBuilder.defaultClient()
        val request = GetParametersRequest()
        request.withNames("slack.gardener.oauth.access_token", "slack.gardener.bot.oauth.access_token").withDecryption = true
        val parameterResults = client.getParameters(request)
        val slackOauthAccessToken = parameterResults.parameters.find { it -> it.name == "slack.gardener.oauth.access_token" }?.value
        val slackBotOauthAccessToken = parameterResults.parameters.find { it -> it.name == "slack.gardener.bot.oauth.access_token" }?.value

        val slackUri = URI("https://api.slack.com")

        val gardener = GardenerFactory().build(slackUri, slackOauthAccessToken!!, slackBotOauthAccessToken!!)
        gardener.process()
    }
}
