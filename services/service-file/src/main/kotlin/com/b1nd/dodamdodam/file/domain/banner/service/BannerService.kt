package com.b1nd.dodamdodam.file.domain.banner.service

import com.b1nd.dodamdodam.file.domain.banner.entity.BannerEntity
import com.b1nd.dodamdodam.file.domain.banner.exception.BannerNotFoundException
import com.b1nd.dodamdodam.file.domain.banner.repository.BannerRepository
import org.springframework.data.repository.findByIdOrNull
import org.springframework.stereotype.Service

@Service
class BannerService(
    private val bannerRepository: BannerRepository,
) {
    fun save(banner: BannerEntity): BannerEntity = bannerRepository.save(banner)

    fun getById(id: Long): BannerEntity =
        bannerRepository.findByIdOrNull(id) ?: throw BannerNotFoundException()

    fun getAll(): List<BannerEntity> = bannerRepository.findAll()

    fun delete(id: Long) {
        val banner = getById(id)
        bannerRepository.delete(banner)
    }
}
