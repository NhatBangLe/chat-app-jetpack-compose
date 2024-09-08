package com.application.android.chatapplication.data.entity.response

import com.application.android.chatapplication.constant.ParticipantType

data class ParticipantResponse(
    val id: Long,
    val type: ParticipantType,
    val createdAtTimestamp: Long,
    val conversationId: Long,
    val userId: String
)
