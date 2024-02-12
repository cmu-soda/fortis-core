package cmu.s3d.fortis.weakening

import cmu.s3d.fortis.ts.lts.toFluent
import net.automatalib.word.Word
import org.junit.jupiter.api.Test
import kotlin.test.assertEquals

class GR1InvariantWeakenerTests {

    private fun loadTherac(): GR1InvariantWeakener {
        return GR1InvariantWeakener.build(
            invariant = listOf(
                SimpleGR1Invariant(
                    antecedent = "Xray".parseCNF(),
                    consequent = "InPlace".parseDNF()
                )
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

    private fun loadTherac2(): GR1InvariantWeakener {
        return GR1InvariantWeakener.build(
            invariant = listOf(
                SimpleGR1Invariant(
                    antecedent = "Xray && Fired".parseCNF(),
                    consequent = "InPlace".parseDNF()
                ),
                SimpleGR1Invariant(
                    antecedent = "EBeam".parseCNF(),
                    consequent = "!InPlace".parseDNF()
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
            ),
            maxNumOfNode = 12
        )
    }

    @Test
    fun testTherac() {
        val learner = loadTherac()
        assertEquals("""
            open util/ordering[SeqIdx]

            abstract sig DAGNode {
            	l: set DAGNode,
            	r: set DAGNode
            }
            fact {
            	all n: DAGNode | n not in n.^(l + r)
            }

            sig And, Or, Imply extends DAGNode {} {
            	one l
            	one r
            }

            sig Neg, G extends DAGNode {} {
                one l
                no r
            }
            abstract sig Literal extends DAGNode {} {
            	no l
            	no r
            }

            abstract sig SeqIdx {}
            abstract sig Trace {
            	lasso: SeqIdx -> SeqIdx,
            	valuation: DAGNode -> SeqIdx
            } {
            	// Negation
                all n: Neg, i: seqRange | n->i in valuation iff n.l->i not in valuation
            	// Or
                all n: Or, i: seqRange | n->i in valuation iff (n.l->i in valuation or n.r->i in valuation)
            	// And
                all n: And, i: seqRange | n->i in valuation iff (n.l->i in valuation and n.r->i in valuation)
            	// Imply
                all n: Imply, i: seqRange | n->i in valuation iff (n.l->i not in valuation or n.r->i in valuation)
            	// G
                all n: G, i: seqRange | n->i in valuation iff all i': futureIdx[i] | n.l->i' in valuation
                // X
                
            	// F
                
            	// Until
                
            }
            fun seqRange[t: Trace]: set SeqIdx {
            	let lastOfTrace = t.lasso.SeqIdx | lastOfTrace.prevs + lastOfTrace
            }
            fun futureIdx[t: Trace, i: SeqIdx]: set SeqIdx {
            	i.^((next :> seqRange[t]) + t.lasso) + i
            }

            abstract sig PositiveTrace, NegativeTrace extends Trace {}

            one sig Xray, EBeam, InPlace, Fired extends Literal {}
            one sig T0, T1, T2, T3, T4, T5, T6, T7, T8, T9 extends SeqIdx {}
            fact {
                first = T0
                next = T0->T1 + T1->T2 + T2->T3 + T3->T4 + T4->T5 + T5->T6 + T6->T7 + T7->T8 + T8->T9
            }

            one sig PT0 extends PositiveTrace {} {
                lasso = T9->T9
                InPlace->T0 + InPlace->T1 + Xray->T2 + InPlace->T2 + Xray->T3 + InPlace->T3 + Xray->T4 + EBeam->T5 + EBeam->T6 + EBeam->T7 + EBeam->T8 + Fired->T8 in valuation
                no (Xray->T0 + EBeam->T0 + Fired->T0 + Xray->T1 + EBeam->T1 + Fired->T1 + EBeam->T2 + Fired->T2 + EBeam->T3 + Fired->T3 + EBeam->T4 + InPlace->T4 + Fired->T4 + Xray->T5 + InPlace->T5 + Fired->T5 + Xray->T6 + InPlace->T6 + Fired->T6 + Xray->T7 + InPlace->T7 + Fired->T7 + Xray->T8 + InPlace->T8 + Xray->T9 + EBeam->T9 + InPlace->T9 + Fired->T9) & valuation
            }


            one sig NT0 extends NegativeTrace {} {
                lasso = T8->T8
                InPlace->T0 + InPlace->T1 + Xray->T2 + InPlace->T2 + Xray->T3 + InPlace->T3 + Xray->T4 + Xray->T5 + Xray->T6 + Xray->T7 + Fired->T7 in valuation
                no (Xray->T0 + EBeam->T0 + Fired->T0 + Xray->T1 + EBeam->T1 + Fired->T1 + EBeam->T2 + Fired->T2 + EBeam->T3 + Fired->T3 + EBeam->T4 + InPlace->T4 + Fired->T4 + EBeam->T5 + InPlace->T5 + Fired->T5 + EBeam->T6 + InPlace->T6 + Fired->T6 + EBeam->T7 + InPlace->T7 + Xray->T8 + EBeam->T8 + InPlace->T8 + Fired->T8) & valuation
            }

            one sig LearnedLTL {
                Root: DAGNode
            }
            fun root: one DAGNode { LearnedLTL.Root }

            fun childrenOf[n: DAGNode]: set DAGNode { n.^(l+r) }
            fun childrenAndSelfOf[n: DAGNode]: set DAGNode { n.*(l+r) }
            fun ancestorsOf[n: DAGNode]: set DAGNode { n.~^(l+r) }

            fact {
                // learn G(a -> b) && G(c -> d)
                root in G + And
                all n: G {
                    ancestorsOf[n] in And
                    n.l in Imply
                    all n': childrenOf[n.l] | n' not in Imply + G
                    all n': childrenOf[n.l] & Neg | n'.l in Literal
                    all n': childrenAndSelfOf[n.l.l] & Or | no childrenOf[n'] & And
                    all n': childrenAndSelfOf[n.l.r] & And | no childrenOf[n'] & Or
                }
            }

            fact {
                some rt: childrenAndSelfOf[root] & G {
                    some antecedent: DAGNode {
                        antecedent in Xray
                        rt.l.l in Xray or rt.l.l in And and rt.l.l.l in Xray
                        
                    }
                
                    some consequent: DAGNode {
                        consequent in InPlace
                        rt.l.r in InPlace or rt.l.r in Or and rt.l.r.l in InPlace
                        
                    }
                }
            }


            run {
                all t: PositiveTrace | root->T0 in t.valuation
                all t: NegativeTrace | root->T0 not in t.valuation
                minsome l + r
            } for %d DAGNode
        """.trimIndent(),
            learner.generateAlloyModel()
        )

        val solution = learner.learn()
        assert(solution != null)
        assertEquals(
            "(G (Imply (And Xray Fired) InPlace))",
            solution!!.getLTL()
        )
    }

    @Test
    fun testTherac2() {
        val learner = loadTherac2()
        val solution = learner.learn()
        assert(solution != null)
        assertEquals(
            "(And (G (Imply (And Xray Fired) InPlace)) (G (Imply (And EBeam Fired) (Neg InPlace))))",
            solution!!.getLTL()
        )
    }
}