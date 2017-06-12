package com.equalexperts.slack.gardener

import com.equalexperts.slack.gardener.rest.SlackApi
import com.equalexperts.slack.gardener.rest.SlackBotApi
import com.natpryce.konfig.ConfigurationProperties.Companion.fromFile
import com.natpryce.konfig.PropertyGroup
import com.natpryce.konfig.getValue
import com.natpryce.konfig.stringType
import com.natpryce.konfig.uriType
import java.io.File
import java.time.Clock
import java.time.Period
import kotlin.system.measureNanoTime

fun main(vararg args : String) {
    val nanoTime = measureNanoTime {

        val config = fromFile(File("config.properties"))
        val slackUri = config[slack.uri]

        val slackApi = SlackApi.factory(slackUri, config[slack.apiKey])
        val slackBotApi = SlackBotApi.factory(slackUri, config[slack.bot.apiKey])

        val clock = Clock.systemUTC()
        val idlePeriod = Period.ofMonths(3)
        val warningPeriod = Period.ofWeeks(1)

        Gardener(slackApi, slackBotApi, clock, idlePeriod, warningPeriod).process()
    }

    println("done in ${nanoTime / 1_000_000} ms")
}

//private object slack : PropertyGroup() {
//    val uri by uriType
//    val apiKey by stringType
//    object bot : PropertyGroup() {
//        val apiKey by stringType
//    }
//}