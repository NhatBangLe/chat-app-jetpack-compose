package com.application.android.chatapplication.data.datasource

import com.application.android.chatapplication.data.entity.request.ClientMessage
import com.application.android.chatapplication.data.entity.request.ConversationCreateRequest
import com.application.android.chatapplication.data.entity.request.ConversationUpdateRequest
import com.application.android.chatapplication.data.entity.request.ParticipantCreateRequest
import com.application.android.chatapplication.data.entity.request.ParticipantUpdateRequest
import com.application.android.chatapplication.data.entity.response.AttachmentResponse
import com.application.android.chatapplication.data.entity.response.ConversationResponse
import com.application.android.chatapplication.data.entity.response.MessageResponse
import com.application.android.chatapplication.data.entity.response.NotificationResponse
import com.application.android.chatapplication.data.entity.response.ParticipantResponse
import io.reactivex.Completable
import io.reactivex.disposables.Disposable

interface IChatService {

    suspend fun getAllConversations(
        userId: String,
        pageNumber: Int = 0,
        pageSize: Int = 6
    ): List<ConversationResponse>

    suspend fun getConversation(conversationId: Long): ConversationResponse

    suspend fun createConversation(createRequestData: ConversationCreateRequest): Long

    suspend fun updateConversation(
        conversationId: Long,
        updateRequestData: ConversationUpdateRequest
    ): Boolean

    suspend fun deleteConversation(conversationId: Long): Boolean

    suspend fun getAllParticipants(
        conversationId: String,
        pageNumber: Int = 0,
        pageSize: Int = 6
    ): List<ParticipantResponse>

    suspend fun addParticipant(createData: ParticipantCreateRequest): Long

    suspend fun updateParticipant(
        participantId: Long,
        updateData: ParticipantUpdateRequest
    ): Boolean

    suspend fun updateParticipant(
        conversationId: Long,
        userId: String,
        updateData: ParticipantUpdateRequest
    ): Boolean

    suspend fun deleteParticipant(
        participantId: Long
    ): Boolean

    suspend fun deleteParticipant(
        conversationId: Long,
        userId: String,
    ): Boolean

    suspend fun getAllMessages(
        conversationId: Long,
        pageNumber: Int = 0,
        pageSize: Int = 6
    ): List<MessageResponse>

    suspend fun deleteMessage(messageId: Long): Boolean

    suspend fun getAllAttachments(
        conversationId: String,
        pageNumber: Int = 0,
        pageSize: Int = 6
    ): List<AttachmentResponse>

    /**
     * Initialize a STOMP client which is used for communicating via WebSocket.
     * This method is going to initialize a client and connect to the service server.
     * @param userId ID of a user who is connecting to server.
     */
    fun connectWebSocketServer(userId: String): Completable

    /**
     * @param userId ID of a user who is disconnecting to server.
     */
    fun disconnectWebSocketServer(userId: String)

    fun sendMessage(
        destinationPath: String,
        payload: String? = null,
        retryTimes: Long = 2,
        errorHandler: ((Throwable) -> Unit)? = null
    ): Completable

    /**
     * Send a new message to a conversation.
     */
    fun sendMessage(
        conversationId: Long,
        message: ClientMessage,
    ): Completable

    fun <T> subscribeTopic(
        destinationPath: String,
        classOfT: Class<T>,
        handler: (T) -> Unit
    ): Disposable?

    /**
     * Subscribe to a conversation.
     * If the conversation has a new message, the handler is going to be called.
     */
    fun subscribeConversation(conversationId: Long, handler: (MessageResponse) -> Unit): Disposable?

    fun subscribeNotification(userId: String, handler: (NotificationResponse) -> Unit): Disposable?

}