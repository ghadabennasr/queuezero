package com.queuezero.app.ui.login

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

data class LoginUiState(
    val isLoading: Boolean = false,
    val errorMessage: String? = null
)

class LoginViewModel(private val repository: AuthRepository) : ViewModel() {

    private val _state = MutableStateFlow(LoginUiState())
    val state: StateFlow<LoginUiState> = _state.asStateFlow()

    fun login(email: String, password: String) {
        if (_state.value.isLoading) return
        _state.value = LoginUiState(isLoading = true)
        viewModelScope.launch {
            when (val result = repository.login(email, password)) {
                // On success the stored token makes MainActivity switch to the home screen.
                is AppResult.Success -> _state.value = LoginUiState()
                is AppResult.Failure -> _state.value =
                    LoginUiState(errorMessage = result.error.toUserMessage())
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
                LoginViewModel(app.container.authRepository)
            }
        }
    }
}
