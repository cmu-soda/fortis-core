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
            abstract sig PositiveTrace extends Trace {}
            abstract sig NegativeTrace extends Trace {}
            one sig Xray, EBeam, InPlace, Fired extends Literal {}
            one sig T0, T1, T2, T3, T4, T5, T6, T7, T8 extends SeqIdx {}
            fact {
                first = T0
                next = T0->T1 + T1->T2 + T2->T3 + T3->T4 + T4->T5 + T5->T6 + T6->T7 + T7->T8
            }
            
            one sig PT0 extends PositiveTrace {} {
                lasso = T8->T8
                InPlace->T0 + Xray->T1 + InPlace->T1 + Xray->T2 + InPlace->T2 + Xray->T3 + EBeam->T4 + EBeam->T5 + EBeam->T6 + EBeam->T7 + Fired->T7 in valuation
                no (Xray->T0 + EBeam->T0 + Fired->T0 + EBeam->T1 + Fired->T1 + EBeam->T2 + Fired->T2 + EBeam->T3 + InPlace->T3 + Fired->T3 + Xray->T4 + InPlace->T4 + Fired->T4 + Xray->T5 + InPlace->T5 + Fired->T5 + Xray->T6 + InPlace->T6 + Fired->T6 + Xray->T7 + InPlace->T7 + Xray->T8 + EBeam->T8 + InPlace->T8 + Fired->T8) & valuation
            }
            
            
            one sig NT0 extends NegativeTrace {} {
                lasso = T7->T7
                InPlace->T0 + Xray->T1 + InPlace->T1 + Xray->T2 + InPlace->T2 + Xray->T3 + Xray->T4 + Xray->T5 + Xray->T6 + Fired->T6 in valuation
                no (Xray->T0 + EBeam->T0 + Fired->T0 + EBeam->T1 + Fired->T1 + EBeam->T2 + Fired->T2 + EBeam->T3 + InPlace->T3 + Fired->T3 + EBeam->T4 + InPlace->T4 + Fired->T4 + EBeam->T5 + InPlace->T5 + Fired->T5 + EBeam->T6 + InPlace->T6 + Xray->T7 + EBeam->T7 + InPlace->T7 + Fired->T7) & valuation
            }

            one sig LearnedLTL {
                Root: DAGNode
            }
            fun root: one DAGNode { LearnedLTL.Root }
            fun childrenOf[n: DAGNode]: set DAGNode { n.^(l+r) }
            fun childrenAndSelfOf[n: DAGNode]: set DAGNode { n.*(l+r) }
            fun ancestorsOf[n: DAGNode]: set DAGNode { n.~^(l+r) }
            fun ancestorsAndSelfOf[n: DAGNode]: set DAGNode { n.~*(l+r) }
            fun subDAG[n: DAGNode]: DAGNode -> DAGNode { n.*(l+r) <: (l+r) }


            fact {
                root = G0
                
                all imply: Imply {
                    all n: childrenOf[imply] | n in (Literal + And + Or + Neg)
                    all n: childrenOf[imply] & Neg | n.l in Literal
                    all n: childrenAndSelfOf[imply.l] & Or | no childrenOf[n] & And
                    all n: childrenAndSelfOf[imply.r] & And | no childrenOf[n] & Or
                }
            }

            one sig G0 extends G {} { l = Imply0 }
            one sig Imply0 extends Imply {}



            fact {
                Imply0.l = Xray or (Imply0.l in And and Imply0.l.l = Xray)
                
                Imply0.r = InPlace or (Imply0.r in Or and Imply0.r.l = InPlace)
                
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
            abstract sig PositiveTrace extends Trace {}
            abstract sig NegativeTrace extends Trace {}
            one sig Xray, EBeam, InPlace, Fired extends Literal {}
            one sig T0, T1, T2, T3, T4, T5, T6, T7, T8 extends SeqIdx {}
            fact {
                first = T0
                next = T0->T1 + T1->T2 + T2->T3 + T3->T4 + T4->T5 + T5->T6 + T6->T7 + T7->T8
            }
            
            one sig PT0 extends PositiveTrace {} {
                lasso = T8->T8
                EBeam->T1 + EBeam->T2 + EBeam->T3 + InPlace->T3 + Xray->T4 + InPlace->T4 + Xray->T5 + InPlace->T5 + Xray->T6 + InPlace->T6 + Xray->T7 + InPlace->T7 + Fired->T7 + InPlace->T8 in valuation
                no (Xray->T0 + EBeam->T0 + InPlace->T0 + Fired->T0 + Xray->T1 + InPlace->T1 + Fired->T1 + Xray->T2 + InPlace->T2 + Fired->T2 + Xray->T3 + Fired->T3 + EBeam->T4 + Fired->T4 + EBeam->T5 + Fired->T5 + EBeam->T6 + Fired->T6 + EBeam->T7 + Xray->T8 + EBeam->T8 + Fired->T8) & valuation
            }
            
            
            one sig NT0 extends NegativeTrace {} {
                lasso = T7->T7
                EBeam->T1 + EBeam->T2 + EBeam->T3 + InPlace->T3 + EBeam->T4 + InPlace->T4 + EBeam->T5 + InPlace->T5 + EBeam->T6 + InPlace->T6 + Fired->T6 + InPlace->T7 in valuation
                no (Xray->T0 + EBeam->T0 + InPlace->T0 + Fired->T0 + Xray->T1 + InPlace->T1 + Fired->T1 + Xray->T2 + InPlace->T2 + Fired->T2 + Xray->T3 + Fired->T3 + Xray->T4 + Fired->T4 + Xray->T5 + Fired->T5 + Xray->T6 + Xray->T7 + EBeam->T7 + Fired->T7) & valuation
            }

            one sig LearnedLTL {
                Root: DAGNode
            }
            fun root: one DAGNode { LearnedLTL.Root }
            fun childrenOf[n: DAGNode]: set DAGNode { n.^(l+r) }
            fun childrenAndSelfOf[n: DAGNode]: set DAGNode { n.*(l+r) }
            fun ancestorsOf[n: DAGNode]: set DAGNode { n.~^(l+r) }
            fun ancestorsAndSelfOf[n: DAGNode]: set DAGNode { n.~*(l+r) }
            fun subDAG[n: DAGNode]: DAGNode -> DAGNode { n.*(l+r) <: (l+r) }

            one sig And0 extends And {}
            fact {
                root = And0
                And0.l = G0 and And0.r = G1
                all imply: Imply {
                    all n: childrenOf[imply] | n in (Literal + And + Or + Neg)
                    all n: childrenOf[imply] & Neg | n.l in Literal
                    all n: childrenAndSelfOf[imply.l] & Or | no childrenOf[n] & And
                    all n: childrenAndSelfOf[imply.r] & And | no childrenOf[n] & Or
                }
            }

            one sig G0 extends G {} { l = Imply0 }
            one sig Imply0 extends Imply {}
            one sig And1 extends And {}


            fact {
                Imply0.l = And1 or (Imply0.l in And and Imply0.l.l = And1)
                (And1->Xray + And1->Fired) in subDAG[Imply0.l]
                Imply0.r = InPlace or (Imply0.r in Or and Imply0.r.l = InPlace)
                
            }

            one sig G1 extends G {} { l = Imply1 }
            one sig Imply1 extends Imply {}


            one sig Neg0 extends Neg {}
            fact {
                Imply1.l = EBeam or (Imply1.l in And and Imply1.l.l = EBeam)
                
                Imply1.r = Neg0 or (Imply1.r in Or and Imply1.r.l = Neg0)
                (Neg0->InPlace) in subDAG[Imply1.r]
            }


            run {
                all t: PositiveTrace | root->T0 in t.valuation
                all t: NegativeTrace | root->T0 not in t.valuation
                minsome l + r
            } for %d DAGNode
        """.trimIndent(),
            learner.generateAlloyModel()
        )
        assert(solution != null)
        assertEquals(
            "(And (G (Imply (And Fired Xray) InPlace)) (G (Imply (And EBeam Fired) (Neg InPlace))))",
            solution!!.getLTL()
        )
    }
}