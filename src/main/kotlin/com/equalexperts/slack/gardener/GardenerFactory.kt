package com.equalexperts.slack.gardener

import com.equalexperts.slack.gardener.rest.SlackApi
import com.equalexperts.slack.gardener.rest.SlackBotApi
import java.net.URI
import java.time.Clock
import java.time.Period

class GardenerFactory {


    fun build(slackUri: URI, slackOauthAccessToken: String, slackBotOauthAccessToken: String): Gardener {


        val slackApi = SlackApi.factory(slackUri, slackOauthAccessToken, Thread::sleep)
        val slackBotApi = SlackBotApi.factory(slackUri, slackBotOauthAccessToken, Thread::sleep)

        val clock = Clock.systemUTC()
        val defaultIdlePeriod = Period.ofMonths(3)
        val warningPeriod = Period.ofWeeks(1)
        val longIdlePeriod = Period.ofYears(1)
//cve-alerts causes issues with loading messages (as it posts 1000's of messages monthly), and causes the lambda to time out
        val channelWhitelist = setOf("announcements", "ask-aws", "meta-Slack", "ee-alumni", "feedback-to-ee", "remembering_torben", "ber-flynn", "cve-alerts")
        val longIdlePeriodChannels = setOf("coderetreat", "pt-global-coderetreat", "sk-ee-trip")

        val warningMessage = """Hi <!channel>.
                |This channel hasn't been used in a while, so I’d like to archive it.
                |This will keep the list of channels smaller and help users find things more easily.
                |If you _don't_ want this channel to be archived, just post a message and I'll leave it alone for a while.
                |You can archive the channel now using the `/archive` command.
                |If nobody posts in a few days I will come back and archive the channel for you.""".trimMargin().replace('\n', ' ')

        return Gardener(slackApi, slackBotApi, clock, defaultIdlePeriod, warningPeriod, channelWhitelist, longIdlePeriodChannels, longIdlePeriod, warningMessage)
    }
}