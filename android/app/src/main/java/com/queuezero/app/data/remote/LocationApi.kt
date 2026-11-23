package com.queuezero.app.data.remote

import retrofit2.http.GET
import retrofit2.http.Query

interface LocationApi {

    @GET("api/categories")
    suspend fun categories(): List<CategoryDto>

    // A null parameter is simply left out of the URL.
    @GET("api/locations")
    suspend fun locations(
        @Query("categoryId") categoryId: Long?,
        @Query("q") query: String?
    ): List<LocationSummaryDto>
}
