package com.equalexperts.slack

import com.amazonaws.services.simplesystemsmanagement.AWSSimpleSystemsManagementClientBuilder
import com.equalexperts.slack.aws.AWSExecutionEnvironment

fun main() {
    val lambda = AWSExecutionEnvironment()
    val client = AWSSimpleSystemsManagementClientBuilder.defaultClient()
    lambda.run(client)
}
