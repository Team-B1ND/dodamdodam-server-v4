package com.b1nd.dodamdodam.nightstudy.domain.nightstudy.repository.nightStudy

import com.b1nd.dodamdodam.nightstudy.domain.nightstudy.entity.NightStudyEntity
import com.b1nd.dodamdodam.nightstudy.domain.nightstudy.entity.QNightStudyEntity.nightStudyEntity
import com.b1nd.dodamdodam.nightstudy.domain.nightstudy.entity.QNightStudyMemberEntity.nightStudyMemberEntity
import com.b1nd.dodamdodam.nightstudy.domain.nightstudy.enumeration.NightStudyType
import com.querydsl.jpa.impl.JPAQueryFactory
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.data.support.PageableExecutionUtils
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

    override fun findAllByUserIdAndType(userId: UUID, type: NightStudyType, pageable: Pageable): Page<NightStudyEntity> {
        val content = queryFactory.select(nightStudyMemberEntity.nightStudy)
            .from(nightStudyMemberEntity)
            .where(
                nightStudyMemberEntity.userId.eq(userId),
                nightStudyMemberEntity.nightStudy.type.eq(type)
            )
            .orderBy(nightStudyMemberEntity.nightStudy.id.asc())
            .offset(pageable.offset)
            .limit(pageable.pageSize.toLong())
            .fetch()

        val countQuery = queryFactory
            .select(nightStudyMemberEntity.count())
            .from(nightStudyMemberEntity)
            .where(
                nightStudyMemberEntity.userId.eq(userId),
                nightStudyMemberEntity.nightStudy.type.eq(type)
            )

        return PageableExecutionUtils.getPage(content, pageable) { countQuery.fetchOne() ?: 0L }
    }

    override fun findAllByType(type: NightStudyType, pageable: Pageable): Page<NightStudyEntity> {
        val today = LocalDate.now()

        val content = queryFactory.selectFrom(nightStudyEntity)
            .where(
                nightStudyEntity.type.eq(type),
                nightStudyEntity.endAt.goe(today)
            )
            .offset(pageable.offset)
            .limit(pageable.pageSize.toLong())
            .fetch()

        val countQuery = queryFactory
            .select(nightStudyEntity.count())
            .from(nightStudyEntity)
            .where(
                nightStudyEntity.type.eq(type),
                nightStudyEntity.endAt.goe(today)
            )

        return PageableExecutionUtils.getPage(content, pageable) { countQuery.fetchOne() ?: 0L }
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

    override fun existsByRoomAndPeriodOverlap(
        roomId: Long,
        period: Int,
        startAt: LocalDate,
        endAt: LocalDate,
        excludeNightStudyId: Long
    ): Boolean {
        return queryFactory.selectOne()
            .from(nightStudyEntity)
            .where(
                nightStudyEntity.room.id.eq(roomId),
                nightStudyEntity.period.eq(period),
                nightStudyEntity.startAt.loe(endAt),
                nightStudyEntity.endAt.goe(startAt),
                nightStudyEntity.id.ne(excludeNightStudyId)
            )
            .fetchFirst() != null
    }
}