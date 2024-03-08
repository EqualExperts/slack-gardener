package com.equalexperts.slack.api.rest.model

import feign.Param

abstract class Expander<T : Any> : Param.Expander {
    override fun expand(value: Any?): String {
        if (value == null) {
            throw NullPointerException("value must not be null")
        }
        @Suppress("UNCHECKED_CAST")
        return this.expandParameter(value as T)
    }

    abstract fun expandParameter(value: T): String
}