package com.b1nd.dodamdodam.nightstudy.domain.nightstudy.repository

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
}