package cmu.s3d.fortis.ts.lts

import cmu.s3d.ltl.State
import net.automatalib.word.Word
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
            "fluent Xray = <set_xray, {set_ebeam, reset}>".toFluent()
        )
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
            "fluent EBeam = <set_ebeam, {set_xray, reset}>".toFluent()
        )
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
            "fluent InPlace = <x, e> initially 1".toFluent()
        )
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
            "fluent Fired = <{fire_xray, fire_ebeam}, reset>".toFluent()
        )
    }

    @Test
    fun testFluent5() {
        assertEquals(
            Fluent(
                "Confirmed",
                listOf("v.confirm", "eo.confirm"),
                listOf("v.password", "eo.password"),
                false
            ),
            "fluent Confirmed = <{v, eo}.confirm, {{v, eo}.password}>".toFluent()
        )
    }

    @Test
    fun testFluent6() {
        assertEquals(
            Fluent(
                "SelectByVoter",
                listOf("v.select"),
                listOf("v.password", "eo.password", "eo.select"),
                true
            ),
            "fluent SelectByVoter = <v.select, {{v, eo}.password, eo.select}> initially 1".toFluent()
        )
    }

    @Test
    fun testFluent7() {
        assertEquals(
            Fluent(
                "A",
                listOf("a.b.c"),
                listOf("a.c.d", "b.c.d", "f"),
                false
            ),
            "fluent A = <{a.b.c}, {{a, b}.c.d, f}>".toFluent()
        )
    }

    @Test
    fun testFluent8() {
        assertEquals(
            Fluent(
                "A",
                listOf("a.1.b"),
                listOf("a.12.c"),
                false
            ),
            "fluent A = <a[1].b, a[12].c>".toFluent()
        )
    }

    @Test
    fun testFluent9() {
        assertEquals(
            Fluent(
                "A",
                listOf("a.1.2.b"),
                listOf("a.12.34.c"),
                false
            ),
            "fluent A = <a[1][2].b, a[12][34].c>".toFluent()
        )
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
//                State(mapOf(
//                    fluents[0].name to false,
//                    fluents[1].name to false,
//                    fluents[2].name to true,
//                    fluents[3].name to false
//                )),
                State(mapOf(
                    fluents[0].name to false,
                    fluents[1].name to false,
                    fluents[2].name to true,
                    fluents[3].name to false
                )),
                State(mapOf(
                    fluents[0].name to true,
                    fluents[1].name to false,
                    fluents[2].name to true,
                    fluents[3].name to false
                )),
                State(mapOf(
                    fluents[0].name to true,
                    fluents[1].name to false,
                    fluents[2].name to true,
                    fluents[3].name to false
                )),
                State(mapOf(
                    fluents[0].name to true,
                    fluents[1].name to false,
                    fluents[2].name to false,
                    fluents[3].name to false
                ))
            ),
            evaluation
        )
    }
}