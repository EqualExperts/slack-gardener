package com.equalexperts.slack.aws

import com.amazonaws.services.simplesystemsmanagement.AWSSimpleSystemsManagement
import com.amazonaws.services.simplesystemsmanagement.model.GetParametersRequest
import com.equalexperts.slack.channel.ChannelChecker
import com.equalexperts.slack.profile.ProfileChecker
import org.slf4j.LoggerFactory
import java.net.URI
import java.util.*


class AWSExecutionEnvironment {

    private val logger = LoggerFactory.getLogger(this::class.java.name)

    fun run(client: AWSSimpleSystemsManagement) {
        val request = GetParametersRequest()

        val gardenerChannelActivityCheckingParamName = "slack.gardener.channel.checking"
        val gardenerProfileFieldCheckingParamName = "slack.gardener.profile.checking"

        request.withNames(gardenerChannelActivityCheckingParamName,
            gardenerProfileFieldCheckingParamName).isWithDecryption = true
        val parameterResults = client.getParameters(request)

        logger.info("Gardener Functionality Parameters: ${parameterResults.parameters.map { Pair(it.name, it.value) }}")

        if (parameterResults.invalidParameters.isNotEmpty()) {
            logger.warn("Gardener Functionality Parameters that are invalid/missing: ${parameterResults.invalidParameters}")
        }

        val gardenerChannelActivityChecking = parameterResults.parameters.find { it.name == gardenerChannelActivityCheckingParamName }?.let { it.value }
            ?: "false"

        val gardenerProfileFieldChecking = parameterResults.parameters.find { it.name == gardenerProfileFieldCheckingParamName }?.let { it.value }
            ?: "false"

        if (gardenerChannelActivityChecking.lowercase(Locale.getDefault()) == "true") {
            runChannelChecker(client)
        }

        if (gardenerProfileFieldChecking.lowercase(Locale.getDefault()) == "true") {
            runProfileChecker(client)
        }

    }

    fun runChannelChecker(client: AWSSimpleSystemsManagement) {
        val request = GetParametersRequest()

        val gardenerOauthAccessTokenParamName = "slack.gardener.oauth.access_token"
        val gardenerBotOauthAccessTokenParamName = "slack.gardener.bot.oauth.access_token"

        val gardenerUriParamName = "slack.gardener.uri"

        val idleMonthsParamName = "slack.gardener.idle.months"
        val longIdleYearsParamName = "slack.gardener.idle.long.years"
        val longIdleChannelsParamName = "slack.gardener.idle.long.channels"

        val warningWaitWeeksParamName = "slack.gardener.warning.wait.weeks"
        val warningMessageParamName = "slack.gardener.warning.wait.message"

        val dryRunParamName = "slack.channel.dryrun"

        request.withNames(gardenerOauthAccessTokenParamName,
            gardenerBotOauthAccessTokenParamName,
            gardenerUriParamName,
            idleMonthsParamName,
            warningWaitWeeksParamName,
            longIdleYearsParamName,
            longIdleChannelsParamName,
            warningMessageParamName,
            dryRunParamName).withDecryption = true
        val parameterResults = client.getParameters(request)

        if (parameterResults.invalidParameters.isNotEmpty()) {
            logger.warn("Gardener Channel Checker Parameters that are invalid/missing: ${parameterResults.invalidParameters}")
        }

        val slackOauthAccessToken = parameterResults.parameters.find { it.name == gardenerOauthAccessTokenParamName }?.value!!
        val slackBotOauthAccessToken = parameterResults.parameters.find { it.name == gardenerBotOauthAccessTokenParamName }?.value!!

        val slackUriRaw = parameterResults.parameters.find { it.name == gardenerUriParamName }?.value!!
        val slackUri = URI(slackUriRaw)

        val idleMonths = parameterResults.parameters.find { it.name == idleMonthsParamName }?.value!!.toInt()
        val longIdleYears = parameterResults.parameters.find { it.name == longIdleYearsParamName }?.value!!.toInt()
        val longIdleChannels = parameterResults.parameters.find { it.name == longIdleChannelsParamName }?.value!!.split(",")

        val warningWaitWeeks = parameterResults.parameters.find { it.name == warningWaitWeeksParamName }?.value!!.toInt()
        val warningMessage = parameterResults.parameters.find { it.name == warningMessageParamName }?.value!!

        val dryRun = parameterResults.parameters.find { it.name == dryRunParamName }?.value!!.toBoolean()


        val gardener = ChannelChecker.build(dryRun,
            slackUri,
            slackOauthAccessToken,
            slackBotOauthAccessToken,
            idleMonths,
            warningWaitWeeks,
            longIdleYears,
            longIdleChannels,
            warningMessage)

        gardener.process()
    }

