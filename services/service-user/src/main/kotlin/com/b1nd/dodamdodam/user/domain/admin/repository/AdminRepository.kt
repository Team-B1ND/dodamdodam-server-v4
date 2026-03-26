package com.b1nd.dodamdodam.user.domain.admin.repository

import com.b1nd.dodamdodam.user.domain.admin.entity.AdminEntity
import com.b1nd.dodamdodam.user.domain.user.entity.UserEntity
import org.springframework.data.jpa.repository.JpaRepository

interface AdminRepository : JpaRepository<AdminEntity, Long> {
    fun findByUser(user: UserEntity): AdminEntity?
    fun findAllByUserIn(users: Collection<UserEntity>): List<AdminEntity>
}
