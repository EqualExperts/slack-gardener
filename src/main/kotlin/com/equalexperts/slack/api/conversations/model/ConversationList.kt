package com.equalexperts.slack.api.conversations.model

import com.equalexperts.slack.api.rest.model.ResponseMetadata

data class ConversationList(val channels: List<Conversation>,
                            val response_metadata: ResponseMetadata)

