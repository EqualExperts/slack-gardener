package com.equalexperts.slack.gardener


fun main(args: Array<String>) {
    System.setProperty("org.slf4j.simpleLogger.logFile", "System.out")
    AwsLambda().process()
}
