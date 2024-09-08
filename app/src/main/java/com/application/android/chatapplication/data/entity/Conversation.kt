package com.application.android.chatapplication.data.entity

import java.sql.Timestamp

data class Conversation(
    val id: Long,
    val title: String,
    val updatedAt: Timestamp,
    val creatorId: String,
)
