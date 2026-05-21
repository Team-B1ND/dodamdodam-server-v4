package com.b1nd.dodamdodam.nightstudy.domain.nightstudy.repository.nightStudy

import com.b1nd.dodamdodam.nightstudy.domain.nightstudy.entity.NightStudyEntity
import com.b1nd.dodamdodam.nightstudy.domain.nightstudy.entity.QNightStudyEntity
import com.b1nd.dodamdodam.nightstudy.domain.nightstudy.entity.QNightStudyEntity.nightStudyEntity
import com.b1nd.dodamdodam.nightstudy.domain.nightstudy.entity.QNightStudyMemberEntity
import com.b1nd.dodamdodam.nightstudy.domain.nightstudy.entity.QNightStudyMemberEntity.nightStudyMemberEntity
import com.b1nd.dodamdodam.nightstudy.domain.nightstudy.enumeration.NightStudyStatusType
import com.b1nd.dodamdodam.nightstudy.domain.nightstudy.enumeration.NightStudyType
import com.b1nd.dodamdodam.nightstudy.domain.room.entity.QProjectRoomEntity
import com.querydsl.core.types.dsl.BooleanExpression
import com.querydsl.jpa.JPAExpressions
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
                nightStudyMemberEntity.nightStudy.type.eq(type),
                nightStudyMemberEntity.nightStudy.endAt.goe(LocalDate.now())
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

    override fun findAllByTypeAndStatus(type: NightStudyType, status: NightStudyStatusType?): List<NightStudyEntity> {
        val today = LocalDate.now()
        return queryFactory.selectFrom(nightStudyEntity)
            .where(
                nightStudyEntity.type.eq(type),
                nightStudyEntity.endAt.goe(today),
                status?.let { nightStudyEntity.status.eq(it) },
                hideByActiveProjectCondition(type)
            )
            .fetch()
    }

    override fun findAllByTypeAndUserIdsAndStatus(type: NightStudyType, userIds: List<UUID>, status: NightStudyStatusType?): List<NightStudyEntity> {
        val today = LocalDate.now()
        return queryFactory.select(nightStudyEntity)
            .from(nightStudyMemberEntity)
            .join(nightStudyMemberEntity.nightStudy, nightStudyEntity)
            .where(
                nightStudyEntity.type.eq(type),
                nightStudyEntity.endAt.goe(today),
                nightStudyMemberEntity.userId.`in`(userIds),
                status?.let { nightStudyEntity.status.eq(it) },
                hideByActiveProjectCondition(type)
            )
            .distinct()
            .fetch()
    }

    override fun countAllowedMembersGroupByTypeAndPeriod(): Map<Pair<NightStudyType, Int>, Int> {
        val today = LocalDate.now()
        val distinctUser = nightStudyMemberEntity.userId.countDistinct()

        return queryFactory
            .select(nightStudyEntity.type, nightStudyEntity.period, distinctUser)
            .from(nightStudyMemberEntity)
            .join(nightStudyMemberEntity.nightStudy, nightStudyEntity)
            .where(
                nightStudyEntity.status.eq(NightStudyStatusType.ALLOWED),
                nightStudyEntity.endAt.goe(today),
            )
            .groupBy(nightStudyEntity.type, nightStudyEntity.period)
            .fetch()
            .associate { tuple ->
                val type = tuple.get(nightStudyEntity.type)!!
                val period = tuple.get(nightStudyEntity.period) ?: 0
                (type to period) to (tuple.get(distinctUser)?.toInt() ?: 0)
            }
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

    override fun existsByUserIdAndPeriodOverlap(
        userId: UUID,
        period: Int,
        type: NightStudyType,
        startAt: LocalDate,
        endAt: LocalDate
    ): Boolean {
        return queryFactory.selectOne()
            .from(nightStudyMemberEntity)
            .join(nightStudyMemberEntity.nightStudy, nightStudyEntity)
            .where(
                nightStudyMemberEntity.userId.eq(userId),
                nightStudyEntity.period.eq(period),
                nightStudyEntity.type.eq(type),
                nightStudyEntity.startAt.loe(endAt),
                nightStudyEntity.endAt.goe(startAt),
                nightStudyEntity.status.ne(NightStudyStatusType.REJECTED)
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
        val room = QProjectRoomEntity("assignedRoom")
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

    override fun findActivePersonalsByUserIdsAndPeriodOverlap(
        userIds: List<UUID>,
        period: Int,
        startAt: LocalDate,
        endAt: LocalDate
    ): List<NightStudyEntity> {
        if (userIds.isEmpty()) return emptyList()

        return queryFactory.select(nightStudyEntity)
            .from(nightStudyMemberEntity)
            .join(nightStudyMemberEntity.nightStudy, nightStudyEntity)
            .where(
                nightStudyEntity.type.eq(NightStudyType.PERSONAL),
                nightStudyEntity.status.`in`(NightStudyStatusType.PENDING, NightStudyStatusType.ALLOWED),
                nightStudyEntity.period.eq(period),
                nightStudyEntity.startAt.loe(endAt),
                nightStudyEntity.endAt.goe(startAt),
                nightStudyMemberEntity.userId.`in`(userIds)
            )
            .distinct()
            .fetch()
    }

    private fun hideByActiveProjectCondition(type: NightStudyType): BooleanExpression? {
        if (type != NightStudyType.PERSONAL) return null

        val project = QNightStudyEntity("project")
        val projectMember = QNightStudyMemberEntity("projectMember")
        val personalMember = QNightStudyMemberEntity("personalMember")

        return JPAExpressions
            .selectOne()
            .from(project)
            .join(projectMember).on(projectMember.nightStudy.eq(project))
            .join(personalMember).on(personalMember.nightStudy.eq(nightStudyEntity))
            .where(
                project.type.eq(NightStudyType.PROJECT),
                project.status.`in`(NightStudyStatusType.PENDING, NightStudyStatusType.ALLOWED),
                project.period.eq(nightStudyEntity.period),
                project.startAt.loe(nightStudyEntity.endAt),
                project.endAt.goe(nightStudyEntity.startAt),
                projectMember.userId.eq(personalMember.userId)
            )
            .notExists()
    }
}