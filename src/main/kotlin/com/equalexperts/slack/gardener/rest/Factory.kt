package com.equalexperts.slack.gardener.rest

import com.equalexperts.slack.gardener.rest.decoders.SlackDecoder
import com.fasterxml.jackson.databind.DeserializationFeature
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.module.kotlin.registerKotlinModule
import feign.Feign
import feign.jackson.JacksonEncoder
import feign.okhttp.OkHttpClient

private val httpClient = OkHttpClient()

private val jackson = ObjectMapper()
    .registerKotlinModule()
    .configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false)

fun feignBuilder(): Feign.Builder {
    return Feign.builder()
        .client(httpClient)
        .encoder(JacksonEncoder(jackson))
        .decoder(SlackDecoder(jackson))
}