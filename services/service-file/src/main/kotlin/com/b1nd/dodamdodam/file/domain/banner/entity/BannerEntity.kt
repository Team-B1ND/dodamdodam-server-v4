package com.b1nd.dodamdodam.file.domain.banner.entity

import com.b1nd.dodamdodam.core.jpa.entity.BaseTimeEntity
import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.GeneratedValue
import jakarta.persistence.GenerationType
import jakarta.persistence.Id
import jakarta.persistence.Table

@Entity
@Table(name = "banners")
class BannerEntity(
    @Column(nullable = false, length = 512)
    var imageUrl: String,

    @Column(nullable = false, length = 512)
    var linkUrl: String,

    @Column(nullable = false)
    var isActive: Boolean = false,
) : BaseTimeEntity() {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Long? = null

    fun updateActive(active: Boolean) {
        this.isActive = active
    }
}
