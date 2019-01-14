package com.equalexperts.slack.gardener

import com.equalexperts.slack.rest.SlackApi
import com.natpryce.konfig.ConfigurationProperties.Companion.fromFile
import java.io.File

fun main(vararg args : String) {
    val config = fromFile(File("config.properties"))

    val gardener = GardenerFactory().build(config[Slack.uri], config[Slack.apiKey], config[Slack.Bot.apiKey])
    gardener.process()
}