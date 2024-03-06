package cmu.s3d.fortis.service.impl

import cmu.s3d.fortis.common.Spec
import cmu.s3d.fortis.common.SpecType
import net.automatalib.word.Word
import org.junit.jupiter.api.Test
import kotlin.test.assertEquals

class WeakeningServiceImplTests {
    private val service = WeakeningServiceImpl()

    @Test
    fun testGenerateExamplesFromTraceForTherac25() {
        val examples = service.generateExamplesFromTrace(
            listOf(Spec(SpecType.FSP, ClassLoader.getSystemResource("specs/therac25-2/sys.lts").readText())),
            listOf(Spec(SpecType.FSP, ClassLoader.getSystemResource("specs/therac25-2/env.lts").readText())),
            Word.fromSymbols("x", "up", "e", "enter", "b"),
            listOf("x", "up", "e", "enter", "b"),
            listOf(
                "fluent Xray = <set_xray, {set_ebeam, reset}>",
                "fluent EBeam = <set_ebeam, {set_xray, reset}>",
                "fluent InPlace = <x, e> initially 1",
                "fluent Fired = <{fire_xray, fire_ebeam}, reset>",
            )
        )
        assertEquals(
            setOf(
                Word.fromList("x,set_xray,up,e,set_ebeam,enter,b,fire_ebeam,reset".split(",")),
                Word.fromList("x,set_xray,up,e,enter,b,fire_xray,reset".split(",")),
            ),
            examples.toSet()
        )
    }

    @Test
    fun testGenerateExamplesFromTraceForVoting() {
        val examples = service.generateExamplesFromTrace(
            listOf(Spec(SpecType.FSP, ClassLoader.getSystemResource("specs/voting-2/sys.lts").readText())),
            listOf(Spec(SpecType.FSP, ClassLoader.getSystemResource("specs/voting-2/env2.lts").readText())),
            Word.fromSymbols("password", "select", "vote", "confirm"),
            listOf("password", "select", "vote", "confirm"),
            listOf(
                "fluent Confirmed = <confirm, password>",
                "fluent SelectByVoter = <v.select, {password, eo.select}>",
                "fluent VoteByVoter = <v.vote, {password, eo.vote}>",
            )
        )
        assertEquals(
            setOf(
                Word.fromList(
                    "v.enter,password,v.password,v.exit,eo.enter,select,eo.select,eo.exit,v.enter,vote,v.vote,v.exit,eo.enter,confirm,eo.confirm,eo.exit,eo.enter".split(
                        ','
                    )
                ),
                Word.fromList(
                    "v.enter,password,v.password,v.exit,eo.enter,select,eo.select,vote,eo.vote,eo.exit,v.enter,confirm,v.confirm,v.exit,v.enter".split(
                        ','
                    )
                ),
                Word.fromList(
                    "v.enter,password,v.password,select,v.select,v.exit,eo.enter,vote,eo.vote,eo.exit,v.enter,confirm,v.confirm,v.exit,v.enter".split(
                        ','
                    )
                ),
            ),
            examples.toSet()
        )
    }

    @Test
    fun testWeakenSafetyInvariantForTherac25() {
        val solutions = service.weakenSafetyInvariant(
            "[](Xray -> InPlace)",
            listOf(
                "fluent Xray = <set_xray, {set_ebeam, reset}>",
                "fluent EBeam = <set_ebeam, {set_xray, reset}>",
                "fluent InPlace = <x, e> initially 1",
                "fluent Fired = <{fire_xray, fire_ebeam}, reset>",
            ),
            listOf(
                Word.fromList("x,set_xray,up,e,set_ebeam,enter,b,fire_ebeam,reset".split(",")),
            ),
            listOf(
                Word.fromList("x,set_xray,up,e,enter,b,fire_xray,reset".split(",")),
            )
        )
        assertEquals(
            listOf(
                "[](Xray && Fired -> InPlace)",
                "[](Xray && !EBeam && Fired -> InPlace)",
            ),
            solutions
        )
    }

    @Test
    fun testWeakenSafetyInvariantForTherac25_2() {
        val solutions = service.weakenSafetyInvariant(
            "[](Xray && Fired -> InPlace) && [](EBeam && Fired -> !InPlace)",
            listOf(
                "fluent Xray = <set_xray, {set_ebeam, reset}>",
                "fluent EBeam = <set_ebeam, {set_xray, reset}>",
                "fluent InPlace = <x, e> initially 1",
                "fluent Fired = <{fire_xray, fire_ebeam}, reset>",
            ),
            listOf(
                Word.fromList("x,set_xray,up,e,set_ebeam,enter,b,fire_ebeam,reset".split(",")),
                Word.fromList("e,set_ebeam,up,x,set_xray,enter,b,fire_xray,reset".split(",")),
                Word.fromList("e,set_ebeam,up,x,enter,b,fire_ebeam,reset".split(","))
            ),
            listOf(
                Word.fromList("x,set_xray,up,e,enter,b,fire_xray,reset".split(",")),
            )
        )
        assertEquals(
            listOf(
                "[](Xray && Fired -> InPlace) && [](Xray && EBeam && Fired -> !InPlace)",
                "[](Xray && Fired -> InPlace) && [](false && EBeam && Fired -> !InPlace)",
                "[](Xray && !EBeam && Fired -> InPlace) && [](Xray && EBeam && Fired -> !InPlace)",
                "[](Xray && !EBeam && Fired -> InPlace) && [](false && EBeam && Fired -> !InPlace)"
            ),
            solutions
        )
    }

