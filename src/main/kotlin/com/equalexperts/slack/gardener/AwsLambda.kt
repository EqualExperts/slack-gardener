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
        main()
    }

    fun main() {
        val version = AwsLambda::class.java.getPackage().implementationVersion
        logger.info("Running version: $version")

        val client = AWSSimpleSystemsManagementClientBuilder.defaultClient()
        val request = GetParametersRequest()
        request.withNames("slack.gardener.oauth.access_token", "slack.gardener.bot.oauth.access_token").withDecryption = true
        val parameterResults = client.getParameters(request)
        val slackOauthAccessToken = parameterResults.parameters.find { it -> it.name == "slack.gardener.oauth.access_token" }?.value!!
        val slackBotOauthAccessToken = parameterResults.parameters.find { it -> it.name == "slack.gardener.bot.oauth.access_token" }?.value!!

        val slackUri = URI("https://api.slack.com")

        val idleMonths = 3
        val warningWeeks = 1
        val longIdleYears = 1
        val channelWhitelist = setOf("announcements",
                "meta-slack",
                "ee-alumni",
                "feedback-to-ee",
                "remembering_torben",
                "ber-flynn")
        val longIdlePeriodChannels = setOf("sk-ee-trip")
        val warningMessage = """Hi <!channel>.
                            |This channel hasn't been used in a while, so I’d like to archive it.
                            |This will keep the list of channels smaller and help users find things more easily.
                            |If you _don't_ want this channel to be archived, just post a message and I'll leave it alone for a while.
                            |You can archive the channel now using the `/archive` command.
                            |If nobody posts in a few days I will come back and archive the channel for you.""".trimMargin().replace('\n', ' ')

        val gardener = Gardener.build(slackUri,
                slackOauthAccessToken,
                slackBotOauthAccessToken,
                idleMonths,
                warningWeeks,
                longIdleYears,
                channelWhitelist,
                longIdlePeriodChannels,
                warningMessage)

        gardener.process()
    }
}
