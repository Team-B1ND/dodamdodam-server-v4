package com.b1nd.dodamdodam.nightstudy.domain.nightstudy.repository.nightStudy

import com.b1nd.dodamdodam.nightstudy.domain.nightstudy.entity.NightStudyEntity
import com.b1nd.dodamdodam.nightstudy.domain.nightstudy.entity.QNightStudyEntity.nightStudyEntity
import com.b1nd.dodamdodam.nightstudy.domain.nightstudy.entity.QNightStudyMemberEntity.nightStudyMemberEntity
import com.b1nd.dodamdodam.nightstudy.domain.nightstudy.enumeration.NightStudyStatusType
import com.b1nd.dodamdodam.nightstudy.domain.nightstudy.enumeration.NightStudyType
import com.b1nd.dodamdodam.nightstudy.domain.room.entity.QRoomEntity
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

    override fun findAllByUserIdAndType(userId: UUID, type: NightStudyType): List<NightStudyEntity> {
        return queryFactory.select(nightStudyMemberEntity.nightStudy)
            .from(nightStudyMemberEntity)
            .where(
                nightStudyMemberEntity.userId.eq(userId),
                nightStudyMemberEntity.nightStudy.type.eq(type)
            )
            .orderBy(nightStudyMemberEntity.nightStudy.id.desc())
            .fetch()
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

    override fun existsByUserIdAndPeriodOverlap(userId: UUID, type: NightStudyType, period: Int, startAt: LocalDate, endAt: LocalDate): Boolean {
        return queryFactory.selectOne()
            .from(nightStudyMemberEntity)
            .join(nightStudyMemberEntity.nightStudy, nightStudyEntity)
            .where(
                nightStudyMemberEntity.userId.eq(userId),
                nightStudyEntity.startAt.loe(endAt),
                nightStudyEntity.endAt.goe(startAt),
                nightStudyEntity.status.ne(NightStudyStatusType.REJECTED),
                periodConflictCondition(type, period)
            )
            .fetchFirst() != null
    }

    /**
     * 신청하려는 심자의 점유 교시 집합과 기존 심자의 점유 교시 집합이 겹치는지 판단하는 조건
     *
     * 점유 교시:
     * - 개인 p=1  → {1}
     * - 개인 p=2  → {1, 2}
     * - 프로젝트 p=1 → {1}
     * - 프로젝트 p=2 → {2}
     */
    private fun periodConflictCondition(newType: NightStudyType, newPeriod: Int) = when {
        newType == NightStudyType.PERSONAL && newPeriod == 2 ->
            // {1,2} — 모든 기존 심자와 겹침 → 제한 없음
            null
        newType == NightStudyType.PROJECT && newPeriod == 2 ->
            // {2} — 기존 심자 중 2교시를 점유하는 것만 충돌 (개인 p=2, 프로젝트 p=2)
            nightStudyEntity.period.eq(2)
        else ->
            // PERSONAL p=1 또는 PROJECT p=1: {1} — 프로젝트 2교시만 허용
            nightStudyEntity.type.ne(NightStudyType.PROJECT)
                .or(nightStudyEntity.period.ne(2))
    }

    override fun existsByRoomAndPeriodOverlap(
        roomId: Long,
        period: Int,
        startAt: LocalDate,
        endAt: LocalDate,
        excludeNightStudyId: Long
    ): Boolean {
        val room = QRoomEntity("assignedRoom")
        return queryFactory.selectOne()
            .from(nightStudyEntity)
            .join(nightStudyEntity.room, room)
            .where(
                room.id.eq(roomId),
                nightStudyEntity.period.eq(period),
                nightStudyEntity.startAt.loe(endAt),
                nightStudyEntity.endAt.goe(startAt),
                nightStudyEntity.id.ne(excludeNightStudyId),
                nightStudyEntity.status.ne(NightStudyStatusType.REJECTED)
            )
            .fetchFirst() != null
    }
}