    @Test
    fun testWeakenSafetyInvariantForTherac25_3() {
        val solutions = service.weakenSafetyInvariant(
            "[](Xray && Fired -> InPlace) && [](EBeam && Fired -> !InPlace)",
            listOf(
                "fluent Xray = <set_xray, {set_ebeam, reset}>",
                "fluent EBeam = <set_ebeam, {set_xray, reset}>",
                "fluent InPlace = <x, e> initially 1",
                "fluent Fired = <{fire_xray, fire_ebeam}, reset>",
            ),
            listOf(
                Word.fromList("e,set_ebeam,up,x,set_xray,enter,b,fire_xray,reset".split(",")),
                Word.fromList("e,set_ebeam,up,x,enter,b,fire_ebeam,reset".split(","))
            ),
            emptyList()
        )
        assertEquals(
            listOf(
                "[](Xray && Fired -> InPlace) && [](Xray && EBeam && Fired -> !InPlace)",
                "[](Xray && Fired -> InPlace) && [](false && EBeam && Fired -> !InPlace)",
                "[](Xray && !EBeam && Fired -> InPlace) && [](Xray && EBeam && Fired -> !InPlace)",
                "[](Xray && !EBeam && Fired -> InPlace) && [](false && EBeam && Fired -> !InPlace)",
                "[](Xray && EBeam && Fired -> InPlace) && [](false && EBeam && Fired -> !InPlace)",
                "[](Xray && EBeam && Fired -> InPlace) && [](Xray && EBeam && Fired -> !InPlace)",
                "[](false && Xray && Fired -> InPlace) && [](Xray && EBeam && Fired -> !InPlace)",
                "[](false && Xray && Fired -> InPlace) && [](false && EBeam && Fired -> !InPlace)",
            ),
            solutions
        )
    }

    @Test
    fun testWeakenGR1ForTherac25() {
        val solutions = service.weakenGR1Invariant(
            "[](Xray -> InPlace)",
            listOf(
                "fluent Xray = <set_xray, {set_ebeam, reset}>",
                "fluent EBeam = <set_ebeam, {set_xray, reset}>",
                "fluent InPlace = <x, e> initially 1",
                "fluent Fired = <{fire_xray, fire_ebeam}, reset>",
            ),
            listOf(
                Word.fromList("x,set_xray,up,e,set_ebeam,enter,b,fire_ebeam,reset".split(",")),
            ),
            listOf(
                Word.fromList("x,set_xray,up,e,enter,b,fire_xray,reset".split(",")),
            ),
            3
        )
        assertEquals(
            "[]((Xray && Fired) -> InPlace)",
            solutions
        )
    }

    @Test
    fun testWeakenGR1ForTherac25_2() {
        val solutions = service.weakenGR1Invariant(
            "[](Xray && Fired -> InPlace) && [](EBeam -> !InPlace)",
            listOf(
                "fluent Xray = <set_xray, {set_ebeam, reset}>",
                "fluent EBeam = <set_ebeam, {set_xray, reset}>",
                "fluent InPlace = <x, e> initially 1",
                "fluent Fired = <{fire_xray, fire_ebeam}, reset>",
            ),
            listOf(
                Word.fromList("e,set_ebeam,up,x,set_xray,enter,b,fire_xray,reset".split(","))
            ),
            listOf(
                Word.fromList("e,set_ebeam,up,x,enter,b,fire_ebeam,reset".split(","))
            ),
            8
        )
        assertEquals(
            "([]((Fired && Xray) -> InPlace) && []((EBeam && Fired) -> !InPlace))",
            solutions
        )
    }

    @Test
    fun testWeakenSafetyInvariantForVoting() {
        val solutions = service.weakenSafetyInvariant(
            "[](Confirmed -> SelectByVoter && VoteByVoter)",
            listOf(
                "fluent Confirmed = <confirm, password>",
                "fluent SelectByVoter = <v.select, {password, eo.select}>",
                "fluent VoteByVoter = <v.vote, {password, eo.vote}>",
            ),
            listOf(
                Word.fromList("v.enter,password,v.password,select,v.select,v.exit,eo.enter,vote,eo.vote,confirm,eo.confirm".split(","))
            ),
            listOf(
                Word.fromList("v.enter,password,v.password,v.exit,eo.enter,select,eo.select,vote,eo.vote,confirm,eo.confirm".split(",")),
            )
        )
        assertEquals(
            listOf(
                "[](Confirmed -> SelectByVoter)"
            ),
            solutions
        )
    }
}