package com.b1nd.dodamdodam.nightstudy.domain.team

import com.b1nd.dodamdodam.core.common.uuid.UuidV7
import com.b1nd.dodamdodam.core.jpa.entity.BaseTimeEntity
import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.GeneratedValue
import jakarta.persistence.GenerationType
import jakarta.persistence.Id
import jakarta.persistence.PrePersist
import jakarta.persistence.Table
import java.util.UUID

@Entity
@Table(name = "night_study_team")
class NightStudyTeamEntity(
    var name: String,
    var description: String? = null,
    var imageUrl: String? = null
): BaseTimeEntity() {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Long? = null

    @Column(nullable = false, unique = true)
    var publicId: UUID? = null
        protected set

    @PrePersist
    fun generatePublicId() {
        publicId = UuidV7.generate()
    }

    fun update(name: String, description: String?, imageUrl: String?) {
        this.name = name
        description?.let { this.description = it }
        imageUrl?.let { this.imageUrl = it }
    }
}