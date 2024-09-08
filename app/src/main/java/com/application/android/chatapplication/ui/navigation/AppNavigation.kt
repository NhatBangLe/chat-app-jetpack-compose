package com.application.android.chatapplication.ui.navigation

import androidx.compose.runtime.Composable
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.application.android.chatapplication.ui.screen.ChatScreen
import com.application.android.chatapplication.ui.screen.CreateConversationScreen
import com.application.android.chatapplication.ui.screen.HomeScreen
import com.application.android.chatapplication.ui.screen.LoginScreen

@Composable
fun AppNavigation() {
    val controller = rememberNavController()

    NavHost(navController = controller, startDestination = Route.LOGIN_SCREEN) {
        composable(route = Route.LOGIN_SCREEN) {
            LoginScreen { userId ->
                controller.navigate("${Route.HOME_SCREEN}/$userId")
            }
        }

        composable(
            route = "${Route.HOME_SCREEN}/{userId}",
            arguments = listOf(
                navArgument("userId") {
                    type = NavType.StringType
                }
            )
        ) { stackEntry ->
            val userId = stackEntry.arguments?.getString("userId")!!
            val navigateToLogin: () -> Unit = {
                controller.popBackStack()
            }
            val navigateToChatScreen: (Long) -> Unit = { conversationId ->
                controller.navigate(
                    route = "${Route.CHAT_SCREEN}/$conversationId/$userId"
                )
            }

            HomeScreen(
                userId = userId,
                navigateToLogin = navigateToLogin,
                navigateToChatScreen = navigateToChatScreen
            ) {
                controller.navigate("${Route.CREATE_SCREEN}/$userId")
            }
        }

        composable(
            route = "${Route.CREATE_SCREEN}/{userId}",
            arguments = listOf(
                navArgument("userId") {
                    type = NavType.StringType
                }
            )
        ) { stackEntry ->
            val userId = stackEntry.arguments?.getString("userId")!!
            val navigateToChatScreen: (Long) -> Unit = { conversationId ->
                controller.navigate(
                    route = "${Route.CHAT_SCREEN}/$conversationId/$userId"
                )
            }

            CreateConversationScreen(
                userId = userId,
                navigateToChatScreen = navigateToChatScreen
            )
        }

        composable(
            route = "${Route.CHAT_SCREEN}/{conversationId}/{userId}",
            arguments = listOf(
                navArgument("conversationId") {
                    type = NavType.LongType
                },
                navArgument("userId") {
                    type = NavType.StringType
                }
            )
        ) { stackEntry ->
            val conversationId = stackEntry.arguments?.getLong("conversationId")!!
            val userId = stackEntry.arguments?.getString("userId")!!

            ChatScreen(userId = userId, conversationId = conversationId) {
                controller.popBackStack(
                    route = "${Route.HOME_SCREEN}/$userId",
                    inclusive = false
                )
            }
        }
    }
}