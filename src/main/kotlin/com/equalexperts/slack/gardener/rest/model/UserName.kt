package com.equalexperts.slack.gardener.rest.model

data class UserName(val name: String) {
    override fun toString() = name
}