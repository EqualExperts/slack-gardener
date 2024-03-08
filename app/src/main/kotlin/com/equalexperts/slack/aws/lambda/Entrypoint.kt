package com.equalexperts.slack.aws.lambda

import com.amazonaws.services.lambda.runtime.Context
import com.amazonaws.services.lambda.runtime.RequestHandler
import com.amazonaws.services.simplesystemsmanagement.AWSSimpleSystemsManagementClientBuilder
import com.equalexperts.slack.aws.AWSExecutionEnvironment
import org.slf4j.LoggerFactory

class Entrypoint : RequestHandler<Any, Unit> {

    private val logger = LoggerFactory.getLogger(this::class.java.name)

    override fun handleRequest(input: Any?, context: Context?) {
        val client = AWSSimpleSystemsManagementClientBuilder.defaultClient()
        AWSExecutionEnvironment().run(client)
    }

}
