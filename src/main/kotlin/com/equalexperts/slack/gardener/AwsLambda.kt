package com.equalexperts.slack.gardener

import com.amazonaws.services.lambda.runtime.Context
import com.amazonaws.services.lambda.runtime.RequestHandler
import com.natpryce.konfig.*

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