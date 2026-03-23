package com.b1nd.dodamdodam.nightstudy.domain.nightstudy.repository

import com.b1nd.dodamdodam.nightstudy.domain.nightstudy.entity.NightStudyEntity
import com.b1nd.dodamdodam.nightstudy.domain.nightstudy.entity.QNightStudyEntity.nightStudyEntity
import com.b1nd.dodamdodam.nightstudy.domain.nightstudy.entity.QNightStudyMemberEntity.nightStudyMemberEntity
import com.b1nd.dodamdodam.nightstudy.domain.nightstudy.enumeration.NightStudyStatusType
import com.b1nd.dodamdodam.nightstudy.domain.nightstudy.enumeration.NightStudyType
import com.querydsl.jpa.impl.JPAQueryFactory
import org.springframework.stereotype.Repository
import java.time.LocalDate
import java.util.UUID

@Repository
class NightStudyQueryRepositoryImpl(
    private val queryFactory: JPAQueryFactory
) : NightStudyQueryRepository {

    override fun findByPublicId(publicId: UUID): NightStudyEntity? {
        return queryFactory.selectFrom(nightStudyEntity)
            .where(nightStudyEntity.publicId.eq(publicId))
            .fetchOne()
    }

    override fun findAllByUserIdAndStatusAndType(
        userId: UUID,
        status: NightStudyStatusType,
        type: NightStudyType
    ): List<NightStudyEntity> {
        return queryFactory.select(nightStudyMemberEntity.nightStudy)
            .from(nightStudyMemberEntity)
            .where(
                nightStudyMemberEntity.userId.eq(userId),
                nightStudyMemberEntity.nightStudy.status.eq(status),
                nightStudyMemberEntity.nightStudy.type.eq(type)
            )
            .orderBy(nightStudyMemberEntity.nightStudy.id.asc())
            .fetch()
    }

    override fun findAllByType(type: NightStudyType): List<NightStudyEntity> {
        return queryFactory.selectFrom(nightStudyEntity)
            .where(nightStudyEntity.type.eq(type))
            .fetch()
    }

    override fun existsByPublicIdAndUserId(publicId: UUID, userId: UUID): Boolean {
        return queryFactory.selectOne()
            .from(nightStudyMemberEntity)
            .where(
                nightStudyMemberEntity.nightStudy.publicId.eq(publicId),
                nightStudyMemberEntity.userId.eq(userId)
            )
            .fetchFirst() != null
    }

    override fun existsByUserIdAndPeriodOverlap(userId: UUID, startAt: LocalDate, endAt: LocalDate): Boolean {
        return queryFactory.selectOne()
            .from(nightStudyMemberEntity)
            .join(nightStudyMemberEntity.nightStudy, nightStudyEntity)
            .where(
                nightStudyMemberEntity.userId.eq(userId),
                nightStudyEntity.startAt.loe(endAt),
                nightStudyEntity.endAt.goe(startAt)
            )
            .fetchFirst() != null
    }
}