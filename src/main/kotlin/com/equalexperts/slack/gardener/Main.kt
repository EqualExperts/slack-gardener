package com.equalexperts.slack.gardener

import com.equalexperts.slack.gardener.rest.SlackApi
import com.equalexperts.slack.gardener.rest.SlackBotApi
import com.natpryce.konfig.ConfigurationProperties.Companion.fromFile
import java.io.File
import java.time.Clock
import java.time.Period

fun main(vararg args : String) {
    val config = fromFile(File("config.properties"))
    val slackUri = config[slack.uri]

    val slackApi = SlackApi.factory(slackUri, config[slack.apiKey], Thread::sleep)
    val slackBotApi = SlackBotApi.factory(slackUri, config[slack.bot.apiKey], Thread::sleep)

    val clock = Clock.systemUTC()
    val defaultIdlePeriod = Period.ofMonths(3)
    val warningPeriod = Period.ofWeeks(1)

    Gardener(slackApi, slackBotApi, clock, defaultIdlePeriod, warningPeriod).process()
}