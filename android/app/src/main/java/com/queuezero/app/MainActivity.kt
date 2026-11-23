package com.queuezero.app

import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.queuezero.app.ui.navigation.AuthNavHost
import com.queuezero.app.ui.navigation.MainNavHost
import com.queuezero.app.ui.theme.QueueZeroTheme

class MainActivity : ComponentActivity() {

    // No action needed on the result: if the user refuses, calls to the backend simply fail
    // and the screens show their normal "can't reach the server" error with a retry button.
    private val localNetworkPermission =
        registerForActivityResult(ActivityResultContracts.RequestPermission()) { }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        requestLocalNetworkPermissionIfNeeded()
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
                        true -> MainNavHost(modifier = contentModifier)
                        false -> AuthNavHost(modifier = contentModifier)
                    }
                }
            }
        }
    }

    private fun requestLocalNetworkPermissionIfNeeded() {
        if (Build.VERSION.SDK_INT < 37) return
        val permission = "android.permission.ACCESS_LOCAL_NETWORK"
        if (checkSelfPermission(permission) != PackageManager.PERMISSION_GRANTED) {
            localNetworkPermission.launch(permission)
        }
    }
}
