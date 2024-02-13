package cmu.s3d.fortis.weakening

data class SimpleInvariant(
    val antecedent: Conjunctions,
    val consequent: Conjunctions,
) {
    companion object {
        fun oneFromString(inv: String): SimpleInvariant {
            return multipleFromString(inv).firstOrNull() ?: error("Invalid invariant string: $inv")
        }

        fun multipleFromString(inv: String): List<SimpleInvariant> {
            val invRegex = "\\[\\]\\s*\\(([^\\[\\]]+)\\)".toRegex()
            return invRegex.findAll(inv).map {
                val splits = it.groupValues[1].split("->")
                SimpleInvariant(
                    splits[0].parseConjunction(),
                    splits[1].parseConjunction()
                )
            }.toList()
        }
    }

    override fun toString(): String {
        val antecedentStr = antecedent.props.joinToString(" && ") { (name, value) ->
            if (value) name else "!$name"
        }
        val consequentStr = consequent.props.joinToString(" && ") { (name, value) ->
            if (value) name else "!$name"
        }
        return "[]($antecedentStr -> $consequentStr)"
    }

    fun toGR1Invariant(): SimpleGR1Invariant {
        return SimpleGR1Invariant(
            antecedent = CNF(antecedent.props.map { Disjunctions(listOf(it)) }),
            consequent = DNF(listOf(consequent))
        )
    }

    fun toLTL2String(literals: List<String>): String {
        return "G(->(${toLTL2String(antecedent.props, literals)},${toLTL2String(consequent.props, literals)}))"
    }

    private fun toLTL2String(props: List<Proposition>, literals: List<String>): String {
        return if (props.size == 1) {
            toLTL2String(props[0], literals)
        } else if (props.size > 1) {
            "&(${toLTL2String(props[0], literals)},${toLTL2String(props.subList(1, props.size), literals)})"
        } else {
            error("Empty list of propositions")
        }
    }

    private fun toLTL2String(prop: Proposition, literals: List<String>): String {
        val name = "x${literals.indexOf(prop.first)}"
        return if (prop.second) name else "!($name)"
    }
}