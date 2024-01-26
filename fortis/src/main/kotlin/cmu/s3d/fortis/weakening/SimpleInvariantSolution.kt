package cmu.s3d.fortis.weakening

import edu.mit.csail.sdg.parser.CompModule
import edu.mit.csail.sdg.parser.CompUtil
import edu.mit.csail.sdg.translator.A4Solution
import edu.mit.csail.sdg.translator.A4TupleSet

class SimpleInvariantSolution(private val world: CompModule, private val alloySolution: A4Solution) {

    fun getInvariant(): List<SimpleInvariant> {
        val expr = CompUtil.parseOneExpression_fromString(world, "Invariant")
        val invNames = (alloySolution.eval(expr) as A4TupleSet).map { it.atom(0).split('$')[0] }
        return invNames.map {
            SimpleInvariant(
                getPropositions("${it}.antecedent"),
                getPropositions("${it}.consequent")
            )
        }
    }

    private fun getPropositions(exprStr: String): Conjunctions {
        val expr = CompUtil.parseOneExpression_fromString(world, exprStr)
        return Conjunctions((alloySolution.eval(expr) as A4TupleSet).map {
            (it.atom(0).split('$')[0]) to (it.atom(1).split('$')[0] == "True")
        })
    }

    fun next(): SimpleInvariantSolution? {
        val nextSolution = alloySolution.next()
        return if (nextSolution.satisfiable()) {
            SimpleInvariantSolution(world, nextSolution)
        } else {
            null
        }
    }
}