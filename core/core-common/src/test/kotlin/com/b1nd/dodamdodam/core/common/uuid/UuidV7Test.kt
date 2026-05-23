package com.b1nd.dodamdodam.core.common.uuid

import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test

class UuidV7Test {
    @Test
    fun `generate uuid v7`() {
        val uuid = UuidV7.generate()

        assertThat(uuid.version()).isEqualTo(7)
        assertThat(uuid.variant()).isEqualTo(2)
    }

    @Test
    fun `generate time ordered uuid`() {
        val uuids = List(1_000) { UuidV7.generate().toString() }

        assertThat(uuids).isEqualTo(uuids.sorted())
    }
}
