package com.b1nd.dodamdodam.core.common.uuid

import java.security.SecureRandom
import java.util.UUID

object UuidV7 {
    private const val VERSION: Long = 0x7
    private const val VARIANT: Long = 0x2
    private const val MAX_SEQUENCE: Int = 0xFFF
    private const val TIMESTAMP_MASK: Long = 0xFFFFFFFFFFFFL
    private const val RANDOM_BITS_MASK: Long = 0x3FFFFFFFFFFFFFFFL

    private val random = SecureRandom()

    private var lastTimestamp = -1L
    private var sequence = 0

    @Synchronized
    fun generate(): UUID {
        var timestamp = System.currentTimeMillis()

        if (timestamp > lastTimestamp) {
            lastTimestamp = timestamp
            sequence = random.nextInt(MAX_SEQUENCE + 1)
        } else {
            if (sequence == MAX_SEQUENCE) {
                do {
                    timestamp = System.currentTimeMillis()
                } while (timestamp <= lastTimestamp)
                lastTimestamp = timestamp
                sequence = random.nextInt(MAX_SEQUENCE + 1)
            } else {
                sequence += 1
            }
        }

        val mostSignificantBits =
            ((lastTimestamp and TIMESTAMP_MASK) shl 16) or
                (VERSION shl 12) or
                sequence.toLong()
        val leastSignificantBits =
            (VARIANT shl 62) or (random.nextLong() and RANDOM_BITS_MASK)

        return UUID(mostSignificantBits, leastSignificantBits)
    }
}
