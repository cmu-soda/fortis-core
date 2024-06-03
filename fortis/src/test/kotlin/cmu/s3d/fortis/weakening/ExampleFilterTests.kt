package cmu.s3d.fortis.weakening

import cmu.s3d.fortis.ts.lts.ltsa.LTSACall
import cmu.s3d.fortis.ts.lts.ltsa.LTSACall.asLTS
import cmu.s3d.fortis.ts.lts.ltsa.LTSACall.compose
import cmu.s3d.fortis.ts.lts.toFluent
import cmu.s3d.fortis.ts.parallel
import net.automatalib.alphabet.Alphabets
import net.automatalib.word.Word
import org.junit.jupiter.api.Test
import kotlin.test.assertEquals

class ExampleFilterTests {
    @Test
    fun testTherac25() {
        val sys = LTSACall
            .compile(ClassLoader.getSystemResource("specs/therac25-2/sys.lts").readText())
            .compose()
            .asLTS()
        val env = LTSACall
            .compile(ClassLoader.getSystemResource("specs/therac25-2/env.lts").readText())
            .compose()
            .asLTS()
        val examples = TraceExampleGenerator(
            parallel(sys, env),
            Word.fromSymbols("x", "up", "e", "enter", "b"),
            Alphabets.fromArray("x", "up", "e", "enter", "b")
        ).withFilter(InvariantExampleFilter(
            listOf(
                "fluent Xray = <set_xray, {set_ebeam, reset}>".toFluent()!!,
                "fluent EBeam = <set_ebeam, {set_xray, reset}>".toFluent()!!,
                "fluent InPlace = <x, e> initially 1".toFluent()!!,
                "fluent Fired = <{fire_xray, fire_ebeam}, reset>".toFluent()!!,
            )
        ))
        val expected = setOf(
            Word.fromList("x,set_xray,up,e,set_ebeam,enter,b,fire_ebeam,reset".split(",")),
            Word.fromList("x,set_xray,up,e,enter,b,fire_xray,reset".split(",")),
        )
        assertEquals(expected, examples.toSet())
        // test re-iteration
        assertEquals(expected, examples.toSet())
    }

    @Test
    fun testTherac25_2() {
        val sys = LTSACall
            .compile(ClassLoader.getSystemResource("specs/therac25-2/sys.lts").readText())
            .compose()
            .asLTS()
        val env = LTSACall
            .compile(ClassLoader.getSystemResource("specs/therac25-2/env.lts").readText())
            .compose()
            .asLTS()
        val examples = TraceExampleGenerator(
            parallel(sys, env),
            Word.fromSymbols("x", "up", "e", "enter", "b"),
            Alphabets.fromArray("x", "up", "e", "enter", "b"),
            -1
        ).withFilter(InvariantExampleFilter(
            listOf(
                "fluent Xray = <set_xray, {set_ebeam, reset}>".toFluent()!!,
                "fluent EBeam = <set_ebeam, {set_xray, reset}>".toFluent()!!,
                "fluent InPlace = <x, e> initially 1".toFluent()!!,
                "fluent Fired = <{fire_xray, fire_ebeam}, reset>".toFluent()!!,
            )
        ))
        val expected = setOf(
            Word.fromList("x,set_xray,up,e,set_ebeam,enter,b,fire_ebeam,reset".split(",")),
            Word.fromList("x,set_xray,up,e,enter,b,fire_xray,reset".split(",")),
        )
        assertEquals(expected, examples.toSet())
        // test re-iteration
        assertEquals(expected, examples.toSet())
    }

    @Test
    fun testVoting() {
        val sys = LTSACall
            .compile(ClassLoader.getSystemResource("specs/voting-2/sys.lts").readText())
            .compose()
            .asLTS()
        val env = LTSACall
            .compile(ClassLoader.getSystemResource("specs/voting-2/env2.lts").readText())
            .compose()
            .asLTS()
        val examples = TraceExampleGenerator(
            parallel(sys, env),
            Word.fromSymbols("password", "select", "vote", "confirm"),
            Alphabets.fromArray("password", "select", "vote", "confirm")
        ).withFilter(InvariantExampleFilter(
            listOf(
                "fluent Confirmed = <confirm, password>".toFluent()!!,
                "fluent SelectByVoter = <v.select, {password, eo.select}>".toFluent()!!,
                "fluent VoteByVoter = <v.vote, {password, eo.vote}>".toFluent()!!,
            )
        ))
        assertEquals(
            setOf(
                Word.fromList(
                    "v.enter,password,v.password,v.exit,eo.enter,select,eo.select,eo.exit,v.enter,vote,v.vote,v.exit,eo.enter,confirm,eo.confirm,eo.exit,eo.enter".split(
                        ','
                    )
                ),
                Word.fromList(
                    "v.enter,password,v.password,v.exit,eo.enter,select,eo.select,vote,eo.vote,confirm,eo.confirm,eo.exit,eo.enter".split(
                        ','
                    )
                ),
                Word.fromList(
                    "v.enter,password,v.password,select,v.select,vote,v.vote,v.exit,eo.enter,confirm,eo.confirm,eo.exit,eo.enter".split(
                        ','
                    )
                ),
            ),
            examples.toSet()
        )
    }
}