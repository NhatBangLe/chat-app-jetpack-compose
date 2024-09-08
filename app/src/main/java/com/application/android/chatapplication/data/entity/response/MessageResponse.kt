package com.application.android.chatapplication.data.entity.response

import com.application.android.chatapplication.constant.MessageType

data class MessageResponse(
    val id: String,
    val type: MessageType,
    val text: String,
    val createdAt: Long,
    val senderId: String,
    val conversationId: Long
)