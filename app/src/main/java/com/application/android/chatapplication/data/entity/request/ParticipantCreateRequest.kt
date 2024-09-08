package com.application.android.chatapplication.data.entity.request

import com.application.android.chatapplication.constant.ParticipantType

data class ParticipantCreateRequest(
    val userId: String,
    val type: ParticipantType,
    val conversationId: Long
)
