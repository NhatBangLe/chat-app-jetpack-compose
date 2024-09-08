package com.application.android.chatapplication.data.entity.request

data class ClientMessage(
    val text: String,
    val attachments: List<ClientAttachment>? = null,
    val senderId: String
)