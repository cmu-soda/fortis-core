package cmu.s3d.fortis.weakening

import cmu.s3d.fortis.ts.lts.Fluent
import cmu.s3d.fortis.ts.lts.evaluateFluent
import cmu.s3d.ltl.FiniteTrace
import cmu.s3d.ltl.LassoTrace
import cmu.s3d.ltl.learning.LTLLearner
import cmu.s3d.ltl.learning.LTLLearningSolution
import edu.mit.csail.sdg.translator.A4Options
import net.automatalib.word.Word

class GR1InvariantWeakener(
    private val invariant: List<SimpleGR1Invariant>,
    literals: List<String>,
    positiveTraces: List<FiniteTrace>,
    negativeTraces: List<FiniteTrace>,
    maxNumOfNode: Int,
    customAlloyOptions: A4Options? = null
) {
    private val ltlLearner: LTLLearner

    init {
        val constraints = generateConstraints()
        ltlLearner = LTLLearner(
            literals = literals,
            positiveExamples = positiveTraces.map { LassoTrace(prefix = it) },
            negativeExamples = negativeTraces.map { LassoTrace(prefix = it) },
            maxNumOfNode = maxNumOfNode,
            excludedOperators = listOf("F", "Until", "X"),
            customConstraints = constraints,
            customAlloyOptions = customAlloyOptions
        )
    }

    companion object {
        fun build(
            invariant: List<SimpleGR1Invariant>,
            fluents: List<Fluent>,
            positiveExamples: List<Word<String>>,
            negativeExamples: List<Word<String>>,
            maxNumOfNode: Int
        ): GR1InvariantWeakener {
            return GR1InvariantWeakener(
                invariant,
                fluents.map { it.name },
                positiveExamples.map { evaluateFluent(it, fluents) },
                negativeExamples.map { evaluateFluent(it, fluents) },
                maxNumOfNode
            )
        }
    }

    fun learn(): LTLLearningSolution? {
        return ltlLearner.learn()
    }

    fun generateAlloyModel(): String {
        return ltlLearner.generateAlloyModel()
    }

    private var numberOfGs = 0
    private var numberOfImplies = 0
    private var numberOfAnds = 0
    private var numberOfOrs = 0
    private var numberOfNegs = 0

    private fun generateConstraints(): String {
        val Ands = (0 until invariant.size-1).map { "And${numberOfAnds++}" }
        val Gs = invariant.indices.map { "G$it" }
        val dag = mutableListOf<String>()

        return """
            ${if (Ands.isNotEmpty()) "one sig ${Ands.joinToString(", ")} extends And {}" else ""}
            fact {
                root = ${generateConjunctionOfInvariants(Ands, Gs, dag)}
                ${if (dag.isNotEmpty()) dag.joinToString(" and ") else ""}
                all imply: Imply {
                    all n: childrenOf[imply] | n in (Literal + And + Or + Neg)
                    all n: childrenOf[imply] & Neg | n.l in Literal
                    all n: childrenAndSelfOf[imply.l] & Or | no childrenOf[n] & And
                    all n: childrenAndSelfOf[imply.r] & And | no childrenOf[n] & Or
                }
            }
            ${invariant.joinToString("") { generateInvariantConstraints(it) }}
            fact {
                all n: And + Or + Imply | n.l != n.r
            }
        """
    }

    private fun generateConjunctionOfInvariants(Ands: List<String>, Gs: List<String>, dag: MutableList<String>): String {
        return if (Ands.isEmpty()) {
            return Gs[0]
        } else {
            val and = Ands[0]
            val l = Gs[0]
            val r = generateConjunctionOfInvariants(Ands.subList(1, Ands.size), Gs.subList(1, Gs.size), dag)
            dag.add("$and.l = $l")
            dag.add("$and.r = $r")
            and
        }
    }

    private fun generateInvariantConstraints(inv: SimpleGR1Invariant): String {
        val G = "G${numberOfGs++}"
        val Imply = "Imply${numberOfImplies++}"
        val Ands = mutableListOf<String>()
        val Ors = mutableListOf<String>()
        val Negs = mutableListOf<String>()
        val antecedentDAG = mutableListOf<String>()
        val antecedentRoot = generateDAG(inv.antecedent, antecedentDAG, Ands, Ors, Negs)
        val consequentDAG = mutableListOf<String>()
        val consequentRoot = generateDAG(inv.consequent, consequentDAG, Ands, Ors, Negs)

        return """
            one sig $G extends G {} { l = $Imply }
            one sig $Imply extends Imply {}
            ${if (Ands.isNotEmpty()) "one sig ${Ands.joinToString(", ")} extends And {}" else ""}
            ${if (Ors.isNotEmpty()) "one sig ${Ors.joinToString(", ")} extends Or {}" else ""}
            ${if (Negs.isNotEmpty()) "one sig ${Negs.joinToString(", ")} extends Neg {}" else ""}
            fact {
                ${Imply}.l = $antecedentRoot or (${Imply}.l in And and ${Imply}.l.l = $antecedentRoot)
                ${if (antecedentDAG.isNotEmpty()) "(${antecedentDAG.joinToString(" + ")}) in subDAG[${Imply}.l]" else ""}
                ${Imply}.r = $consequentRoot or (${Imply}.r in Or and ${Imply}.r.l = $consequentRoot)
                ${if (consequentDAG.isNotEmpty()) "(${consequentDAG.joinToString(" + ")}) in subDAG[${Imply}.r]" else ""}
            }
        """
    }

    private fun generateDAG(
        cnf: CNF,
        dag: MutableList<String>,
        ands: MutableList<String>,
        ors: MutableList<String>,
        negs: MutableList<String>
    ): String {
        if (cnf.clauses.size == 1)
            return generateDAG(cnf.clauses[0], dag, ands, ors, negs)
        else {
            val and = "And${numberOfAnds++}"
            ands.add(and)
            val l = generateDAG(cnf.clauses[0], dag, ands, ors, negs)
            val r = generateDAG(CNF(cnf.clauses.subList(1, cnf.clauses.size)), dag, ands, ors, negs)
            dag.add("$and->$l")
            dag.add("$and->$r")
            return and
        }
    }

    private fun generateDAG(
        dnf: DNF,
        dag: MutableList<String>,
        ands: MutableList<String>,
        ors: MutableList<String>,
        negs: MutableList<String>
    ): String {
        if (dnf.clauses.size == 1)
            return generateDAG(dnf.clauses[0], dag, ands, ors, negs)
        else {
            val or = "Or${numberOfOrs++}"
            ors.add(or)
            val l = generateDAG(dnf.clauses[0], dag, ands, ors, negs)
            val r = generateDAG(DNF(dnf.clauses.subList(1, dnf.clauses.size)), dag, ands, ors, negs)
            dag.add("$or->$l")
            dag.add("$or->$r")
            return or
        }
    }

    private fun generateDAG(
        conjunction: Conjunctions,
        dag: MutableList<String>,
        ands: MutableList<String>,
        ors: MutableList<String>,
        negs: MutableList<String>
    ): String {
        if (conjunction.props.size == 1)
            return generateDAG(conjunction.props[0], dag, negs)
        else {
            val and = "And${numberOfAnds++}"
            ands.add(and)
            val l = generateDAG(conjunction.props[0], dag, negs)
            val r = generateDAG(Conjunctions(conjunction.props.subList(1, conjunction.props.size)), dag, ands, ors, negs)
            dag.add("$and->$l")
            dag.add("$and->$r")
            return and
        }
    }

    private fun generateDAG(
        disjunction: Disjunctions,
        dag: MutableList<String>,
        ands: MutableList<String>,
        ors: MutableList<String>,
        negs: MutableList<String>
    ): String {
        if (disjunction.props.size == 1)
            return generateDAG(disjunction.props[0], dag, negs)
        else {
            val or = "Or${numberOfOrs++}"
            ors.add(or)
            val l = generateDAG(disjunction.props[0], dag, negs)
            val r = generateDAG(Disjunctions(disjunction.props.subList(1, disjunction.props.size)), dag, ands, ors, negs)
            dag.add("$or->$l")
            dag.add("$or->$r")
            return or
        }
    }

    private fun generateDAG(
        prop: Proposition,
        dag: MutableList<String>,
        negs: MutableList<String>
    ): String {
        if (prop.second) {
            return prop.first
        } else {
            val neg = "Neg${numberOfNegs++}"
            negs.add(neg)
            dag.add("$neg->${prop.first}")
            return neg
        }
    }
}