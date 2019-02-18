package com.equalexperts.slack.gardener

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

        val gardenerOauthAccessTokenParamName = "slack.gardener.oauth.access_token"
        val gardenerBotOauthAccessTokenParamName = "slack.gardener.bot.oauth.access_token"

        val gardenerUriParamName = "slack.gardener.uri"

        val idleMonthsParamName = "slack.gardener.idle.months"
        val longIdleYearsParamName = "slack.gardener.idle.long.years"
        val longIdleChannelsParamName = "slack.gardener.idle.long.channels"

        val warningWaitWeeksParamName = "slack.gardener.warning.wait.weeks"
        val warningMessageParamName = "slack.gardener.warning.wait.message"

        request.withNames(gardenerOauthAccessTokenParamName,
                gardenerBotOauthAccessTokenParamName,
                gardenerUriParamName,
                idleMonthsParamName,
                warningWaitWeeksParamName,
                longIdleYearsParamName,
                longIdleChannelsParamName,
                warningMessageParamName).withDecryption = true
        val parameterResults = client.getParameters(request)

        val slackOauthAccessToken = parameterResults.parameters.find { it.name == gardenerOauthAccessTokenParamName }?.value!!
        val slackBotOauthAccessToken = parameterResults.parameters.find { it.name == gardenerBotOauthAccessTokenParamName }?.value!!

        val slackUriRaw = parameterResults.parameters.find { it.name == gardenerUriParamName }?.value!!
        val slackUri = URI(slackUriRaw)

        val idleMonths = parameterResults.parameters.find { it.name == idleMonthsParamName }?.value!!.toInt()
        val longIdleYears = parameterResults.parameters.find { it.name == longIdleYearsParamName }?.value!!.toInt()
        val longIdleChannels = parameterResults.parameters.find { it.name == longIdleChannelsParamName }?.value!!.split(",")

        val warningWaitWeeks = parameterResults.parameters.find { it.name == warningWaitWeeksParamName }?.value!!.toInt()
        val warningMessage = parameterResults.parameters.find { it.name == warningMessageParamName }?.value!!


        val gardener = Gardener.build(slackUri,
                slackOauthAccessToken,
                slackBotOauthAccessToken,
                idleMonths,
                warningWaitWeeks,
                longIdleYears,
                longIdleChannels,
                warningMessage)

        gardener.process()
    }
}
