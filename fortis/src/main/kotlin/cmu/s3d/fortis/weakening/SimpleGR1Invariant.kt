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

    fun toSimpleInvariant(): SimpleInvariant {
        if (antecedent.clauses.any { it.props.size > 1 }) error("Clauses in antecedent (CNF) should not be disjunctions")
        if (consequent.clauses.size != 1) error("Consequent must have exactly one clause")

        return SimpleInvariant(
            Conjunctions(antecedent.clauses.flatMap { it.props }),
            consequent.clauses[0]
        )
    }

    override fun toString(): String {
        return "[](${antecedent} -> ${consequent})"
    }

    fun toLTL2String(literals: List<String>): String {
        return "G(->(${toLTL2StringCNF(antecedent.clauses, literals)},${toLTL2StringDNF(consequent.clauses, literals)}))"
    }

    private fun toLTL2StringCNF(clauses: List<Disjunctions>, literals: List<String>): String {
        return if (clauses.size == 1) {
            toLTL2String(clauses[0], literals)
        } else if (clauses.size > 1) {
            "&(${toLTL2String(clauses[0], literals)},${toLTL2StringCNF(clauses.subList(1, clauses.size), literals)})"
        } else {
            error("Empty list of CNF clauses")
        }
    }

    private fun toLTL2String(clause: Disjunctions, literals: List<String>): String {
        return if (clause.props.size == 1) {
            toLTL2String(clause.props[0], literals)
        } else if (clause.props.size > 1) {
            "|(${toLTL2String(clause.props[0], literals)},${toLTL2String(Disjunctions(clause.props.subList(1, clause.props.size)), literals)})"
        } else {
            error("Empty list of Disjunction propositions")
        }
    }

    private fun toLTL2StringDNF(clauses: List<Conjunctions>, literals: List<String>): String {
        return if (clauses.size == 1) {
            toLTL2String(clauses[0], literals)
        } else if (clauses.size > 1) {
            "|(${toLTL2String(clauses[0], literals)},${toLTL2StringDNF(clauses.subList(1, clauses.size), literals)})"
        } else {
            error("Empty list of DNF clauses")
        }
    }

    private fun toLTL2String(clause: Conjunctions, literals: List<String>): String {
        return if (clause.props.size == 1) {
            toLTL2String(clause.props[0], literals)
        } else if (clause.props.size > 1) {
            "&(${toLTL2String(clause.props[0], literals)},${toLTL2String(Conjunctions(clause.props.subList(1, clause.props.size)), literals)})"
        } else {
            error("Empty list of Conjunction propositions")
        }
    }

    private fun toLTL2String(prop: Proposition, literals: List<String>): String {
        val name = "x${literals.indexOf(prop.first)}"
        return if (prop.second) name else "!($name)"
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
    val sigName = name.replace("\\d+$".toRegex(), "")
    return when {
        leftNode == null && rightNode == null -> name
        leftNode != null && rightNode == null -> "${operatorMapping[sigName]}${getGR1Invariant(leftNode)}"
        leftNode != null && rightNode != null -> "(${getGR1Invariant(leftNode)} ${operatorMapping[sigName]} ${getGR1Invariant(rightNode)})"
        else -> error("Invalid LTL formula.")
    }
}