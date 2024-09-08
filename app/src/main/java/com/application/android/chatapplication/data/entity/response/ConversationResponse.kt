package com.application.android.chatapplication.data.entity.response

data class ConversationResponse(
    val id: Long,
    val title: String,
    val messageCount: Long,
    val participantCount: Int,
    val createdAt: Long,
    val updatedAt: Long,
    val creatorId: String,
)