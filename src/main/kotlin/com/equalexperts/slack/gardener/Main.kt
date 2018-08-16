package com.equalexperts.slack.gardener

import com.equalexperts.slack.gardener.rest.SlackApi
import com.equalexperts.slack.gardener.rest.SlackBotApi
import com.natpryce.konfig.ConfigurationProperties.Companion.fromFile
import java.io.File
import java.time.Clock
import java.time.Period

fun main(vararg args : String) {
    val config = fromFile(File("config.properties"))
    val slackUri = config[Slack.uri]

    val apiKey = config[Slack.apiKey]
    val slackApi = SlackApi.factory(slackUri, apiKey, Thread::sleep)

    val gardener = GardenerFactory().build(config[Slack.uri], config[Slack.apiKey], config[Slack.Bot.apiKey])
    gardener.process()
}