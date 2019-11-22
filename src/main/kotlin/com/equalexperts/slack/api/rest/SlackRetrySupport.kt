package com.equalexperts.slack.api.rest

import feign.Response
import feign.RetryableException
import feign.Retryer
import feign.codec.ErrorDecoder
import org.slf4j.LoggerFactory
import java.time.LocalDateTime
import java.time.ZoneOffset
import java.util.*

class SlackRetrySupport(private val sleeper: (Long) -> Unit) : Retryer {
    private val logger = LoggerFactory.getLogger(this::class.java.name)
    override fun clone(): Retryer {
        return this
    }

    override fun continueOrPropagate(e: RetryableException) {
        if (e is SlackRetryException) {
            val additionalSecondsToWait = 1
            val secondsToWait = (e.secondsToWait + additionalSecondsToWait)  * 1000L
            if(logger.isTraceEnabled){
                logger.trace("Sleeping for $secondsToWait millis due to 429 response asking for ${e.secondsToWait} seconds as backoff")
            }
            sleeper.invoke(secondsToWait)
            return
        }
        throw e
    }

    class SlackRetryException(internal val secondsToWait: Long) : RetryableException("Retrying due to a slack 429 response", null, Date.from(LocalDateTime.now().plusSeconds(secondsToWait).toInstant(ZoneOffset.UTC)))

    class SlackErrorDecoder : ErrorDecoder {
        private val logger = LoggerFactory.getLogger(this::class.java.name)

        override fun decode(methodKey: String, response: Response): Exception {
            if (response.status() == 429) {
                if(logger.isTraceEnabled){
                    logger.trace("Asked to backoff for ${response.headers()["Retry-After"]}")
                }
                val secondsToWait = response.headers()["Retry-After"]!!.first().toLong()
                throw SlackRetryException(secondsToWait)
            }
            return default.decode(methodKey, response)
        }

        private val default = ErrorDecoder.Default()
    }
}
