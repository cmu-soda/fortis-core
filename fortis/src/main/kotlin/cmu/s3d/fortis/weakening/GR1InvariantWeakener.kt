package cmu.s3d.fortis.weakening

import cmu.s3d.fortis.ts.lts.Fluent
import cmu.s3d.fortis.ts.lts.evaluateFluent
import cmu.s3d.ltl.LassoTrace
import cmu.s3d.ltl.State
import cmu.s3d.ltl.learning.LTLLearner
import cmu.s3d.ltl.learning.LTLLearningSolution
import net.automatalib.word.Word

data class SimpleGR1Invariant(
    val antecedent: CNF,
    val consequent: DNF,
)

class GR1InvariantWeakener(
    private val invariant: SimpleGR1Invariant,
    private val fluents: List<Fluent>,
    private val positiveExamples: List<Word<String>>,
    private val negativeExamples: List<Word<String>>,
    private val maxNumOfNode: Int
) {
    fun learn(): LTLLearningSolution? {
        val constraints = generateConstraints()
        val ltlLearner = LTLLearner(
            literals = fluents.map { it.name },
            positiveExamples = positiveExamples.map { toLassoTrace(evaluateFluent(it, fluents)) },
            negativeExamples = negativeExamples.map { toLassoTrace(evaluateFluent(it, fluents)) },
            maxNumOfNode = maxNumOfNode,
            excludedOperators = listOf("F", "Until", "X"),
            customConstraints = constraints
        )
        return ltlLearner.learn()
    }

    private fun toLassoTrace(valuation: List<Map<Fluent, Boolean>>): LassoTrace {
        return LassoTrace(
            prefix = valuation.map { s -> State(s.map { (k, v) -> k.name to v }.toMap()) }
        )
    }

    private fun generateConstraints(): String {
        return """
            fact {
                root in G
                root.l in Imply
                
                all n: root.l.^(l+r) | n not in Imply + G
                all n: Neg & root.l.^(l+r) | n.l in Literal
                all n: Or & root.l.l.*(l+r) | no n.^(l+r) & And
                all n: And & root.l.r.*(l+r) | no n.^(l+r) & Or
            }
            ${generateInvariantConstraints()}
        """
    }

    private fun generateInvariantConstraints(): String {
        val antecedentDAG = mutableListOf<String>()
        val antecedentAnds = mutableListOf<String>()
        val antecedentOrs = mutableListOf<String>()
        val antecedentNegs = mutableListOf<String>()
        val antecedentRoot = generateDAG(invariant.antecedent, antecedentDAG, antecedentAnds, antecedentOrs, antecedentNegs)

        val consequentDAG = mutableListOf<String>()
        val consequentAnds = mutableListOf<String>()
        val consequentOrs = mutableListOf<String>()
        val consequentNegs = mutableListOf<String>()
        val consequentRoot = generateDAG(invariant.consequent, consequentDAG, consequentAnds, consequentOrs, consequentNegs)

        return """
            fact {
                some antecedent: DAGNode${
            if (antecedentAnds.isNotEmpty()) ", ${antecedentAnds.joinToString(", ")}: And" else ""
        }${
            if (antecedentOrs.isNotEmpty()) ", ${antecedentOrs.joinToString(", ")}: Or" else ""
        }${
            if (antecedentNegs.isNotEmpty()) ", ${antecedentNegs.joinToString(", ")}: Neg" else ""
        } {
                    antecedent in $antecedentRoot
                    root.l.l in $antecedentRoot or root.l.l in And and root.l.l.l in $antecedentRoot
                    ${if (antecedentDAG.isNotEmpty()) "(${antecedentDAG.joinToString(" + ")}) in ((l+r) :> root.l.l.^(l+r))" else ""}
                }
                
                some consequent: DAGNode${
            if (consequentOrs.isNotEmpty()) ", ${consequentOrs.joinToString(", ")}: Or" else ""
        }${
            if (consequentAnds.isNotEmpty()) ", ${consequentAnds.joinToString(", ")}: And" else ""
        }${
            if (consequentNegs.isNotEmpty()) ", ${consequentNegs.joinToString(", ")}: Neg" else ""
        } {
                    consequent in $consequentRoot
                    root.l.r in $consequentRoot or root.l.r in Or and root.l.r.l in $consequentRoot
                    ${if (consequentDAG.isNotEmpty()) "(${consequentDAG.joinToString(" + ")}) in ((l+r) :> root.l.r.^(l+r))" else ""}
                }
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
            val and = "a${ands.size}"
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
            val or = "o${ors.size}"
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
            return generateDAG(conjunction.props[0], dag, ands, ors, negs)
        else {
            val and = "a${ands.size}"
            ands.add(and)
            val l = generateDAG(conjunction.props[0], dag, ands, ors, negs)
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
            return generateDAG(disjunction.props[0], dag, ands, ors, negs)
        else {
            val or = "o${ors.size}"
            ors.add(or)
            val l = generateDAG(disjunction.props[0], dag, ands, ors, negs)
            val r = generateDAG(Disjunctions(disjunction.props.subList(1, disjunction.props.size)), dag, ands, ors, negs)
            dag.add("$or->$l")
            dag.add("$or->$r")
            return or
        }
    }

    private fun generateDAG(
        prop: Proposition,
        dag: MutableList<String>,
        ands: MutableList<String>,
        ors: MutableList<String>,
        negs: MutableList<String>
    ): String {
        if (prop.second) {
            return prop.first
        } else {
            val neg = "ng${negs.size}"
            negs.add(neg)
            dag.add("$neg->${prop.first}")
            return neg
        }
    }
}