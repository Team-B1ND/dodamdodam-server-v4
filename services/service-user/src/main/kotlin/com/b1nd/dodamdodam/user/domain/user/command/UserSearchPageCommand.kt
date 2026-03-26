package com.b1nd.dodamdodam.user.domain.user.command

data class UserSearchPageCommand(
    val content: List<UserSearchCommand>,
    val hasNext: Boolean,
)