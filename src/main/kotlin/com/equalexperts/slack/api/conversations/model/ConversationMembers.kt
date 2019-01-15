package com.equalexperts.slack.api.conversations.model

import com.equalexperts.slack.api.rest.model.ResponseMetadata

data class ConversationMembers(val members: List<String>, val response_metadata: ResponseMetadata)
