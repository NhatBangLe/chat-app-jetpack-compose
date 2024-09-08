package com.application.android.chatapplication.ui.viewmodel.state

import com.application.android.chatapplication.constant.StateStatus
import com.application.android.chatapplication.data.entity.Conversation

data class HomeUiState(
    val userId: String? = null,
    val conversations: List<Conversation> = emptyList(),
    val conversationPage: Int = 0,
    val status: StateStatus = StateStatus.INIT
)
