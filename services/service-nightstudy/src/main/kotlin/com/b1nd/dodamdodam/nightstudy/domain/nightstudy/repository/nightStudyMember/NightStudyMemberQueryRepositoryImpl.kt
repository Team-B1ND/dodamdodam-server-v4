package com.b1nd.dodamdodam.nightstudy.domain.nightstudy.repository.nightStudyMember

import com.b1nd.dodamdodam.nightstudy.domain.nightstudy.entity.NightStudyEntity
import com.b1nd.dodamdodam.nightstudy.domain.nightstudy.entity.QNightStudyMemberEntity.nightStudyMemberEntity
import com.querydsl.jpa.impl.JPAQueryFactory
import org.springframework.stereotype.Repository
import java.util.UUID

@Repository
class NightStudyMemberQueryRepositoryImpl(
    private val queryFactory: JPAQueryFactory
) : NightStudyMemberQueryRepository {

    override fun findAllUserIdsByNightStudy(nightStudy: NightStudyEntity): List<UUID> {
        return queryFactory
            .select(nightStudyMemberEntity.userId)
            .from(nightStudyMemberEntity)
            .where(nightStudyMemberEntity.nightStudy.eq(nightStudy))
            .fetch()
    }

    override fun findLeaderUserIdByNightStudy(nightStudy: NightStudyEntity): UUID? {
        return queryFactory
            .select(nightStudyMemberEntity.userId)
            .from(nightStudyMemberEntity)
            .where(
                nightStudyMemberEntity.nightStudy.eq(nightStudy),
                nightStudyMemberEntity.isLeader.eq(true)
            )
            .fetchOne()
    }

    override fun findLeaderUserIdsByNightStudies(nightStudies: List<NightStudyEntity>): Map<Long, UUID> {
        if (nightStudies.isEmpty()) return emptyMap()

        return queryFactory
            .select(nightStudyMemberEntity.nightStudy.id, nightStudyMemberEntity.userId)
            .from(nightStudyMemberEntity)
            .where(
                nightStudyMemberEntity.nightStudy.`in`(nightStudies),
                nightStudyMemberEntity.isLeader.eq(true)
            )
            .fetch()
            .associate { tuple ->
                tuple.get(0, Long::class.java)!! to tuple.get(1, UUID::class.java)!!
            }
    }

    override fun findAllMemberUserIdsByNightStudies(nightStudies: List<NightStudyEntity>): Map<Long, List<UUID>> {
        if (nightStudies.isEmpty()) return emptyMap()

        return queryFactory
            .select(nightStudyMemberEntity.nightStudy.id, nightStudyMemberEntity.userId)
            .from(nightStudyMemberEntity)
            .where(nightStudyMemberEntity.nightStudy.`in`(nightStudies))
            .fetch()
            .groupBy(
                { tuple -> tuple.get(0, Long::class.java)!! },
                { tuple -> tuple.get(1, UUID::class.java)!! }
            )
    }
}