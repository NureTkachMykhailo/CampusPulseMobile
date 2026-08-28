package com.mtkach.campuspulse.ui

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.mtkach.campuspulse.data.ChronicleRepository
import com.mtkach.campuspulse.data.Session
import com.mtkach.campuspulse.data.SessionStore
import com.mtkach.campuspulse.ui.screens.CategoriesScreen
import com.mtkach.campuspulse.ui.screens.ArticleFormScreen
import com.mtkach.campuspulse.ui.screens.DetailScreen
import com.mtkach.campuspulse.ui.screens.FeedScreen
import com.mtkach.campuspulse.ui.screens.LoginScreen

@Composable
fun CampusPulseApp(repository: ChronicleRepository, sessionStore: SessionStore) {
    val navController = rememberNavController()
    var session by remember { mutableStateOf(sessionStore.load()) }

    fun onSessionChanged(next: Session?) {
        if (next == null) sessionStore.clear() else sessionStore.save(next)
        session = next
    }

    NavHost(navController = navController, startDestination = "feed") {
        composable("feed") {
            FeedScreen(
                repository = repository,
                session = session,
                onOpenArticle = { navController.navigate("detail/$it") },
                onNewArticle = {
                    if (session == null) navController.navigate("login") else navController.navigate("form")
                },
                onOpenCategories = {
                    if (session == null) navController.navigate("login") else navController.navigate("categories")
                },
                onOpenLogin = { navController.navigate("login") },
                onLogout = { onSessionChanged(null) },
            )
        }
        composable("login") {
            LoginScreen(
                repository = repository,
                onLoggedIn = {
                    onSessionChanged(it)
                    navController.popBackStack()
                },
                onBack = { navController.popBackStack() },
            )
        }
        composable(
            "detail/{articleId}",
            arguments = listOf(navArgument("articleId") { type = NavType.LongType }),
        ) { backStackEntry ->
            val articleId = backStackEntry.arguments?.getLong("articleId") ?: 0L
            DetailScreen(
                repository = repository,
                session = session,
                articleId = articleId,
                onBack = { navController.popBackStack() },
                onEdit = { navController.navigate("form?id=$it") },
                onDeleted = { navController.popBackStack() },
                onRequireLogin = { navController.navigate("login") },
            )
        }
        composable(
            "form?id={id}",
            arguments = listOf(navArgument("id") { type = NavType.LongType; defaultValue = 0L }),
        ) { backStackEntry ->
            val id = backStackEntry.arguments?.getLong("id") ?: 0L
            val currentSession = session
            if (currentSession == null) {
                navController.popBackStack()
            } else {
                ArticleFormScreen(
                    repository = repository,
                    session = currentSession,
                    articleId = if (id == 0L) null else id,
                    onDone = { savedId ->
                        navController.popBackStack()
                        navController.navigate("detail/$savedId")
                    },
                    onCancel = { navController.popBackStack() },
                )
            }
        }
        composable("categories") {
            CategoriesScreen(
                repository = repository,
                onBack = { navController.popBackStack() },
            )
        }
    }
}
