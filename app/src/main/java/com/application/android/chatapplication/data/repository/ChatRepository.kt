package com.application.android.chatapplication.data.repository

import android.util.Log
import com.application.android.chatapplication.constant.ConversationType
import com.application.android.chatapplication.constant.ParticipantType
import com.application.android.chatapplication.data.datasource.IChatService
import com.application.android.chatapplication.data.entity.Conversation
import com.application.android.chatapplication.data.entity.Message
import com.application.android.chatapplication.data.entity.Resource
import com.application.android.chatapplication.data.entity.request.ClientMessage
import com.application.android.chatapplication.data.entity.request.ConversationCreateRequest
import com.application.android.chatapplication.data.entity.request.ParticipantCreateRequest
import com.application.android.chatapplication.data.entity.response.NotificationResponse
import io.reactivex.Completable
import io.reactivex.disposables.Disposable
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.flow
import java.sql.Timestamp

class ChatRepository(
    private val service: IChatService
) {
    private var notificationSubscriber: Disposable? = null
    private val registeredConversations = mutableMapOf<Long, Disposable?>()

    fun getAllConversations(
        userId: String,
        pageNumber: Int = 0,
        pageSize: Int = 6
    ): Flow<Resource<List<Conversation>>> {
        return flow<Resource<List<Conversation>>> {
            val conversations = service.getAllConversations(userId, pageNumber, pageSize)
                .map {
                    Conversation(
                        id = it.id,
                        title = it.title,
                        creatorId = it.creatorId,
                        updatedAt = Timestamp(it.updatedAt)
                    )
                }
            emit(Resource.Success(conversations))
        }.catch {
            emit(Resource.Error("Cannot get conversations"))
            Log.e(TAG, it.message ?: "Unknown exception")
        }
    }

    fun getConversation(conversationId: Long): Flow<Resource<Conversation>> {
        return flow<Resource<Conversation>> {
            val response = service.getConversation(conversationId)
            val conversation = Conversation(
                id = response.id,
                title = response.title,
                updatedAt = Timestamp(response.updatedAt),
                creatorId = response.creatorId
            )

            emit(Resource.Success(conversation))
        }.catch {
            emit(Resource.Error("Cannot get a conversation"))
            Log.e(TAG, it.message ?: "Unknown exception")
        }
    }

    fun createConversation(
        title: String,
        type: ConversationType = ConversationType.SINGLE,
        creatorId: String,
        participantId: String
    ): Flow<Resource<Long>> {
        val createRequest = ConversationCreateRequest(title, type, creatorId)
        return flow<Resource<Long>> {
            val conversationId = service.createConversation(createRequest)
            service.addParticipant(
                ParticipantCreateRequest(
                    participantId,
                    ParticipantType.MEMBER,
                    conversationId
                )
            )
            emit(Resource.Success(conversationId))
        }.catch {
            emit(Resource.Error("Cannot create a new conversation"))
            Log.e(TAG, it.message ?: "Unknown exception")
        }
    }

    fun loadMessageHistory(
        conversationId: Long,
        pageNumber: Int = 0,
        pageSize: Int = 6
    ): Flow<Resource<List<Message>>> {
        return flow<Resource<List<Message>>> {
            val result = service.getAllMessages(conversationId, pageNumber, pageSize).map {
                Message(it.id, it.type, it.text, Timestamp(it.createdAt), it.senderId)
            }
            emit(Resource.Success(result))
        }.catch {
            emit(Resource.Error("Cannot load message history"))
            Log.e(TAG, it.message ?: "Unknown exception")
        }
    }

    /**
     * Connect to Chat Server and register a notification handler.
     */
    fun connectChatServer(userId: String): Completable {
        return service.connectWebSocketServer(userId)
    }

    fun disconnectChatServer(userId: String) {
        registeredConversations.keys.forEach(this::unregisterMessageHandler)
        unregisterNotificationHandler()
        service.disconnectWebSocketServer(userId)
    }

    /**
     * Send a new message to server via WebSocket.
     */
    fun sendMessage(
        conversationId: Long,
        message: ClientMessage,
    ): Completable {
        return service.sendMessage(conversationId, message)
    }

    /**
     * Register an incoming message handler which will be called whenever having a new message.
     * In addition, this method is going to initialize a new client to configure and connect to WebSocket.
     */
    fun registerMessageHandler(conversationId: Long, handler: (Message) -> Unit) {
        if (registeredConversations.containsKey(conversationId)) return

        Log.i(TAG, "Registered message handler for conversation ID: $conversationId.")
        val disposable = service.subscribeConversation(conversationId) {
            val incomingMessage = Message(
                id = it.id,
                type = it.type,
                text = it.text,
                createdAt = Timestamp(it.createdAt),
                senderId = it.senderId
            )
            handler(incomingMessage)
        }
        registeredConversations[conversationId] = disposable
    }

    private fun unregisterMessageHandler(conversationId: Long) {
        if (!registeredConversations.containsKey(conversationId)) return
        val disposable = registeredConversations[conversationId]
        if (disposable?.isDisposed == false) disposable.dispose()
    }

    fun registerNotificationHandler(
        userId: String,
        handler: (NotificationResponse) -> Unit
    ) {
        if (notificationSubscriber != null) return
        val disposable = service.subscribeNotification(userId, handler)
        notificationSubscriber = disposable
    }

    private fun unregisterNotificationHandler() {
        notificationSubscriber?.dispose()
    }

    companion object {
        const val TAG = "ChatRepository"
    }

}