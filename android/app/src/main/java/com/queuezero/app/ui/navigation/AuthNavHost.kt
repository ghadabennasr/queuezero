package com.queuezero.app.ui.navigation

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.queuezero.app.ui.login.LoginRoute
import com.queuezero.app.ui.register.RegisterRoute

object Routes {
    const val LOGIN = "login"
    const val REGISTER = "register"
}

/**
 * Screens available while logged out.
 * Logging in (or registering, which logs in) stores a token, and MainActivity then replaces
 * this whole graph with the logged-in screens, so no manual navigation is needed.
 */
@Composable
fun AuthNavHost(modifier: Modifier = Modifier) {
    val navController = rememberNavController()
    NavHost(
        navController = navController,
        startDestination = Routes.LOGIN,
        modifier = modifier
    ) {
        composable(Routes.LOGIN) {
            LoginRoute(onCreateAccount = { navController.navigate(Routes.REGISTER) })
        }
        composable(Routes.REGISTER) {
            RegisterRoute(onBack = { navController.popBackStack() })
        }
    }
}
