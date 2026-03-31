package com.b1nd.dodamdodam.inapp.infrastructure.redis.key

import com.b1nd.dodamdodam.core.redis.key.RedisKeyType

enum class TeamInviteRedisKey(
    override val prefix: String
) : RedisKeyType {
    INVITE("team-invite"),
}
