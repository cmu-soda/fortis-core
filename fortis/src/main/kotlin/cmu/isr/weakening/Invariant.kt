package cmu.isr.weakening

data class Invariant(
    val antecedent: List<Pair<String, Boolean>>,
    val consequent: List<Pair<String, Boolean>>,
) {
    override fun toString(): String {
        val antecedentStr = antecedent.joinToString(" && ") { (name, value) ->
            if (value) name else "!$name"
        }
        val consequentStr = consequent.joinToString(" && ") { (name, value) ->
            if (value) name else "!$name"
        }
        return "$antecedentStr -> $consequentStr"
    }
}