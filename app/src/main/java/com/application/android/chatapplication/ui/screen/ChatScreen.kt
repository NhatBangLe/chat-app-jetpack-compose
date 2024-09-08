package com.application.android.chatapplication.ui.screen

import android.graphics.Color.rgb
import android.text.format.DateFormat
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.ShoppingCart
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.LocalTextStyle
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.application.android.chatapplication.constant.StateStatus
import com.application.android.chatapplication.data.entity.Message
import com.application.android.chatapplication.ui.component.ErrorBody
import com.application.android.chatapplication.ui.viewmodel.ChatViewModel

@Composable
fun ChatScreen(
    chatViewModel: ChatViewModel = hiltViewModel(),
    userId: String,
    conversationId: Long,
    navigateToHome: () -> Unit
) {
    val state by chatViewModel.state.collectAsState()
    val listState = rememberLazyListState()

    LaunchedEffect(key1 = "autoScrollToBottom") {
        listState.scrollToItem(state.messages.size)
    }

    Scaffold(
        modifier = Modifier.fillMaxSize(),
        topBar = {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                TopChatBar(
                    title = state.conversation?.title ?: "Unknown title",
                    modifier = Modifier
                        .padding(bottom = 3.dp)
                        .fillMaxWidth(),
                    onBackClick = navigateToHome
                ) {
                    IconButton(onClick = { chatViewModel.loadMoreMessages() }) {
                        Icon(
                            imageVector = Icons.Default.ShoppingCart,
                            contentDescription = "Load more messages"
                        )
                    }
                }
                if (state.messageStatus == StateStatus.LOADING)
                    LinearProgressIndicator(modifier = Modifier.fillMaxWidth())
            }
        },
        bottomBar = {
            ReplyBar(
                modifier = Modifier.fillMaxWidth(),
                onSendText = { newMessage ->
                    chatViewModel.sendMessage(message = newMessage, senderId = userId)
                }
            )
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier.padding(innerPadding),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            when (state.status) {
                StateStatus.INIT -> chatViewModel.joinChat(conversationId)
                StateStatus.LOADING -> LinearProgressIndicator()
                StateStatus.ERROR -> ErrorBody()
                StateStatus.SUCCESS -> ChatList(
                    state = listState,
                    userId = userId,
                    messages = state.messages
                )
            }
        }

    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TopChatBar(
    modifier: Modifier = Modifier,
    title: String = "",
    onBackClick: () -> Unit,
    actions: @Composable (RowScope.() -> Unit) = {}
) {
    TopAppBar(
        modifier = modifier,
        colors = TopAppBarDefaults.topAppBarColors().copy(
            containerColor = Color(50, 219, 137, 255)
        ),
        title = {
            Text(
                text = title,
                color = Color.White,
                fontWeight = FontWeight.Bold,
                fontSize = 20.sp
            )
        },
        navigationIcon = {
            IconButton(onClick = onBackClick) {
                Icon(
                    modifier = Modifier.size(55.dp),
                    imageVector = Icons.Default.Clear,
                    contentDescription = null,
                    tint = Color.White,
                )
            }
        },
        scrollBehavior = TopAppBarDefaults.pinnedScrollBehavior(),
        actions = actions
    )
}


@Composable
fun ReplyBar(
    modifier: Modifier = Modifier, onSendText: (String) -> Unit
) {
    var value by remember { mutableStateOf("") }

    Row(
        modifier = modifier
            .background(Color.White)
            .size(50.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.Center
    ) {
        BasicTextField(
            modifier = Modifier
                .background(
                    color = Color(217, 221, 224, 255),
                    shape = RoundedCornerShape(30.dp)
                )
                .fillMaxWidth(.8f)
                .fillMaxHeight(.7f),
            value = value,
            onValueChange = { newText ->
                value = newText
            },
            textStyle = TextStyle(
                textAlign = TextAlign.Start
            ),
            decorationBox = { innerTextField ->
                Row(
                    modifier = Modifier.padding(start = 10.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(Modifier.weight(1f)) {
                        if (value.isEmpty()) {
                            Text(
                                text = "Your message",
                                style = LocalTextStyle.current.copy(
                                    color = Color(139, 139, 139, 255),
                                    fontSize = 14.sp
                                )
                            )
                        }
                        innerTextField()
                    }
                }
            },
        )


        Spacer(modifier = Modifier.size(5.dp))
        IconButton(
            modifier = Modifier.size(35.dp),
            onClick = {
                if (value.isNotBlank()) {
                    onSendText(value)
                    value = ""
                }
            }
        ) {
            Icon(
                modifier = Modifier.fillMaxSize(.85f),
                imageVector = Icons.AutoMirrored.Filled.Send,
                contentDescription = null,
                tint = Color.Unspecified
            )
        }
    }
}

@Composable
fun ChatList(
    modifier: Modifier = Modifier,
    userId: String,
    state: LazyListState,
    messages: List<Message>
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        LazyColumn(
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth(),
            state = state
        ) {
            items(messages) { chatMessage ->
                val isOwnMessage = chatMessage.senderId == userId
                Box(
                    contentAlignment = if (isOwnMessage) {
                        Alignment.CenterEnd
                    } else Alignment.CenterStart, modifier = Modifier.fillMaxWidth()
                ) {
                    Column(
                        modifier = Modifier
                            .drawBehind {
                                val trianglePath = Path().apply {
                                    if (isOwnMessage) {
                                        moveTo(size.width, size.height)
                                        lineTo(size.width, size.height)
                                        lineTo(
                                            size.width, size.height
                                        )
                                        close()
                                    } else {
                                        moveTo(0f, size.height)
                                        lineTo(0f, size.height)
                                        lineTo(0.dp.toPx(), size.height)
                                        close()
                                    }
                                }
                                drawPath(
                                    path = trianglePath, color = if (isOwnMessage) Color(
                                        rgb(
                                            229, 239, 255
                                        )
                                    ) else Color.White
                                )
                            }
                            .background(
                                color = if (isOwnMessage) Color(rgb(229, 239, 255))
                                else Color.White, shape = RoundedCornerShape(15.dp)
                            )
                            .padding(8.dp)) {
                        Text(text = chatMessage.text, color = Color.Black)
                        Text(
                            modifier = Modifier.align(Alignment.End),
                            text = DateFormat.format("hh:mm", chatMessage.createdAt).toString(),
                            color = Color.Gray,
                            fontSize = 12.sp
                        )
                    }
                }
                Spacer(modifier = Modifier.size(10.dp))
            }
        }
    }
}

@Preview
@Composable
private fun ChatScreenPreview() {
    ChatScreen(userId = "", conversationId = 1L) {

    }
}