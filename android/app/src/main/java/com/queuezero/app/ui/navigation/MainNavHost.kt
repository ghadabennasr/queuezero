package com.queuezero.app.ui.navigation

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.queuezero.app.ui.explore.ExploreRoute
import com.queuezero.app.ui.home.HomeRoute

object MainRoutes {
    const val HOME = "home"
    const val EXPLORE = "explore"
    const val LOCATION = "location/{id}"

    fun location(id: Long) = "location/$id"
}

/** Screens available while logged in. */
@Composable
fun MainNavHost(modifier: Modifier = Modifier) {
    val navController = rememberNavController()
    NavHost(
        navController = navController,
        startDestination = MainRoutes.HOME,
        modifier = modifier
    ) {
        composable(MainRoutes.HOME) {
            HomeRoute(onExplore = { navController.navigate(MainRoutes.EXPLORE) })
        }
        composable(MainRoutes.EXPLORE) {
            ExploreRoute(onLocationClick = { id ->
                navController.navigate(MainRoutes.location(id))
            })
        }
        composable(
            route = MainRoutes.LOCATION,
            arguments = listOf(navArgument("id") { type = NavType.LongType })
        ) { entry ->
            val id = entry.arguments?.getLong("id") ?: 0L
            LocationPlaceholder(id = id, onBack = { navController.popBackStack() })
        }
    }
}

/** Temporary screen: replaced by the real location details in the next step. */
@Composable
private fun LocationPlaceholder(id: Long, onBack: () -> Unit) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(text = "Location #$id", style = MaterialTheme.typography.headlineSmall)
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            text = "Details and the live queue arrive in the next step.",
            textAlign = TextAlign.Center
        )
        Spacer(modifier = Modifier.height(24.dp))
        Button(onClick = onBack) { Text("Back") }
    }
}
