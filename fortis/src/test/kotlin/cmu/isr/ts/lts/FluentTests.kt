package cmu.isr.ts.lts

import net.automatalib.words.Word
import org.junit.jupiter.api.Test
import kotlin.test.assertEquals

class FluentTests {

    @Test
    fun testFluent1() {
        assertEquals(
            Fluent(
                "Xray",
                listOf("set_xray"),
                listOf("set_ebeam", "reset"),
                false
            ),
            "fluent Xray = <set_xray, {set_ebeam, reset}>".toFluent())
    }

    @Test
    fun testFluent2() {
        assertEquals(
            Fluent(
                "EBeam",
                listOf("set_ebeam"),
                listOf("set_xray", "reset"),
                false
            ),
            "fluent EBeam = <set_ebeam, {set_xray, reset}>".toFluent())
    }

    @Test
    fun testFluent3() {
        assertEquals(
            Fluent(
                "InPlace",
                listOf("x"),
                listOf("e"),
                true
            ),
            "fluent InPlace = <x, e> initially 1".toFluent())
    }

    @Test
    fun testFluent4() {
        assertEquals(
            Fluent(
                "Fired",
                listOf("fire_xray", "fire_ebeam"),
                listOf("reset"),
                false
            ),
            "fluent Fired = <{fire_xray, fire_ebeam}, reset>".toFluent())
    }

    @Test
    fun testEvaluateFluent1() {
        val fluents = listOf(
            "fluent Xray = <set_xray, {set_ebeam, reset}>".toFluent()!!,
            "fluent EBeam = <set_ebeam, {set_xray, reset}>".toFluent()!!,
            "fluent InPlace = <x, e> initially 1".toFluent()!!,
            "fluent Fired = <{fire_xray, fire_ebeam}, reset>".toFluent()!!,
        )
        val word = Word.fromSymbols("x", "set_xray", "up", "e")
        val evaluation = evaluateFluent(word, fluents)
        assertEquals(
            listOf(
                mapOf(
                    fluents[0] to false,
                    fluents[1] to false,
                    fluents[2] to true,
                    fluents[3] to false
                ),
                mapOf(
                    fluents[0] to false,
                    fluents[1] to false,
                    fluents[2] to true,
                    fluents[3] to false
                ),
                mapOf(
                    fluents[0] to true,
                    fluents[1] to false,
                    fluents[2] to true,
                    fluents[3] to false
                ),
                mapOf(
                    fluents[0] to true,
                    fluents[1] to false,
                    fluents[2] to true,
                    fluents[3] to false
                ),
                mapOf(
                    fluents[0] to true,
                    fluents[1] to false,
                    fluents[2] to false,
                    fluents[3] to false
                )
            ),
            evaluation
        )
    }
}