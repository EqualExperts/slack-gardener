package com.equalexperts.slack

import com.equalexperts.slack.lambda.AwsLambda


fun main() {
    System.setProperty("org.slf4j.simpleLogger.logFile", "System.out")
    AwsLambda().runChannelChecker()
}
