package com.equalexperts.slack.gardener.rest

import feign.Response
import feign.RetryableException
import feign.Retryer
import feign.codec.ErrorDecoder
import java.lang.Exception

class SlackRetrySupport(private val sleeper: (Long) -> Unit) : Retryer {
    override fun clone(): Retryer {
        return this
    }

    override fun continueOrPropagate(e: RetryableException) {
        if (e is SlackRetryException) {
            sleeper.invoke(e.secondsToWait * 1000L)
            return
        }
        throw e
    }

    class SlackRetryException(internal val secondsToWait: Int) :
        RetryableException("Retrying due to a slack 429 response", null) {

    }

    class SlackErrorDecoder() : ErrorDecoder {
        override fun decode(methodKey: String, response: Response): Exception {
            if (response.status() == 429) {
                throw SlackRetryException(response.headers()["Retry-After"]!!.first().toInt())
            }
            return default.decode(methodKey, response)
        }

        private val default = ErrorDecoder.Default()
    }
}