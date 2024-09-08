package com.application.android.chatapplication.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.application.android.chatapplication.constant.StateStatus
import com.application.android.chatapplication.data.entity.Resource
import com.application.android.chatapplication.data.repository.ChatRepository
import com.application.android.chatapplication.ui.viewmodel.state.CreateConversationUiState
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class CreateConversationViewModel @Inject constructor(
    private val repository: ChatRepository
) : ViewModel() {

    private val _state = MutableStateFlow(CreateConversationUiState())
    val state = _state.asStateFlow()

    fun createConversation(
        title: String,
        creatorId: String,
        participantId: String,
        onCreatedCallback: (Long) -> Unit
    ) {
        _state.update { currentState -> currentState.copy(status = StateStatus.LOADING) }

        viewModelScope.launch {
            repository.createConversation(
                title = title,
                creatorId = creatorId,
                participantId = participantId
            ).collectLatest {
                    when (it) {
                        is Resource.Success -> {
                            _state.update { currentState -> currentState.copy(status = StateStatus.SUCCESS) }
                            onCreatedCallback(it.data)
                        }

                        is Resource.Error -> {
                            _state.update { currentState ->
                                currentState.copy(status = StateStatus.ERROR)
                            }
                        }
                    }
                }
        }
    }

}