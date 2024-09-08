package com.application.android.chatapplication.data.datasource.implement

import android.util.Log
import com.application.android.chatapplication.constant.ServiceHost
import com.application.android.chatapplication.data.datasource.AbstractClient
import com.application.android.chatapplication.data.datasource.IChatService
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
import com.google.gson.Gson
import io.ktor.client.call.body
import io.ktor.client.request.delete
import io.ktor.client.request.get
import io.ktor.client.request.patch
import io.ktor.client.request.post
import io.ktor.client.request.setBody
import io.ktor.http.ContentType
import io.ktor.http.HttpStatusCode
import io.ktor.http.contentType
import io.ktor.http.parameters
import io.reactivex.Completable
import io.reactivex.disposables.Disposable
import ua.naiksoftware.stomp.Stomp

class ChatServiceImpl : AbstractClient(), IChatService {

    private val serviceHost = ServiceHost.CHAT_SERVICE
    private val client = getClient(baseUrl = "http://${serviceHost}/api/v1/chat/")
    private val stompClient = Stomp.over(Stomp.ConnectionProvider.OKHTTP, "ws://$serviceHost/chat")

    override suspend fun getAllConversations(
        userId: String,
        pageNumber: Int,
        pageSize: Int
    ): List<ConversationResponse> {
        return client
            .get(urlString = "conversation/$userId/user") {
                url {
                    parameters {
                        append("pageNumber", "$pageNumber")
                        append("pageSize", "$pageSize")
                    }
                }
            }
            .body()
    }

    override suspend fun getConversation(conversationId: Long): ConversationResponse {
        return client.get(urlString = "conversation/$conversationId").body()
    }

    override suspend fun createConversation(createRequestData: ConversationCreateRequest): Long {
        return client
            .post(urlString = "conversation") {
                contentType(ContentType.Application.Json)
                setBody(createRequestData)
            }
            .body()
    }

    override suspend fun updateConversation(
        conversationId: Long,
        updateRequestData: ConversationUpdateRequest
    ): Boolean {
        val response = client
            .patch(urlString = "conversation/$conversationId") {
                contentType(ContentType.Application.Json)
                setBody(updateRequestData)
            }
        return response.status == HttpStatusCode.NoContent
    }

    override suspend fun deleteConversation(conversationId: Long): Boolean {
        val response = client.delete(urlString = "conversation/$conversationId")
        return response.status == HttpStatusCode.NoContent
    }

    override suspend fun getAllParticipants(
        conversationId: String,
        pageNumber: Int,
        pageSize: Int
    ): List<ParticipantResponse> {
        return client
            .get(urlString = "participant/$conversationId") {
                url {
                    parameters {
                        append("pageNumber", "$pageNumber")
                        append("pageSize", "$pageSize")
                    }
                }
            }
            .body()
    }

    override suspend fun addParticipant(createData: ParticipantCreateRequest): Long {
        return client
            .post(urlString = "participant") {
                contentType(ContentType.Application.Json)
                setBody(createData)
            }
            .body()
    }

    override suspend fun updateParticipant(
        participantId: Long,
        updateData: ParticipantUpdateRequest
    ): Boolean {
        val response = client
            .patch(urlString = "participant/$participantId") {
                contentType(ContentType.Application.Json)
                setBody(updateData)
            }
        return response.status == HttpStatusCode.NoContent
    }

    override suspend fun updateParticipant(
        conversationId: Long,
        userId: String,
        updateData: ParticipantUpdateRequest
    ): Boolean {
        val response = client
            .patch(urlString = "participant/$conversationId/$userId") {
                contentType(ContentType.Application.Json)
                setBody(updateData)
            }
        return response.status == HttpStatusCode.NoContent
    }

    override suspend fun deleteParticipant(
        participantId: Long
    ): Boolean {
        val response = client.delete(urlString = "participant/$participantId")
        return response.status == HttpStatusCode.NoContent
    }

    override suspend fun deleteParticipant(
        conversationId: Long,
        userId: String,
    ): Boolean {
        val response = client.delete(urlString = "participant/$conversationId/$userId")
        return response.status == HttpStatusCode.NoContent
    }

    override suspend fun getAllMessages(
        conversationId: Long,
        pageNumber: Int,
        pageSize: Int
    ): List<MessageResponse> {
        return client
            .get(urlString = "$conversationId") {
                url {
                    parameters {
                        append("pageNumber", "$pageNumber")
                        append("pageSize", "$pageSize")
                    }
                }
            }
            .body()
    }

    override suspend fun deleteMessage(messageId: Long): Boolean {
        val response = client.delete(urlString = "$messageId")
        return response.status == HttpStatusCode.NoContent
    }

    override suspend fun getAllAttachments(
        conversationId: String,
        pageNumber: Int,
        pageSize: Int
    ): List<AttachmentResponse> {
        return client
            .get(urlString = "attachment/$conversationId") {
                url {
                    parameters.append("pageNumber", "$pageNumber")
                    parameters.append("pageSize", "$pageSize")
                }
            }
            .body()
    }

    override fun connectWebSocketServer(userId: String): Completable {
        if (!stompClient.isConnected) Log.i(TAG, "Connected to WebSocket.")
        stompClient.connect()
        return sendMessage("/sc/user/$userId/connected")
    }

    override fun disconnectWebSocketServer(userId: String) {
        if (!stompClient.isConnected) return

        stompClient
            .send("/sc/user/$userId/disconnected")
            .retry(2)
            .onErrorComplete {
                Log.e(TAG, it.message ?: "Unknown exception")
                true
            }
            .doFinally {
                stompClient.disconnect()
            }
            .blockingAwait()
    }

    override fun sendMessage(
        destinationPath: String,
        payload: String?,
        retryTimes: Long,
        errorHandler: ((Throwable) -> Unit)?
    ): Completable {
        Log.i(TAG, "Send message to: $destinationPath\nPayload: $payload")
        Log.i(TAG, "STOMP Client: $stompClient")

        return stompClient
            .send(destinationPath, payload)
            .retry(retryTimes)
            .onErrorComplete { exception ->
                Log.e(TAG, exception.message ?: "Unknown exception")
                errorHandler?.let { errorHandler(exception) }
                false
            }
    }

    override fun sendMessage(
        conversationId: Long,
        message: ClientMessage,
    ): Completable {
        val payload = Gson().toJson(message)
        return sendMessage("/sc/conversation/$conversationId", payload)
    }

    override fun subscribeConversation(
        conversationId: Long,
        handler: (MessageResponse) -> Unit
    ): Disposable? {
        return subscribeTopic(
            "/topic/conversation/$conversationId",
            MessageResponse::class.java,
            handler
        )
    }

    override fun subscribeNotification(
        userId: String,
        handler: (NotificationResponse) -> Unit
    ): Disposable? {
        return subscribeTopic("/topic/user/$userId", NotificationResponse::class.java, handler)
    }

    override fun <T> subscribeTopic(
        destinationPath: String,
        classOfT: Class<T>,
        handler: (T) -> Unit
    ): Disposable? {
        return stompClient.topic(destinationPath)
            .subscribe { stompMessage ->
                val messageResponse =
                    Gson().fromJson(stompMessage.payload, classOfT)
                classOfT.cast(messageResponse)?.let(handler)
            }
    }

    companion object {
        const val TAG = "ChatServiceImpl"
    }

}