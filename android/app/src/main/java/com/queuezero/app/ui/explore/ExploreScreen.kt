package com.queuezero.app.ui.explore

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.FilterChip
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.queuezero.app.data.remote.LocationSummaryDto

/** Connects the screen to its ViewModel. */
@Composable
fun ExploreRoute(
    onLocationClick: (Long) -> Unit,
    modifier: Modifier = Modifier,
    viewModel: ExploreViewModel = viewModel(factory = ExploreViewModel.Factory)
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    ExploreScreen(
        state = state,
        onQueryChanged = viewModel::onQueryChanged,
        onCategorySelected = viewModel::onCategorySelected,
        onRetry = viewModel::retry,
        onLocationClick = onLocationClick,
        modifier = modifier
    )
}

@Composable
fun ExploreScreen(
    state: ExploreUiState,
    onQueryChanged: (String) -> Unit,
    onCategorySelected: (Long?) -> Unit,
    onRetry: () -> Unit,
    onLocationClick: (Long) -> Unit,
    modifier: Modifier = Modifier
) {
    val error = state.errorMessage

    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp)
    ) {
        Text(
            text = "Explore",
            style = MaterialTheme.typography.headlineSmall,
            modifier = Modifier.padding(top = 16.dp, bottom = 8.dp)
        )

        OutlinedTextField(
            value = state.query,
            onValueChange = onQueryChanged,
            label = { Text("Search by name") },
            singleLine = true,
            modifier = Modifier.fillMaxWidth()
        )

        LazyRow(
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            modifier = Modifier.padding(vertical = 8.dp)
        ) {
            items(state.categories, key = { it.id }) { category ->
                FilterChip(
                    selected = state.selectedCategoryId == category.id,
                    onClick = { onCategorySelected(category.id) },
                    label = { Text(category.name) }
                )
            }
        }

        Box(modifier = Modifier.fillMaxSize()) {
            when {
                error != null -> Column(
                    modifier = Modifier.align(Alignment.Center),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = error,
                        color = MaterialTheme.colorScheme.error,
                        textAlign = TextAlign.Center
                    )
                    Button(onClick = onRetry, modifier = Modifier.padding(top = 12.dp)) {
                        Text("Retry")
                    }
                }

                state.isLoading && state.locations.isEmpty() ->
                    CircularProgressIndicator(modifier = Modifier.align(Alignment.Center))

                state.locations.isEmpty() -> Text(
                    text = "No locations match your search.",
                    modifier = Modifier.align(Alignment.Center)
                )

                else -> LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                    contentPadding = PaddingValues(bottom = 16.dp)
                ) {
                    items(state.locations, key = { it.id }) { location ->
                        LocationCard(location = location, onClick = { onLocationClick(location.id) })
                    }
                }
            }
        }
    }
}

@Composable
private fun LocationCard(location: LocationSummaryDto, onClick: () -> Unit) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(text = location.name, style = MaterialTheme.typography.titleMedium)
            val subtitle = listOfNotNull(location.categoryName, location.governorate)
                .joinToString(" - ")
            Text(
                text = subtitle,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.primary
            )
            location.address?.let {
                Text(text = it, style = MaterialTheme.typography.bodySmall)
            }
        }
    }
}
