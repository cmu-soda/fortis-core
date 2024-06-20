package cmu.s3d.fortis.weakening

typealias Proposition = Pair<String, Boolean>

data class Conjunctions(val props: List<Proposition>) {
    override fun toString(): String {
        return props.joinToString(" && ") { (name, value) ->
            if (value) name else "!$name"
        }
    }
}

data class Disjunctions(val props: List<Proposition>) {
    override fun toString(): String {
        return props.joinToString(" || ") { (name, value) ->
            if (value) name else "!$name"
        }
    }
}

data class CNF(val clauses: List<Disjunctions>) {
    override fun toString(): String {
        return clauses.joinToString(" && ") { if (it.props.size > 1) "($it)" else it.toString() }
    }
}

data class DNF(val clauses: List<Conjunctions>) {
    override fun toString(): String {
        return clauses.joinToString(" || ") { if (it.props.size > 1) "($it)" else it.toString() }
    }
}

fun String.parseConjunction(): Conjunctions {
    val literals = this.split("&&").map { it.trim() }
    return Conjunctions(literals.map {
        if (it.startsWith("!")) {
            it.substring(1) to false
        } else {
            it to true
        }
    })
}

fun String.parseDisjunction(): Disjunctions {
    val literals = this.split("||").map { it.trim() }
    return Disjunctions(literals.map {
        if (it.startsWith("!")) {
            it.substring(1) to false
        } else {
            it to true
        }
    })
}

fun String.parseCNF(): CNF {
    val clauses = this.split("&&").map { it.trim().trim('(', ')') }
    return CNF(clauses.map { it.parseDisjunction() })
}

fun String.parseDNF(): DNF {
    val clauses = this.split("||").map { it.trim().trim('(', ')') }
    return DNF(clauses.map { it.parseConjunction() })
}