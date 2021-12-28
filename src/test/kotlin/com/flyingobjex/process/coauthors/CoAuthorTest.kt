package com.flyingobjex.process.coauthors

import com.flyingobjex.paperlyzer.util.CollectionUtils.Companion.withoutFirst
import io.kotest.matchers.shouldBe
import org.junit.Test

class CoAuthorTest {

    val mmff = "mm"
    val a = listOf(1, 2,3)
    val b = listOf(2,3)

    @Test
    fun `withoutFirst should return last element`(){
        val res = withoutFirst(b)

        res[0] shouldBe 3
    }

    @Test
    fun `should calculate gender ratio from MMFF`(){
        val res = withoutFirst(a)
        res[0] shouldBe 2
        res[1] shouldBe 3
    }
}
