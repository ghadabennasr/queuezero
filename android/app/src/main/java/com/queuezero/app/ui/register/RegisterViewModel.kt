package com.queuezero.app.ui.register

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider.AndroidViewModelFactory.Companion.APPLICATION_KEY
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.queuezero.app.QueueZeroApp
import com.queuezero.app.data.AppResult
import com.queuezero.app.data.repository.AuthRepository
import com.queuezero.app.ui.common.toUserMessage
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class RegisterUiState(
    val isLoading: Boolean = false,
    val errorMessage: String? = null
)

class RegisterViewModel(private val repository: AuthRepository) : ViewModel() {

    private val _state = MutableStateFlow(RegisterUiState())
    val state: StateFlow<RegisterUiState> = _state.asStateFlow()

    /** Creates the account, then signs in with the same credentials. */
    fun register(fullName: String, email: String, password: String) {
        if (_state.value.isLoading) return
        _state.value = RegisterUiState(isLoading = true)
        viewModelScope.launch {
            val created = repository.register(email, fullName, password)
            if (created is AppResult.Failure) {
                _state.value = RegisterUiState(errorMessage = created.error.toUserMessage())
                return@launch
            }
            // On success the stored token makes MainActivity switch to the home screen.
            _state.value = when (val login = repository.login(email, password)) {
                is AppResult.Success -> RegisterUiState()
                is AppResult.Failure -> RegisterUiState(
                    errorMessage = "Account created, but automatic sign-in failed: " +
                        login.error.toUserMessage() + " Go back and log in."
                )
            }
        }
    }

    fun clearError() {
        _state.update { it.copy(errorMessage = null) }
    }

    companion object {
        val Factory = viewModelFactory {
            initializer {
                val app = this[APPLICATION_KEY] as QueueZeroApp
                RegisterViewModel(app.container.authRepository)
            }
        }
    }
}
