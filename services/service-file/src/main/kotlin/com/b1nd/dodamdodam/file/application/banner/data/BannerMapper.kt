package com.b1nd.dodamdodam.file.application.banner.data

import com.b1nd.dodamdodam.file.application.banner.data.response.BannerResponse
import com.b1nd.dodamdodam.file.domain.banner.entity.BannerEntity

fun BannerEntity.toResponse() = BannerResponse(
    id = id!!,
    imageUrl = imageUrl,
    linkUrl = linkUrl,
    isActive = isActive,
)
