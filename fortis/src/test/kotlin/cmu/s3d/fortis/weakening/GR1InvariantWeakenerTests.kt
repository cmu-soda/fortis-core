package cmu.s3d.fortis.weakening

import cmu.s3d.fortis.ts.lts.toFluent
import net.automatalib.word.Word
import org.junit.jupiter.api.Test
import kotlin.test.assertEquals

class GR1InvariantWeakenerTests {

    private fun loadTherac(): GR1InvariantWeakener {
        return GR1InvariantWeakener(
            invariant = SimpleGR1Invariant(
                antecedent = "Xray".parseCNF(),
                consequent = "InPlace".parseDNF()
            ),
            fluents = listOf(
                "fluent Xray = <set_xray, {set_ebeam, reset}>".toFluent()!!,
                "fluent EBeam = <set_ebeam, {set_xray, reset}>".toFluent()!!,
                "fluent InPlace = <x, e> initially 1".toFluent()!!,
                "fluent Fired = <{fire_xray, fire_ebeam}, reset>".toFluent()!!
            ),
            positiveExamples = listOf(
                Word.fromList("x,set_xray,up,e,set_ebeam,enter,b,fire_ebeam,reset".split(","))
            ),
            negativeExamples = listOf(
                Word.fromList("x,set_xray,up,e,enter,b,fire_xray,reset".split(","))
            ),
            maxNumOfNode = 7
        )
    }

    @Test
    fun testTherac() {
        val learner = loadTherac()
        val solution = learner.learn()
        assert(solution != null)
        assertEquals(
            "(G (Imply (And Xray Fired) InPlace))",
            solution!!.getLTL()
        )
    }
}