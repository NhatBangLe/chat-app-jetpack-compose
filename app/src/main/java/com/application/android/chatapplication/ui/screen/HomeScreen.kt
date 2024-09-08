package com.application.android.chatapplication.ui.screen

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.ShoppingCart
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FabPosition
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.application.android.chatapplication.constant.StateStatus
import com.application.android.chatapplication.ui.component.ErrorBody
import com.application.android.chatapplication.ui.viewmodel.HomeViewModel
import java.util.Date

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    screenViewModel: HomeViewModel = hiltViewModel(),
    userId: String,
    navigateToLogin: () -> Unit,
    navigateToChatScreen: (Long) -> Unit,
    navigateToCreateScreen: () -> Unit
) {
    val state by screenViewModel.state.collectAsState()
    var showLogoutDialog by remember { mutableStateOf(false) }

    if (showLogoutDialog) {
        AlertDialog(
            title = {
                Text(text = "Logout")
            },
            text = {
                Text(text = "Do you want to log out?")
            },
            onDismissRequest = { showLogoutDialog = false },
            confirmButton = {
                Button(
                    border = BorderStroke(0.dp, Color.Transparent),
                    onClick = {
                        showLogoutDialog = false
                        screenViewModel.disconnectChatServer()
                        navigateToLogin()
                    }
                ) {
                    Text(text = "OK")
                }
            },
            dismissButton = {
                Button(
                    border = BorderStroke(0.dp, Color.Transparent),
                    onClick = { showLogoutDialog = false }
                ) {
                    Text(text = "Cancel")
                }
            }
        )
    }

    BackHandler { showLogoutDialog = true }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(text = "Chat Application") },
                actions = {
                    IconButton(onClick = { screenViewModel.loadConversations(userId) }) {
                        Icon(
                            imageVector = Icons.Default.Refresh,
                            contentDescription = "Reload conversations"
                        )
                    }
                }
            )
        },
        floatingActionButton = {
            FloatingActionButton(onClick = navigateToCreateScreen) {
                Icon(imageVector = Icons.Default.Add, contentDescription = null)
            }
        },
        floatingActionButtonPosition = FabPosition.End
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .padding(innerPadding)
                .fillMaxSize()
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 10.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Available conversations",
                    fontWeight = FontWeight.Bold,
                    style = MaterialTheme.typography.titleLarge
                )
                IconButton(onClick = { screenViewModel.loadMoreConversations() }) {
                    Icon(
                        imageVector = Icons.Default.ShoppingCart,
                        contentDescription = "Load more conversations"
                    )
                }
            }
            when (state.status) {
                StateStatus.INIT -> screenViewModel.initialize(userId)
                StateStatus.LOADING -> LinearProgressIndicator()
                StateStatus.ERROR -> ErrorBody()
                StateStatus.SUCCESS -> LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    items(state.conversations) { conversation ->
                        Spacer(modifier = Modifier.size(10.dp))
                        Row(
                            modifier = Modifier
                                .padding(horizontal = 10.dp)
                                .height(50.dp)
                                .fillMaxWidth()
                                .border(
                                    width = 1.dp,
                                    color = Color.Magenta,
                                    shape = RoundedCornerShape(5.dp)
                                )
                                .clickable { navigateToChatScreen(conversation.id) },
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(text = conversation.title)
                            Text(text = Date.from(conversation.updatedAt.toInstant()).toString())
                        }
                    }
                }
            }

        }
    }
}

@Preview
@Composable
private fun ConversationScreenPreview() {
    HomeScreen(userId = "", navigateToLogin = {}, navigateToChatScreen = {}) {

    }
}