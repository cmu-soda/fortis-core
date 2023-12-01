package cmu.isr.weakening

import cmu.isr.ts.lts.toFluent
import net.automatalib.words.Word
import org.junit.jupiter.api.Test
import kotlin.test.assertEquals

class InvariantWeakenerTests {
    @Test
    fun testTherac() {
        val invWeakener = InvariantWeakener(
            invariant = Invariant(
                antecedent = "Xray".parseConjunction(),
                consequent = "InPlace".parseConjunction()
            ),
            fluents = listOf(
                "fluent Xray = <set_xray, {set_ebeam, reset}>".toFluent()!!,
                "fluent EBeam = <set_ebeam, {set_xray, reset}>".toFluent()!!,
                "fluent InPlace = <x, e> initially 1".toFluent()!!,
                "fluent Fired = <{fire_xray, fire_ebeam}, reset>".toFluent()!!
            ),
            positiveExamples = listOf(
                Word.fromList("x,set_xray,up,e,set_ebeam,enter,b,fire_ebeam,reset".split(",")),
            ),
            negativeExamples = listOf(
                Word.fromList("x,set_xray,up,e,enter,b,fire_xray,reset".split(",")),
            )
        )
        assertEquals("""
            abstract sig Bool {}
            one sig True, False extends Bool {}
            abstract sig Literal {}
            one sig Xray, EBeam, InPlace, Fired extends Literal {}
            one sig Invariant {
                antecedent: Literal -> lone Bool,
                consequent: Literal -> lone Bool
            } {
                consequent not in antecedent
                Xray->True in antecedent
                consequent.Bool in (InPlace)
            }

            abstract sig State {
            	trueValues: set Literal
            }
            abstract sig Trace {
            	states: set State
            }
            abstract sig PositiveTrace, NegativeTrace extends Trace {}

            one sig S0 extends State {} { trueValues = InPlace }
            one sig S1 extends State {} { trueValues = Xray + InPlace }
            one sig S2 extends State {} { trueValues = Xray }
            one sig S3 extends State {} { trueValues = EBeam }
            one sig S4 extends State {} { trueValues = EBeam + Fired }
            one sig S5 extends State {} { trueValues = none }
            one sig S6 extends State {} { trueValues = Xray + Fired }

            one sig PT0 extends PositiveTrace {} { states = S0 + S1 + S2 + S3 + S4 + S5 }

            one sig NT0 extends NegativeTrace {} { states = S0 + S1 + S2 + S6 + S5 }

            pred satisfy[s: State, inv: Invariant] {
            	let trues = s.trueValues, falses = Literal - s.trueValues |
            		inv.antecedent.True in trues and inv.antecedent.False in falses implies
            			inv.consequent.True in trues and inv.consequent.False in falses
            }

            pred G[t: Trace, inv: Invariant] {
            	all s: t.states | satisfy[s, inv]
            }

            run {
            	all inv: Invariant, t: PositiveTrace | G[t, inv]
            	all inv: Invariant, t: NegativeTrace | not G[t, inv]
            	minsome[2] Invariant.antecedent
            	maxsome[1] Invariant.consequent
            }
            """.trimIndent(),
            invWeakener.generateAlloyModel()
        )
    }
}