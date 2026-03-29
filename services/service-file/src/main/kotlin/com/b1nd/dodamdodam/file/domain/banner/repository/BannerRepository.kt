package com.b1nd.dodamdodam.file.domain.banner.repository

import com.b1nd.dodamdodam.file.domain.banner.entity.BannerEntity
import org.springframework.data.jpa.repository.JpaRepository

interface BannerRepository : JpaRepository<BannerEntity, Long>
