package cmu.s3d.fortis.service.impl

import cmu.s3d.fortis.common.Spec
import cmu.s3d.fortis.common.SpecType
import cmu.s3d.fortis.common.asSerializableWord
import cmu.s3d.fortis.service.WeakeningService
import cmu.s3d.fortis.ts.lts.ltsa.LTSACall
import cmu.s3d.fortis.ts.lts.ltsa.LTSACall.asLTS
import cmu.s3d.fortis.ts.lts.ltsa.LTSACall.compose
import cmu.s3d.fortis.ts.lts.toFluent
import cmu.s3d.fortis.ts.parallel
import cmu.s3d.fortis.weakening.*
import net.automatalib.alphabet.Alphabets
import net.automatalib.automaton.fsa.NFA
import net.automatalib.word.Word

class WeakeningServiceImpl : WeakeningService {
    override fun generateExamplesFromProgress(
        sysSpecs: List<Spec>,
        envSpecs: List<Spec>,
        progress: String,
        fluents: List<String>
    ): List<Word<String>> {
        val sys = parseSpecs(sysSpecs)
        val env = parseSpecs(envSpecs)
        val model = parallel(sys, env)
        return ExampleFilterForInvariant(
            ProgressExampleGenerator(model, progress),
            fluents.map { it.toFluent()?: error("Invalid fluent string") }
        ).map { it.asSerializableWord() }.toList()
    }

    override fun generateExamplesFromTrace(
        sysSpecs: List<Spec>,
        envSpecs: List<Spec>,
        trace: Word<String>,
        inputs: Collection<String>,
        fluents: List<String>
    ): List<Word<String>> {
        val sys = parseSpecs(sysSpecs)
        val env = parseSpecs(envSpecs)
        val model = parallel(sys, env)
        return ExampleFilterForInvariant(
            TraceExampleGenerator(model, trace, Alphabets.fromCollection(inputs)),
            fluents.map { it.toFluent()?: error("Invalid fluent string") }
        ).map { it.asSerializableWord() }.toList()
    }

    override fun weakenSafetyInvariant(
        invariant: String,
        fluents: List<String>,
        positiveExamples: List<Word<String>>,
        negativeExamples: List<Word<String>>
    ): List<String> {
        val invRegex = "\\[\\]\\s*\\(([^\\[\\]]+)\\)".toRegex()
        val invariantPairs = invRegex.findAll(invariant).map {
            val splits = it.groupValues[1].split("->")
            SimpleInvariant(
                splits[0].parseConjunction(),
                splits[1].parseConjunction()
            )
        }.toList()
        if (invariantPairs.isEmpty())
            error("Invalid invariant format")

        val weakener = SimpleInvariantWeakener(
            invariant = invariantPairs,
            fluents = fluents.map { it.toFluent()?: error("Invalid fluent string") },
            positiveExamples = positiveExamples,
            negativeExamples = negativeExamples
        )
        val solutions = mutableListOf<String>()
        var solution = weakener.learn()
        while (solution != null) {
            solutions.add(solution.getInvariant().joinToString(" && "))
            solution = solution.next()
        }
        return solutions
    }

    private fun parseSpec(spec: Spec): NFA<Int, String> {
        return when (spec.type) {
            SpecType.FSP -> LTSACall.compile(spec.content).compose().asLTS()
            else -> error("Unsupported spec type")
        }
    }

    private fun parseSpecs(specs: List<Spec>): NFA<Int, String> {
        if (specs.isEmpty()) error("Specs cannot be empty")
        if (specs.size == 1) return parseSpec(specs.first())
        return parallel(*specs.map { parseSpec(it) }.toTypedArray())
    }
}