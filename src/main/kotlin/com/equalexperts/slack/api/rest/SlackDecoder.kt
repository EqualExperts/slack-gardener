package com.equalexperts.slack.api.rest

import com.fasterxml.jackson.databind.ObjectMapper
import feign.Response
import feign.Util
import feign.codec.Decoder
import java.lang.reflect.Type

class SlackDecoder(private val objectMapper: ObjectMapper) : Decoder {

    override fun decode(response: Response, type: Type?): Any? {
        response.use { resp ->
            if (resp.status() == 404) {
                return Util.emptyValueOf(type)
            }

            if (resp.body() == null) {
                return null
            }
            if (ByteArray::class.java == type) {
                return Util.toByteArray(resp.body().asInputStream())
            }
            if (String::class.java == type) {
                return Util.toString(resp.body().asReader())
            }

            val json = objectMapper.readTree(resp.body().asInputStream())
            if (!json["ok"].booleanValue()) {
                //TODO: marshall the error a bit better
                throw RuntimeException(json["error"].textValue())
            }
            return objectMapper.convertValue(json, objectMapper.constructType(type))
        }
    }

}