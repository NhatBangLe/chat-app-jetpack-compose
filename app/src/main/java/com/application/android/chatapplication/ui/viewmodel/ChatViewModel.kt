package com.application.android.chatapplication.ui.viewmodel

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.application.android.chatapplication.constant.StateStatus
import com.application.android.chatapplication.data.entity.Resource
import com.application.android.chatapplication.data.entity.request.ClientMessage
import com.application.android.chatapplication.data.repository.ChatRepository
import com.application.android.chatapplication.ui.viewmodel.state.ChatUiState
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class ChatViewModel @Inject constructor(
    private val repository: ChatRepository
) : ViewModel() {

    private val _state = MutableStateFlow(ChatUiState())
    val state = _state.asStateFlow()

    fun joinChat(conversationId: Long) {
        _state.update { currentState -> currentState.copy(status = StateStatus.LOADING) }
        viewModelScope.launch {
            repository.getConversation(conversationId).collectLatest {
                when (it) {
                    is Resource.Success -> {
                        registerReceivingMessageCallback(conversationId)

                        _state.update { currentState ->
                            currentState.copy(
                                status = StateStatus.SUCCESS,
                                conversation = it.data
                            )
                        }
                    }

                    is Resource.Error -> resolveResourceError()
                }
            }

            // load messages
            repository.loadMessageHistory(conversationId).collectLatest {
                when (it) {
                    is Resource.Success -> _state.update { currentState ->
                        val messages = it.data.sortedBy { message -> message.createdAt }
                        currentState.copy(messages = messages)
                    }

                    is Resource.Error -> resolveResourceError()
                }
            }
        }
    }

    fun sendMessage(message: String, senderId: String) {
        state.value.conversation?.let { conversation ->
            _state.update { it.copy(messageStatus = StateStatus.LOADING) }

            repository.sendMessage(
                conversationId = conversation.id,
                message = ClientMessage(text = message, senderId = senderId)
            )
                .onErrorComplete { false }
                .doOnError { exception ->
                    Log.e(TAG, exception.message ?: "Unknown exception")
                    _state.update { it.copy(messageStatus = StateStatus.ERROR) }
                }
                .doOnComplete {
                    Log.i(TAG, "Message sent by sender $senderId!")
                    _state.update { it.copy(messageStatus = StateStatus.SUCCESS) }
                }.blockingAwait()
        }
    }

    fun loadMoreMessages() {
        state.value.conversation?.let { conversation ->
            _state.update { currentState -> currentState.copy(status = StateStatus.LOADING) }

            viewModelScope.launch {
                val newMessagePage = state.value.messagePage + 1

                repository.loadMessageHistory(
                    conversationId = conversation.id,
                    pageNumber = newMessagePage,
                    pageSize = PAGE_SIZE
                ).collectLatest {
                    when (it) {
                        is Resource.Success -> {
                            val newMessages = it.data
                            val currentMessages = state.value.messages.toMutableList()
                            currentMessages.addAll(0, newMessages) // add at the head list
                            _state.update { currentState ->
                                currentState.copy(
                                    status = StateStatus.SUCCESS,
                                    messages = currentMessages,
                                    messagePage = newMessagePage
                                )
                            }
                        }

                        is Resource.Error -> resolveResourceError()
                    }
                }
            }
        }
    }

    private fun resolveResourceError() {
        _state.update { currentState ->
            currentState.copy(status = StateStatus.ERROR)
        }
    }

    private fun registerReceivingMessageCallback(conversationId: Long) {
        repository.registerMessageHandler(conversationId = conversationId) { newMessage ->
            val currentMessages = state.value.messages.toMutableList()
            currentMessages.add(newMessage)
            _state.update { currentState -> currentState.copy(messages = currentMessages) }
        }
    }

    companion object {
        const val PAGE_SIZE = 6
        const val TAG = "ChatViewModel"
    }

}