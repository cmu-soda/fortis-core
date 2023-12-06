package cmu.s3d.fortis.weakening

fun String.parseConjunction(): List<Pair<String, Boolean>> {
    val literals = this.split("&&").map { it.trim() }
    return literals.map {
        if (it.startsWith("!")) {
            it.substring(1) to false
        } else {
            it to true
        }
    }
}