package cmu.isr.ts.lts

import net.automatalib.words.Word

data class Fluent(
    val name: String,
    val trigger: List<String>,
    val reset: List<String>,
    val init: Boolean
)

fun String.toFluent(): Fluent? {
    val fluentTriggerReg = "\\{\\w+(?:\\s*,\\s*\\w+)*\\}|\\w+".toRegex()
    val fluentReg = Regex("fluent\\s+(\\w+)\\s*=\\s*<\\s*($fluentTriggerReg)\\s*,\\s*($fluentTriggerReg)\\s*>(?:\\s+initially\\s+([0|1]))?")

    return fluentReg.matchEntire(this)?.let { match ->
        val (name, trigger, reset, init) = match.destructured
        Fluent(
            name,
            trigger.trim { it == '{' || it == '}' }.split(",").map(String::trim),
            reset.trim { it == '{' || it == '}' }.split(",").map(String::trim),
            init == "1"
        )
    }
}

fun evaluateFluent(word: Word<String>, fluents: List<Fluent>): List<Map<Fluent, Boolean>> {
    val evaluation = mutableListOf<Map<Fluent, Boolean>>()

    // initial state
    evaluation.add(fluents.associateWith { it.init })
    // for each input in word
    for (i in 0 until word.size()) {
        val input = word.getSymbol(i)
        // evaluate each fluent
        val newEvaluation = evaluation.last().toMutableMap()
        for (fluent in fluents) {
            // if fluent is triggered, then set to true
            if (fluent.trigger.contains(input)) {
                newEvaluation[fluent] = true
            }
            // if fluent is reset, then set to false
            if (fluent.reset.contains(input)) {
                newEvaluation[fluent] = false
            }
        }
        evaluation.add(newEvaluation)
    }

    return evaluation
}