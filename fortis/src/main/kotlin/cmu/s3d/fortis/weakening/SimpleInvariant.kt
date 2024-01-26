package cmu.s3d.fortis.weakening

data class SimpleInvariant(
    val antecedent: Conjunctions,
    val consequent: Conjunctions,
) {
    override fun toString(): String {
        val antecedentStr = antecedent.props.joinToString(" && ") { (name, value) ->
            if (value) name else "!$name"
        }
        val consequentStr = consequent.props.joinToString(" && ") { (name, value) ->
            if (value) name else "!$name"
        }
        return "[]($antecedentStr -> $consequentStr)"
    }
}