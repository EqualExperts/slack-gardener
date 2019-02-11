package com.equalexperts.slack.api.rest

import com.fasterxml.jackson.databind.DeserializationFeature
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.module.kotlin.registerKotlinModule
import feign.Feign
import feign.Logger
import feign.jackson.JacksonEncoder
import feign.okhttp.OkHttpClient
import feign.slf4j.Slf4jLogger

private val httpClient = OkHttpClient()

private val jackson = ObjectMapper()
        .registerKotlinModule()
        .configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false)
        .configure(DeserializationFeature.ACCEPT_EMPTY_ARRAY_AS_NULL_OBJECT, true)

fun feignBuilder(): Feign.Builder {
    return Feign.builder()
            .client(httpClient)
            .encoder(JacksonEncoder(jackson))
            .decoder(SlackDecoder(jackson))
            .logger(Slf4jLogger())
            .logLevel(Logger.Level.FULL)
}