package com.b1nd.dodamdodam.nightstudy.application.team.data.response

import com.b1nd.dodamdodam.grpc.user.UserResponse
import com.b1nd.dodamdodam.nightstudy.domain.team.NightStudyTeamMemberEntity
import java.util.UUID

data class GetTeamMembersResponse(
    val userId: UUID,
    val name: String,
    val profileImage: String?,
    val isOwner: Boolean,
    val isAccept: Boolean,
    val student: StudentInfo
) {
    companion object {
        fun fromList(
            members: List<NightStudyTeamMemberEntity>,
            usersMap: Map<String, UserResponse>
        ): List<GetTeamMembersResponse> = members.mapNotNull { member ->
            usersMap[member.user.toString()]?.let { user ->
                GetTeamMembersResponse(
                    userId = member.user,
                    name = user.name,
                    profileImage = user.profileImage,
                    isOwner = member.isOwner,
                    isAccept = member.isAccept,
                    StudentInfo(
                        user.student.grade,
                        user.student.room,
                        user.student.number,
                    )
                )
            }
        }
    }
}

data class StudentInfo(
    val grade: Int,
    val room: Int,
    val number: Int
)