    fun runProfileChecker(client: AWSSimpleSystemsManagement) {
        val request = GetParametersRequest()

        val gardenerOauthAccessTokenParamName = "slack.gardener.oauth.access_token"
        val gardenerBotOauthAccessTokenParamName = "slack.gardener.bot.oauth.access_token"
        val profileUriParamName = "slack.gardener.uri"
        val warningWaitDaysParamName = "slack.profile.warning.wait.days"
        val warningMessageParamName = "slack.profile.warning.wait.message"
        val dryRunParamName = "slack.profile.dryrun"

        request.withNames(
            gardenerOauthAccessTokenParamName,
            gardenerBotOauthAccessTokenParamName,
            profileUriParamName,
            warningWaitDaysParamName,
            warningMessageParamName,
            dryRunParamName).withDecryption = true
        val parameterResults = client.getParameters(request)

        if (parameterResults.invalidParameters.isNotEmpty()) {
            logger.warn("Gardener Profile Checker Parameters that are invalid/missing: ${parameterResults.invalidParameters}")
        }

        val slackOauthAccessToken = parameterResults.parameters.find { it.name == gardenerOauthAccessTokenParamName }?.value!!
        val slackBotOauthAccessToken = parameterResults.parameters.find { it.name == gardenerBotOauthAccessTokenParamName }?.value!!

        val slackUriRaw = parameterResults.parameters.find { it.name == profileUriParamName }?.value!!
        val slackUri = URI(slackUriRaw)

        val warningWaitDays = parameterResults.parameters.find { it.name == warningWaitDaysParamName }?.value!!.toInt()
        val warningMessage = parameterResults.parameters.find { it.name == warningMessageParamName }?.value!!
        val dryRun = parameterResults.parameters.find { it.name == dryRunParamName }?.value!!.toBoolean()


        //Calculated using ProfileChecker.getDefaultMd5Hashes()
        val knownDefaultPictureMd5Hashes = setOf(
            "26cc91d812f0da60c876e85d57297da0",
            "fe38d005d3a27996b5de82e32d722eb1",
            "9e047fa43bbb0f5b38130859de6e86ab",
            "9a9003f8ebc0533afca6649c6f5a577c",
            "6464c1bb3b2a13e1ff33a90bec01bba0",
            "a98ee59013dab5a54931d21f328d4be9",
            "792dd7a0a57e525248aa1bf19f0c8fe3",
            "a4d2ad061f6d9d6b095b7e32af8bb9c0",
            "8a4f0a054770a968f4005fbd4a62261c",
            "b9d047ec1808b212d01f7a46999f440c",
            "3ebd18250f38e2078bf54133c408b94f",
            "e07b34a65d70a0cbd87685a4305cc16b",
            "496fffd0d34c7c9e3ed8945db90598ed",
            "d015d23e1ff68a89059c415bf3153794",
            "37a2cffb8704125edbd8a74fbb4004aa",
            "975d1a4e88db34fa91aece790f480b45",
            "fa45664e817efd1fd2c30f904887680e",
            "b8eb58868fc9712f6ad87f48d4f791fd",
            "faa43522398fc58472c21210c0a4fe90",
            "64f3d05362c4afeb8cc6e25a6082703e",
            "b1ed1856b88a98273a1b234643051cd2",
            "e87ce31334dcf92eb39c4fd0bed05e65",
            "a537cfd65e3f4b655637e78753bce530",
            "93ac368757cb78b07703bf2ec75a006a",
            "ab9d10bf2f9773efeb4a10d632a2a0bd")

        val profileChecker = ProfileChecker.build(dryRun, slackUri,
            slackOauthAccessToken,
            slackBotOauthAccessToken,
            warningMessage,
            warningWaitDays,
            knownDefaultPictureMd5Hashes)

        profileChecker.process()
    }

}
