package com.equalexperts.slack.api.users.model

import com.equalexperts.slack.api.rest.model.ResponseMetadata

data class UserList(val members: List<User>,
                    val response_metadata: ResponseMetadata)

