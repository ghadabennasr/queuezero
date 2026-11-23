package com.queuezero.app.data.remote

import kotlinx.serialization.Serializable

@Serializable
data class CategoryDto(
    val id: Long,
    val code: String,
    val name: String,
    val icon: String? = null
)

@Serializable
data class LocationSummaryDto(
    val id: Long,
    val name: String,
    val address: String? = null,
    val governorate: String? = null,
    val latitude: Double,
    val longitude: Double,
    val categoryCode: String,
    val categoryName: String
)
