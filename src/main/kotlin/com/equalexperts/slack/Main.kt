package com.equalexperts.slack

import com.equalexperts.slack.lambda.AwsLambda


fun main(args: Array<String>) {
    System.setProperty("org.slf4j.simpleLogger.logFile", "System.out")
    AwsLambda().runChannelChecker()
}
