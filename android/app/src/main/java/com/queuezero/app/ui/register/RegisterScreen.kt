package com.queuezero.app.ui.register

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.queuezero.app.ui.theme.QueueZeroTheme

/** Connects the screen to its ViewModel. */
@Composable
fun RegisterRoute(
    onBack: () -> Unit,
    modifier: Modifier = Modifier,
    viewModel: RegisterViewModel = viewModel(factory = RegisterViewModel.Factory)
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    RegisterScreen(
        state = state,
        onRegister = viewModel::register,
        onInputChanged = viewModel::clearError,
        onBack = onBack,
        modifier = modifier
    )
}

@Composable
fun RegisterScreen(
    state: RegisterUiState,
    onRegister: (fullName: String, email: String, password: String) -> Unit,
    onInputChanged: () -> Unit,
    onBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    var fullName by rememberSaveable { mutableStateOf("") }
    var email by rememberSaveable { mutableStateOf("") }
    var password by rememberSaveable { mutableStateOf("") }
    var confirm by rememberSaveable { mutableStateOf("") }
    var showPassword by rememberSaveable { mutableStateOf(false) }
    var nameError by rememberSaveable { mutableStateOf<String?>(null) }
    var emailError by rememberSaveable { mutableStateOf<String?>(null) }
    var passwordError by rememberSaveable { mutableStateOf<String?>(null) }
    var confirmError by rememberSaveable { mutableStateOf<String?>(null) }

    val passwordTransformation = if (showPassword) {
        VisualTransformation.None
    } else {
        PasswordVisualTransformation()
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 24.dp, vertical = 16.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = "Create your account",
            style = MaterialTheme.typography.headlineMedium,
            color = MaterialTheme.colorScheme.primary
        )

        Spacer(modifier = Modifier.height(24.dp))

        OutlinedTextField(
            value = fullName,
            onValueChange = {
                fullName = it
                nameError = null
                onInputChanged()
            },
            label = { Text("Full name") },
            singleLine = true,
            enabled = !state.isLoading,
            isError = nameError != null,
            supportingText = { nameError?.let { Text(it) } },
            keyboardOptions = KeyboardOptions(
                capitalization = KeyboardCapitalization.Words,
                keyboardType = KeyboardType.Text
            ),
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(modifier = Modifier.height(8.dp))

        OutlinedTextField(
            value = email,
            onValueChange = {
                email = it
                emailError = null
                onInputChanged()
            },
            label = { Text("Email") },
            singleLine = true,
            enabled = !state.isLoading,
            isError = emailError != null,
            supportingText = { emailError?.let { Text(it) } },
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email),
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(modifier = Modifier.height(8.dp))

        OutlinedTextField(
            value = password,
            onValueChange = {
                password = it
                passwordError = null
                onInputChanged()
            },
            label = { Text("Password (8 characters or more)") },
            singleLine = true,
            enabled = !state.isLoading,
            isError = passwordError != null,
            supportingText = { passwordError?.let { Text(it) } },
            visualTransformation = passwordTransformation,
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
            trailingIcon = {
                TextButton(onClick = { showPassword = !showPassword }) {
                    Text(if (showPassword) "Hide" else "Show")
                }
            },
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(modifier = Modifier.height(8.dp))

        OutlinedTextField(
            value = confirm,
            onValueChange = {
                confirm = it
                confirmError = null
                onInputChanged()
            },
            label = { Text("Confirm password") },
            singleLine = true,
            enabled = !state.isLoading,
            isError = confirmError != null,
            supportingText = { confirmError?.let { Text(it) } },
            visualTransformation = passwordTransformation,
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(modifier = Modifier.height(16.dp))

        Button(
            onClick = {
                nameError = if (fullName.trim().length >= 2) null else "Enter your full name"
                emailError = if (email.contains("@") && email.contains(".")) null
                else "Enter a valid email"
                passwordError = when {
                    password.length < 8 -> "At least 8 characters"
                    password.length > 72 -> "At most 72 characters"
                    else -> null
                }
                confirmError = if (confirm == password) null else "Passwords do not match"
                val valid = listOf(nameError, emailError, passwordError, confirmError)
                    .all { it == null }
                if (valid) {
                    onRegister(fullName, email, password)
                }
            },
            enabled = !state.isLoading,
            modifier = Modifier.fillMaxWidth()
        ) {
            if (state.isLoading) {
                CircularProgressIndicator(
                    modifier = Modifier.size(20.dp),
                    strokeWidth = 2.dp
                )
            } else {
                Text("Create account")
            }
        }

        state.errorMessage?.let {
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = it,
                color = MaterialTheme.colorScheme.error,
                textAlign = TextAlign.Center
            )
        }

        TextButton(onClick = onBack, enabled = !state.isLoading) {
            Text("Back to log in")
        }

        Spacer(modifier = Modifier.height(16.dp))

        Text(
            text = "Waiting times are estimates based on simulated data.",
            style = MaterialTheme.typography.bodySmall,
            textAlign = TextAlign.Center
        )
    }
}

@Preview(showBackground = true)
@Composable
private fun RegisterScreenPreview() {
    QueueZeroTheme {
        RegisterScreen(
            state = RegisterUiState(),
            onRegister = { _, _, _ -> },
            onInputChanged = {},
            onBack = {}
        )
    }
}
