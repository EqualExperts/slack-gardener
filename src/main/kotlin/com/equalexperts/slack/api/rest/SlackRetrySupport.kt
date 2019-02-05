package com.equalexperts.slack.api.rest

import feign.Response
import feign.RetryableException
import feign.Retryer
import feign.codec.ErrorDecoder
import java.time.LocalDateTime
import java.time.ZoneOffset
import java.util.*

class SlackRetrySupport(private val sleeper: (Long) -> Unit) : Retryer {
    override fun clone(): Retryer {
        return this
    }

    override fun continueOrPropagate(e: RetryableException) {
        if (e is SlackRetryException) {
            sleeper.invoke((e.secondsToWait + 1) * 1200L)
            return
        }
        throw e
    }

    class SlackRetryException(internal val secondsToWait: Long) : RetryableException("Retrying due to a slack 429 response", null, Date.from(LocalDateTime.now().plusSeconds(secondsToWait).toInstant(ZoneOffset.UTC)))

    class SlackErrorDecoder : ErrorDecoder {
        override fun decode(methodKey: String, response: Response): Exception {
            if (response.status() == 429) {
                throw SlackRetryException(response.headers()["Retry-After"]!!.first().toLong())
            }
            return default.decode(methodKey, response)
        }

        private val default = ErrorDecoder.Default()
    }
}