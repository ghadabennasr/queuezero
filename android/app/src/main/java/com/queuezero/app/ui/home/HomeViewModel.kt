package com.queuezero.app.ui.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider.AndroidViewModelFactory.Companion.APPLICATION_KEY
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.queuezero.app.QueueZeroApp
import com.queuezero.app.data.AppResult
import com.queuezero.app.data.remote.UserResponse
import com.queuezero.app.data.repository.AuthRepository
import com.queuezero.app.ui.common.toUserMessage
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

sealed interface HomeUiState {
    data object Loading : HomeUiState
    data class Ready(val user: UserResponse) : HomeUiState
    data class Error(val message: String) : HomeUiState
}

class HomeViewModel(private val repository: AuthRepository) : ViewModel() {

    private val _state = MutableStateFlow<HomeUiState>(HomeUiState.Loading)
    val state: StateFlow<HomeUiState> = _state.asStateFlow()

    fun load() {
        _state.value = HomeUiState.Loading
        viewModelScope.launch {
            _state.value = when (val result = repository.me()) {
                is AppResult.Success -> HomeUiState.Ready(result.value)
                is AppResult.Failure -> HomeUiState.Error(result.error.toUserMessage())
            }
        }
    }

    fun logout() {
        // Reset first so the next user never briefly sees the previous user's data.
        _state.value = HomeUiState.Loading
        viewModelScope.launch { repository.logout() }
    }

    companion object {
        val Factory = viewModelFactory {
            initializer {
                val app = this[APPLICATION_KEY] as QueueZeroApp
                HomeViewModel(app.container.authRepository)
            }
        }
    }
}
