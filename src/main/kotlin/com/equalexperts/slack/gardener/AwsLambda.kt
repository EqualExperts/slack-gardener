package com.equalexperts.slack.gardener

import com.amazonaws.services.lambda.runtime.Context
import com.amazonaws.services.lambda.runtime.RequestHandler
import com.equalexperts.slack.gardener.rest.SlackApi
import com.equalexperts.slack.gardener.rest.SlackBotApi
import com.natpryce.konfig.*
import java.time.Clock
import java.time.Period

class AwsLambda : RequestHandler<Any, Unit> {
    override fun handleRequest(input: Any?, context: Context?) {
        val config = EnvironmentVariables

        val gardener = GardenerFactory().build(config[Slack.uri], config[Slack.apiKey], config[Slack.Bot.apiKey])
        gardener.process()
    }


}

object Slack : PropertyGroup() {
    val uri by uriType
    val apiKey by stringType
    object Bot : PropertyGroup() {
        val apiKey by stringType
    }
}