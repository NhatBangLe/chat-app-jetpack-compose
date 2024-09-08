package com.application.android.chatapplication.data.entity.request

import com.application.android.chatapplication.constant.ConversationType

data class ConversationCreateRequest(
    val title: String,
    val type: ConversationType,
    val creatorId: String,
)
