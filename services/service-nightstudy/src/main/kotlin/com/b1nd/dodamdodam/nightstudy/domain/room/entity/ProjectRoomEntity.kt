package com.b1nd.dodamdodam.nightstudy.domain.room.entity

import com.b1nd.dodamdodam.core.jpa.entity.BaseTimeEntity
import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.GeneratedValue
import jakarta.persistence.GenerationType
import jakarta.persistence.Id
import jakarta.persistence.Table

@Entity
@Table(name = "project_rooms")
class ProjectRoomEntity(
    @Column(length = 100, nullable = false, unique = true)
    var name: String,

    @Column(nullable = false)
    var floor: Int,
) : BaseTimeEntity() {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Long? = null

    fun update(name: String, floor: Int) {
        this.name = name
        this.floor = floor
    }
}
