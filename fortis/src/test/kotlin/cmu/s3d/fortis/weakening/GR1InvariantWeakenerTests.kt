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

            fact {
                root in G
                root.l in Imply
                
                all n: root.l.^(l+r) | n not in Imply + G
                all n: Neg & root.l.^(l+r) | n.l in Literal
                all n: Or & root.l.l.*(l+r) | no n.^(l+r) & And
                all n: And & root.l.r.*(l+r) | no n.^(l+r) & Or
            }

            fact {
                some antecedent: DAGNode {
                    antecedent in Xray
                    root.l.l in Xray or root.l.l in And and root.l.l.l in Xray
                    
                }
                
                some consequent: DAGNode {
                    consequent in InPlace
                    root.l.r in InPlace or root.l.r in Or and root.l.r.l in InPlace
                    
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
}