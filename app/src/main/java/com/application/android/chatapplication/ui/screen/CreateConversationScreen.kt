package com.application.android.chatapplication.ui.screen

import android.util.Log
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Devices
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.application.android.chatapplication.constant.StateStatus
import com.application.android.chatapplication.ui.component.ErrorBody
import com.application.android.chatapplication.ui.viewmodel.CreateConversationViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CreateConversationScreen(
    screenViewModel: CreateConversationViewModel = hiltViewModel(),
    userId: String,
    navigateToChatScreen: (Long) -> Unit
) {
    val state by screenViewModel.state.collectAsState()
    var title by remember { mutableStateOf("") }
    var participantId by remember { mutableStateOf("") }

    Scaffold(
        topBar = {
            TopAppBar(title = { Text(text = "Chat Application") })
        },
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .padding(innerPadding)
                .fillMaxSize(),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            TextField(
                placeholder = {
                    Text(text = "Conversation title")
                },
                value = title,
                onValueChange = { title = it }
            )
            Spacer(modifier = Modifier.size(10.dp))
            TextField(
                placeholder = {
                    Text(text = "Participant ID")
                },
                value = participantId,
                onValueChange = { participantId = it }
            )
            Spacer(modifier = Modifier.size(10.dp))
            Button(
                onClick = {
                    screenViewModel.createConversation(
                        title,
                        userId,
                        participantId,
                        navigateToChatScreen
                    )
                }
            ) {
                Text(text = "Join chat")
            }
            Spacer(modifier = Modifier.size(10.dp))
            when (state.status) {
                StateStatus.LOADING -> LinearProgressIndicator()
                StateStatus.ERROR -> ErrorBody()
                else -> {}
            }
        }
    }
}

@Preview(showBackground = true, device = Devices.PIXEL_6_PRO)
@Composable
private fun HomeScreenPreview() {
    CreateConversationScreen(userId = "") { conversationId ->
        Log.i("Test", "$conversationId")
    }
}