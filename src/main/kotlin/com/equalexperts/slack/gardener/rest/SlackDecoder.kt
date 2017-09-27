package com.equalexperts.slack.gardener.rest

import com.equalexperts.slack.gardener.rest.SlackRetrySupport.SlackRetryException
import com.fasterxml.jackson.databind.ObjectMapper
import feign.Response
import feign.Util
import feign.codec.Decoder
import java.lang.reflect.Type

class SlackDecoder(private val objectMapper: ObjectMapper) : Decoder {

    override fun decode(response: Response, type: Type?): Any? {
        try {
            if (response.status() == 404) {
                return Util.emptyValueOf(type)
            }

            if (response.body() == null) {
                return null
            }
            if (ByteArray::class.java == type) {
                return Util.toByteArray(response.body().asInputStream())
            }
            if (String::class.java == type) {
                return Util.toString(response.body().asReader())
            }

            val json = objectMapper.readTree(response.body().asInputStream())
            if (!json["ok"].booleanValue()) {
                //TODO: marshall the error a bit better
                throw RuntimeException(json["error"].textValue())
            }
            return objectMapper.convertValue(json, objectMapper.constructType(type))
        } finally {
            response.close()
        }
    }

}