package com.b1nd.dodamdodam.core.common.data

data class InfinityScrollPageResponse<T>(
    val content: List<T>,
    val hasNext: Boolean,
)
