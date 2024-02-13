package cmu.s3d.fortis.weakening

import cmu.s3d.ltl.learning.LTLLearningSolution

data class SimpleGR1Invariant(
    val antecedent: CNF,
    val consequent: DNF,
) {
    companion object {
        fun oneFromString(inv: String): SimpleGR1Invariant {
            return multipleFromString(inv).firstOrNull() ?: error("Invalid invariant string: $inv")
        }

        fun multipleFromString(inv: String): List<SimpleGR1Invariant> {
            val invRegex = "\\[\\]\\s*\\(([^\\[\\]]+)\\)".toRegex()
            return invRegex.findAll(inv).map {
                val splits = it.groupValues[1].split("->")
                SimpleGR1Invariant(
                    splits[0].parseCNF(),
                    splits[1].parseDNF()
                )
            }.toList()
        }
    }
}

fun LTLLearningSolution.getGR1Invariant(): String {
    val root = getRoot()
    return getGR1Invariant(root)
}

private val operatorMapping = mapOf(
    "G"     to "[]",
    "Neg"   to "!",
    "And"   to "&&",
    "Or"    to "||",
    "Imply" to "->",
)

private fun LTLLearningSolution.getGR1Invariant(node: String) : String {
    val (name, leftNode, rightNode) = getNodeAndChildren(node)
    return when {
        leftNode == null && rightNode == null -> name
        leftNode != null && rightNode == null -> "${operatorMapping[name]}${getGR1Invariant(leftNode)}"
        leftNode != null && rightNode != null -> "(${getGR1Invariant(leftNode)} ${operatorMapping[name]} ${getGR1Invariant(rightNode)})"
        else -> error("Invalid LTL formula.")
    }
}