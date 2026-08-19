package com.b1nd.dodamdodam.nightstudy.domain.nightstudy.repository.nightStudy

import com.b1nd.dodamdodam.nightstudy.domain.nightstudy.entity.NightStudyEntity
import com.b1nd.dodamdodam.nightstudy.domain.nightstudy.enumeration.NightStudyStatusType
import jakarta.persistence.LockModeType
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Lock
import org.springframework.data.jpa.repository.Query
import org.springframework.data.repository.query.Param
import java.util.List
import java.util.UUID

interface NightStudyRepository: JpaRepository<NightStudyEntity, Long> {
    fun findAllByStatus(status: NightStudyStatusType): List<NightStudyEntity>

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("SELECT nightStudy FROM NightStudyEntity nightStudy WHERE nightStudy.publicId = :publicId")
    fun findByPublicIdForUpdate(@Param("publicId") publicId: UUID): NightStudyEntity?
}
