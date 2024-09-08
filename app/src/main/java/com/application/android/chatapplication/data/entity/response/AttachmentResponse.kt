package com.application.android.chatapplication.data.entity.response

data class AttachmentResponse(
    val id: String,
    val messageId: Long,
    val fileUrl: String,
    val createdAtTimestamp: Long
)
