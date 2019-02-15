package com.equalexperts.slack.api.conversations.model

import com.equalexperts.slack.api.rest.model.Message
import com.equalexperts.slack.api.rest.model.ResponseMetadata

data class ConversationHistory(val has_more: Boolean,
                               val messages: List<Message>,
                               val response_metadata: ResponseMetadata?)