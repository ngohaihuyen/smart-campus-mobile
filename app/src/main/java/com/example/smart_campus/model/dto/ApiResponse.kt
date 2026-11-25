package com.example.smart_campus.model.dto

import kotlinx.serialization.Serializable

@Serializable
data class ApiResponse<T>(
    val success: Boolean,
    val message: String = "",
    val data: T? = null,
    val error: ApiError? = null
)


@Serializable
data class ApiError(val code: String)

@Serializable
data class PagedData<T>(
    val items: List<T>,
    val page: Int,
    val limit: Int,
    val total: Int
)