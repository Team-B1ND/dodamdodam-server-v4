package com.b1nd.dodamdodam.user.domain.user.repository

import com.b1nd.dodamdodam.core.security.passport.enumerations.RoleType
import com.b1nd.dodamdodam.user.domain.admin.entity.QAdminEntity.adminEntity
import com.b1nd.dodamdodam.user.domain.student.entity.QStudentEntity.studentEntity
import com.b1nd.dodamdodam.user.domain.teacher.entity.QTeacherEntity.teacherEntity
import com.b1nd.dodamdodam.user.domain.user.command.UserSearchCommand
import com.b1nd.dodamdodam.user.domain.user.entity.QUserEntity.userEntity
import com.b1nd.dodamdodam.user.domain.user.entity.QUserRoleEntity.userRoleEntity
import com.querydsl.core.BooleanBuilder
import com.querydsl.core.Tuple
import com.querydsl.jpa.JPAExpressions
import com.querydsl.jpa.impl.JPAQueryFactory
import org.springframework.data.domain.Pageable
import org.springframework.stereotype.Repository

@Repository
class UserQueryRepositoryImpl(
    private val queryFactory: JPAQueryFactory
) : UserQueryRepository {

    override fun searchUsers(keyword: String?, roles: List<RoleType>?, generationOnly: Boolean?, pageable: Pageable): List<UserSearchCommand> {
        val condition = buildCondition(keyword, roles, generationOnly)

        val tuples = queryFactory
            .select(userEntity, studentEntity, teacherEntity, adminEntity)
            .from(userEntity)
            .leftJoin(studentEntity).on(studentEntity.user.eq(userEntity))
            .leftJoin(teacherEntity).on(teacherEntity.user.eq(userEntity))
            .leftJoin(adminEntity).on(adminEntity.user.eq(userEntity))
            .where(condition)
            .orderBy(userEntity.name.asc())
            .offset(pageable.offset)
            .limit(pageable.pageSize.toLong() + 1)
            .fetch()

        if (tuples.isEmpty()) return emptyList()

        val rolesMap = fetchRolesMap(tuples)

        return tuples.map { it.toCommand(rolesMap) }
    }

    private fun buildCondition(keyword: String?, roles: List<RoleType>?, generationOnly: Boolean?) = BooleanBuilder().apply {
        keyword?.takeIf { it.isNotBlank() }?.let { and(userEntity.name.containsIgnoreCase(it)) }
        roles?.takeIf { it.isNotEmpty() }?.let {
            and(JPAExpressions.selectOne().from(userRoleEntity)
                .where(userRoleEntity.user.eq(userEntity), userRoleEntity.role.`in`(it)).exists())
        }
        generationOnly?.takeIf { it }?.let { and(studentEntity.isGraduated.isFalse) }
    }

    private fun fetchRolesMap(tuples: List<Tuple>): Map<Long, Set<RoleType>> {
        val userIds = tuples.map { it.get(userEntity)!!.id!! }
        return queryFactory
            .select(userRoleEntity.user.id, userRoleEntity.role)
            .from(userRoleEntity)
            .where(userRoleEntity.user.id.`in`(userIds))
            .fetch()
            .groupBy({ it.get(userRoleEntity.user.id)!! }, { it.get(userRoleEntity.role)!! })
            .mapValues { (_, v) -> v.toSet() }
    }

    private fun Tuple.toCommand(rolesMap: Map<Long, Set<RoleType>>): UserSearchCommand {
        val user = get(userEntity)!!
        return UserSearchCommand(user, rolesMap[user.id] ?: emptySet(), get(studentEntity), get(teacherEntity), get(adminEntity))
    }
}
