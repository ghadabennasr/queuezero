package com.queuezero.app

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.queuezero.app.ui.home.HomeRoute
import com.queuezero.app.ui.login.LoginRoute
import com.queuezero.app.ui.theme.QueueZeroTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        val repository = (application as QueueZeroApp).container.authRepository
        setContent {
            QueueZeroTheme {
                Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
                    // null = still reading the stored token, true = logged in, false = logged out
                    val loggedIn by repository.isLoggedIn
                        .collectAsStateWithLifecycle<Boolean?>(initialValue = null)
                    val contentModifier = Modifier.padding(innerPadding)
                    when (loggedIn) {
                        null -> Box(
                            modifier = contentModifier.fillMaxSize(),
                            contentAlignment = Alignment.Center
                        ) { CircularProgressIndicator() }
                        true -> HomeRoute(modifier = contentModifier)
                        false -> LoginRoute(modifier = contentModifier)
                    }
                }
            }
        }
    }
}
