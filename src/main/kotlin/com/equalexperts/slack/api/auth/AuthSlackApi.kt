package com.equalexperts.slack.api.auth

import com.equalexperts.slack.api.auth.model.AuthInfo
import com.equalexperts.slack.api.rest.SlackRetrySupport
import com.equalexperts.slack.api.rest.feignBuilder
import feign.RequestLine
import java.net.URI

interface AuthSlackApi {

    @RequestLine("GET /api/auth.test")
    fun authenticate(): AuthInfo


    companion object {
        fun factory(uri: URI, token: String, sleeper: (Long) -> Unit): AuthSlackApi {
            return feignBuilder()
                    .requestInterceptor { it.query("token", token) }
                    .errorDecoder(SlackRetrySupport.SlackErrorDecoder())
                    .retryer(SlackRetrySupport(sleeper))
                    .target(AuthSlackApi::class.java, uri.toString())
        }
    }
}