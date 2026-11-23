package com.queuezero.app.ui.explore

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider.AndroidViewModelFactory.Companion.APPLICATION_KEY
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.queuezero.app.QueueZeroApp
import com.queuezero.app.data.AppResult
import com.queuezero.app.data.remote.CategoryDto
import com.queuezero.app.data.remote.LocationSummaryDto
import com.queuezero.app.data.repository.LocationRepository
import com.queuezero.app.ui.common.toUserMessage
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class ExploreUiState(
    val isLoading: Boolean = true,
    val categories: List<CategoryDto> = emptyList(),
    val locations: List<LocationSummaryDto> = emptyList(),
    val selectedCategoryId: Long? = null,
    val query: String = "",
    val errorMessage: String? = null
)

class ExploreViewModel(private val repository: LocationRepository) : ViewModel() {

    private val _state = MutableStateFlow(ExploreUiState())
    val state: StateFlow<ExploreUiState> = _state.asStateFlow()

    private var searchJob: Job? = null

    init {
        loadAll()
    }

    fun retry() = loadAll()

    private fun loadAll() {
        searchJob?.cancel()
        searchJob = viewModelScope.launch {
            _state.update { it.copy(isLoading = true, errorMessage = null) }
            // The category chips are a convenience: if they fail, the list still loads or reports its own error.
            val categories = repository.categories()
            if (categories is AppResult.Success) {
                _state.update { it.copy(categories = categories.value) }
            }
            loadLocations()
        }
    }

    fun onQueryChanged(query: String) {
        _state.update { it.copy(query = query) }
        searchJob?.cancel()
        searchJob = viewModelScope.launch {
            delay(300) // wait until the user pauses typing
            loadLocations()
        }
    }

    fun onCategorySelected(categoryId: Long?) {
        _state.update {
            it.copy(selectedCategoryId = if (it.selectedCategoryId == categoryId) null else categoryId)
        }
        searchJob?.cancel()
        searchJob = viewModelScope.launch { loadLocations() }
    }

    private suspend fun loadLocations() {
        _state.update { it.copy(isLoading = true, errorMessage = null) }
        val current = _state.value
        when (val result = repository.locations(current.selectedCategoryId, current.query)) {
            is AppResult.Success ->
                _state.update { it.copy(isLoading = false, locations = result.value) }
            is AppResult.Failure ->
                _state.update { it.copy(isLoading = false, errorMessage = result.error.toUserMessage()) }
        }
    }

    companion object {
        val Factory = viewModelFactory {
            initializer {
                val app = this[APPLICATION_KEY] as QueueZeroApp
                ExploreViewModel(app.container.locationRepository)
            }
        }
    }
}
