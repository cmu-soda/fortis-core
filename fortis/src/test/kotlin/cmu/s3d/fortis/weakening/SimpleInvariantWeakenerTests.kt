package cmu.s3d.fortis.weakening

import cmu.s3d.fortis.ts.lts.toFluent
import net.automatalib.word.Word
import org.junit.jupiter.api.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull

class SimpleInvariantWeakenerTests {

    private fun loadTherac(): SimpleInvariantWeakener {
        return SimpleInvariantWeakener(
            invariant = listOf(
                SimpleInvariant(
                    antecedent = "Xray".parseConjunction(),
                    consequent = "InPlace".parseConjunction()
                )
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
    }

    private fun loadTherac2(): SimpleInvariantWeakener {
        return SimpleInvariantWeakener(
            invariant = listOf(
                SimpleInvariant(
                    antecedent = "Xray && Fired".parseConjunction(),
                    consequent = "InPlace".parseConjunction()
                ),
                SimpleInvariant(
                    antecedent = "EBeam".parseConjunction(),
                    consequent = "!InPlace".parseConjunction()
                )
            ),
            fluents = listOf(
                "fluent Xray = <set_xray, {set_ebeam, reset}>".toFluent()!!,
                "fluent EBeam = <set_ebeam, {set_xray, reset}>".toFluent()!!,
                "fluent InPlace = <x, e> initially 1".toFluent()!!,
                "fluent Fired = <{fire_xray, fire_ebeam}, reset>".toFluent()!!
            ),
            positiveExamples = listOf(
                Word.fromList("e,set_ebeam,up,x,set_xray,enter,b,fire_xray,reset".split(",")),
            ),
            negativeExamples = listOf(
                Word.fromList("e,set_ebeam,up,x,enter,b,fire_ebeam,reset".split(","))
            )
        )
    }

    private fun loadVoting(): SimpleInvariantWeakener {
        return SimpleInvariantWeakener(
            invariant = listOf(
                SimpleInvariant(
                    antecedent = "Confirmed".parseConjunction(),
                    consequent = "SelectByVoter && VoteByVoter".parseConjunction()
                )
            ),
            fluents = listOf(
                "fluent Confirmed = <confirm, password>".toFluent()!!,
                "fluent SelectByVoter = <v.select, {password, eo.select}>".toFluent()!!,
                "fluent VoteByVoter = <v.vote, {password, eo.vote}>".toFluent()!!,
            ),
            positiveExamples = listOf(
                Word.fromList(
                    "v.enter,password,v.password,select,v.select,v.exit,eo.enter,vote,eo.vote,confirm,eo.confirm".split(
                        ","
                    )
                ),
            ),
            negativeExamples = listOf(
                Word.fromList(
                    "v.enter,password,v.password,v.exit,eo.enter,select,eo.select,vote,eo.vote,confirm,eo.confirm".split(
                        ","
                    )
                ),
            )
        )
    }

    @Test
    fun testGenerateAlloyTherac() {
        val invWeakener = loadTherac()
        assertEquals(
            """
            abstract sig Bool {}
            one sig True, False extends Bool {}
            abstract sig Literal {}
            one sig false, Xray, EBeam, InPlace, Fired extends Literal {}
            abstract sig Invariant {
                antecedent: Literal -> lone Bool,
                consequent: Literal -> lone Bool
            } {
                consequent not in antecedent
                all l: Literal | (l -> True in antecedent implies l -> False not in consequent) and
		            (l -> False in antecedent implies l -> True not in consequent)
                false->False not in antecedent
            }
            
            one sig Invariant0 extends Invariant {} {
                Xray->True in antecedent
                false->True in antecedent implies antecedent in (false->True + Xray->True)
                consequent in (InPlace->True)
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
                all t: PositiveTrace | all inv: Invariant | G[t, inv]
                all t: NegativeTrace | some inv: Invariant | not G[t, inv]
                all inv: Invariant | minsome[2] inv.antecedent and maxsome[1] inv.consequent
            }
            """.trimIndent(),
            invWeakener.generateAlloyModel()
        )
    }

    @Test
    fun testLearnTherac() {
        val invWeakener = loadTherac()
        val solutions = mutableListOf<List<SimpleInvariant>>()
        var solution = invWeakener.learn()
        while (solution != null) {
            solutions.add(solution.getInvariant())
            solution = solution.next()
        }
        assertEquals(2, solutions.size)
        assertEquals(
            listOf(
                listOf(
                    SimpleInvariant(
                        antecedent = "Xray && Fired".parseConjunction(),
                        consequent = "InPlace".parseConjunction()
                    )
                ),
                listOf(
                    SimpleInvariant(
                        antecedent = "Xray && !EBeam && Fired".parseConjunction(),
                        consequent = "InPlace".parseConjunction()
                    )
                )
            ),
            solutions
        )
    }

    @Test
    fun testLearnTherac2() {
        val invWeakener = loadTherac2()
        var solution = invWeakener.learn()
        assertNotNull(solution)
        assertEquals(
            listOf(
                SimpleInvariant(
                    antecedent = "Xray && Fired".parseConjunction(),
                    consequent = "InPlace".parseConjunction()
                ),
                SimpleInvariant(
                    antecedent = "EBeam && Fired".parseConjunction(),
                    consequent = "!InPlace".parseConjunction()
                )
            ),
            solution.getInvariant()
        )
    }

    @Test
    fun testLearnVoting() {
        val invWeakener = loadVoting()
        val solutions = mutableListOf<List<SimpleInvariant>>()
        var solution = invWeakener.learn()
        while (solution != null) {
            solutions.add(solution.getInvariant())
            solution = solution.next()
        }
        assertEquals(
            listOf(
                listOf(
                    SimpleInvariant(
                        antecedent = "Confirmed".parseConjunction(),
                        consequent = "SelectByVoter".parseConjunction()
                    )
                )
            ),
            solutions
        )
    }
}