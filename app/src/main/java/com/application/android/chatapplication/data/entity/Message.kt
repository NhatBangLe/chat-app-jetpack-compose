package com.application.android.chatapplication.data.entity

import com.application.android.chatapplication.constant.MessageType
import java.sql.Timestamp

data class Message(
    val id: String,
    val type: MessageType,
    val text: String,
    val createdAt: Timestamp,
    val senderId: String
)
