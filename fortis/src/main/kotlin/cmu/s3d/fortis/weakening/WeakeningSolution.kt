package cmu.s3d.fortis.weakening

import edu.mit.csail.sdg.parser.CompModule
import edu.mit.csail.sdg.parser.CompUtil
import edu.mit.csail.sdg.translator.A4Solution
import edu.mit.csail.sdg.translator.A4TupleSet

class WeakeningSolution(private val world: CompModule, private val alloySolution: A4Solution) {

    fun getInvariant(): Invariant {
        return Invariant(
            getPropositions("Invariant.antecedent"),
            getPropositions("Invariant.consequent")
        )
    }

    private fun getPropositions(exprStr: String): List<Pair<String, Boolean>> {
        val expr = CompUtil.parseOneExpression_fromString(world, exprStr)
        return (alloySolution.eval(expr) as A4TupleSet).map {
            (it.atom(0).split('$')[0]) to (it.atom(1).split('$')[0] == "True")
        }
    }

    fun next(): WeakeningSolution? {
        val nextSolution = alloySolution.next()
        return if (nextSolution.satisfiable()) {
            WeakeningSolution(world, nextSolution)
        } else {
            null
        }
    }
}