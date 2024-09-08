package com.application.android.chatapplication.ui.viewmodel

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.application.android.chatapplication.constant.StateStatus
import com.application.android.chatapplication.data.entity.Resource
import com.application.android.chatapplication.data.repository.ChatRepository
import com.application.android.chatapplication.ui.viewmodel.state.HomeUiState
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class HomeViewModel @Inject constructor(
    private val repository: ChatRepository
) : ViewModel() {

    private val _state = MutableStateFlow(HomeUiState())
    val state = _state.asStateFlow()

    fun initialize(userId: String) {
        repository.connectChatServer(userId)
            .onErrorComplete { false }
            .doOnError {
                Log.e(TAG, it.message ?: "Unknown exception")
                _state.update { current ->
                    current.copy(status = StateStatus.ERROR)
                }
            }
            .doOnComplete {
                loadConversations(userId)
                repository.registerNotificationHandler(userId) { notification ->
                    Log.i(TAG, "You have a new notification!")
                    notification.updatedConversationId?.let(::getNewestConversation)
                }
            }.blockingAwait()
    }

    fun loadConversations(userId: String) {
        _state.update { current -> current.copy(status = StateStatus.LOADING) }
        viewModelScope.launch {
            repository.getAllConversations(userId).collectLatest {
                when (it) {
                    is Resource.Success -> {
                        val conversations = it.data
                        _state.update { current ->
                            current.copy(
                                userId = userId,
                                status = StateStatus.SUCCESS,
                                conversations = conversations
                            )
                        }
                    }

                    is Resource.Error -> _state.update { current ->
                        current.copy(status = StateStatus.ERROR)
                    }
                }
            }
        }
    }

    /**
     * Load conversations, not appeared in current list, from server.
     */
    fun loadMoreConversations() {
        state.value.userId?.let { userId ->
            _state.update { current -> current.copy(status = StateStatus.LOADING) }

            viewModelScope.launch {
                val pageNumber = state.value.conversationPage + 1

                repository.getAllConversations(userId, pageNumber)
                    .collectLatest {
                        when (it) {
                            is Resource.Success -> {
                                val conversations = it.data
                                val currentConversations = state.value.conversations.toMutableList()
                                currentConversations.addAll(conversations) // add at the end of list

                                _state.update { current ->
                                    current.copy(
                                        conversations = currentConversations,
                                        status = StateStatus.SUCCESS
                                    )
                                }
                            }

                            is Resource.Error -> resolveError()
                        }
                    }
            }
        }
    }

    /**
     * Get a newest conversation has been notified.
     */
    private fun getNewestConversation(conversationId: Long) {
        Log.i(TAG, "Loading the newest conversation, ID: $conversationId")

        viewModelScope.launch {
            repository.getConversation(conversationId).collectLatest {
                when (it) {
                    is Resource.Success -> {
                        val currentConversations = state.value.conversations.toMutableList()
                        val existedIdx = currentConversations.indexOfFirst { conversation ->
                            conversation.id == it.data.id
                        }
                        if (existedIdx != -1) {
                            currentConversations.removeAt(existedIdx)
                            currentConversations.add(existedIdx, it.data)
                        } else currentConversations.add(0, it.data)

                        _state.update { currentState ->
                            currentState.copy(conversations = currentConversations)
                        }
                    }

                    is Resource.Error -> resolveError()
                }
            }
        }
    }

    fun disconnectChatServer() {
        state.value.userId?.let { repository.disconnectChatServer(it) }
    }

    private fun resolveError() {
        _state.update { current ->
            current.copy(status = StateStatus.ERROR)
        }
    }

    override fun onCleared() {
        disconnectChatServer()
        super.onCleared()
    }

    companion object {
        const val TAG = "HomeViewModel"
    }

}