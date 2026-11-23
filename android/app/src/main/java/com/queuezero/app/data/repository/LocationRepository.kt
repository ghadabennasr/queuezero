package com.queuezero.app.data.repository

import com.queuezero.app.data.AppResult
import com.queuezero.app.data.remote.CategoryDto
import com.queuezero.app.data.remote.LocationApi
import com.queuezero.app.data.remote.LocationSummaryDto
import com.queuezero.app.data.remote.safeApiCall
import kotlinx.serialization.json.Json

class LocationRepository(
    private val api: LocationApi,
    private val json: Json
) {

    suspend fun categories(): AppResult<List<CategoryDto>> =
        safeApiCall(json) { api.categories() }

    suspend fun locations(categoryId: Long?, query: String): AppResult<List<LocationSummaryDto>> =
        safeApiCall(json) { api.locations(categoryId, query.trim().ifEmpty { null }) }
}
