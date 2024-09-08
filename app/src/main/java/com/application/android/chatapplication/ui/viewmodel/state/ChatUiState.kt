package com.application.android.chatapplication.ui.viewmodel.state

import com.application.android.chatapplication.constant.StateStatus
import com.application.android.chatapplication.data.entity.Conversation
import com.application.android.chatapplication.data.entity.Message

data class ChatUiState(
    val conversation: Conversation? = null,
    val messages: List<Message> = emptyList(),
    val messagePage: Int = 0,
    val messageStatus: StateStatus = StateStatus.INIT,
    val status: StateStatus = StateStatus.INIT
)
