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

    private fun generateConstraints(): String {
        return """
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
            ${invariant.joinToString("") { generateInvariantConstraints(it) } }
        """
    }

    private fun generateInvariantConstraints(inv: SimpleGR1Invariant): String {
        val antecedentDAG = mutableListOf<String>()
        val antecedentAnds = mutableListOf<String>()
        val antecedentOrs = mutableListOf<String>()
        val antecedentNegs = mutableListOf<String>()
        val antecedentRoot = generateDAG(inv.antecedent, antecedentDAG, antecedentAnds, antecedentOrs, antecedentNegs)

        val consequentDAG = mutableListOf<String>()
        val consequentAnds = mutableListOf<String>()
        val consequentOrs = mutableListOf<String>()
        val consequentNegs = mutableListOf<String>()
        val consequentRoot = generateDAG(inv.consequent, consequentDAG, consequentAnds, consequentOrs, consequentNegs)

        return """
            fact {
                some rt: childrenAndSelfOf[root] & G {
                    some antecedent: DAGNode${
            if (antecedentAnds.isNotEmpty()) ", ${antecedentAnds.joinToString(", ")}: And" else ""
        }${
            if (antecedentOrs.isNotEmpty()) ", ${antecedentOrs.joinToString(", ")}: Or" else ""
        }${
            if (antecedentNegs.isNotEmpty()) ", ${antecedentNegs.joinToString(", ")}: Neg" else ""
        } {
                        antecedent in $antecedentRoot
                        rt.l.l in $antecedentRoot or rt.l.l in And and rt.l.l.l in $antecedentRoot
                        ${if (antecedentDAG.isNotEmpty()) "(${antecedentDAG.joinToString(" + ")}) in subDAG[rt.l.l]" else ""}
                    }
                
                    some consequent: DAGNode${
            if (consequentOrs.isNotEmpty()) ", ${consequentOrs.joinToString(", ")}: Or" else ""
        }${
            if (consequentAnds.isNotEmpty()) ", ${consequentAnds.joinToString(", ")}: And" else ""
        }${
            if (consequentNegs.isNotEmpty()) ", ${consequentNegs.joinToString(", ")}: Neg" else ""
        } {
                        consequent in $consequentRoot
                        rt.l.r in $consequentRoot or rt.l.r in Or and rt.l.r.l in $consequentRoot
                        ${if (consequentDAG.isNotEmpty()) "(${consequentDAG.joinToString(" + ")}) in subDAG[rt.l.r]" else ""}
                    }
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