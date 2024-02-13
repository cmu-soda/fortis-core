package cmu.s3d.fortis.weakening.benchmark

import cmu.s3d.fortis.weakening.SimpleInvariant
import cmu.s3d.ltl.FiniteTrace
import cmu.s3d.ltl.State

data class Problem(
    val literals: List<String>,
    val oldInvariant: SimpleInvariant,
    val expected: SimpleInvariant,
    val positiveTraces: List<FiniteTrace>,
    val negativeTraces: List<FiniteTrace>,
    val maxNumOfNode: Int?
) {
    override fun toString(): String {
        return """
            ${positiveTraces.joinToString("\n            ") { finiteTraceToString(it) } }
            ---
            ${negativeTraces.joinToString("\n            ") { finiteTraceToString(it) } }
            ---
            G,!,&,|,->
            ---
            ${maxNumOfNode ?: 3}
            ---
            ${expected.toLTL2String(literals)}
            ---
            $expected
            ---
            $oldInvariant
            ---
            ${literals.joinToString(",")}
        """.trimIndent()
    }

    fun toCSVString(): String {
        return "\"${oldInvariant}\",\"${expected}\",${(positiveTraces + negativeTraces).maxOf { it.size }}," +
                "${positiveTraces.size},${negativeTraces.size}"
    }

    private fun finiteTraceToString(trace: FiniteTrace): String {
        return trace.joinToString(";") { s -> literals.joinToString(",") { if (s.values[it] == true) "1" else "0" } }
    }
}

object ProblemParser {
    fun parseTask(task: String): Problem {
        val parts = task.split("---")
        val positives = parts[0].trim()
        val negatives = parts[1].trim()
        val maxNumOfNode = parts[3].trim().toInt()
        val expected = parts[5].trim()
        val oldInvariant = parts[6].trim()
        val literals = parts[7].trim().split(",")

        return Problem(
            literals = literals,
            oldInvariant = SimpleInvariant.oneFromString(oldInvariant),
            expected = SimpleInvariant.oneFromString(expected),
            positiveTraces = positives.lines().map { parseFiniteTrace(it, literals) },
            negativeTraces = negatives.lines().map { parseFiniteTrace(it, literals) },
            maxNumOfNode = maxNumOfNode
        )
    }

    private fun parseFiniteTrace(trace: String, literals: List<String>): FiniteTrace {
        return trace.split(";").map { parseState(it, literals) }
    }

    private fun parseState(state: String, literals: List<String>): State {
        return State(state.split(",").mapIndexed { i, v -> literals[i] to (v == "1") }.toMap())
    }
}