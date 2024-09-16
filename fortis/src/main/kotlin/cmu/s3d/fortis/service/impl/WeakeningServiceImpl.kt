package cmu.s3d.fortis.service.impl

import cmu.s3d.fortis.common.Spec
import cmu.s3d.fortis.common.SpecType
import cmu.s3d.fortis.common.asSerializableWord
import cmu.s3d.fortis.service.WeakeningService
import cmu.s3d.fortis.ts.lts.ltsa.LTSACall
import cmu.s3d.fortis.ts.lts.ltsa.LTSACall.asLTS
import cmu.s3d.fortis.ts.lts.ltsa.LTSACall.compose
import cmu.s3d.fortis.ts.lts.toFluent
import cmu.s3d.fortis.ts.numOfTransitions
import cmu.s3d.fortis.ts.parallel
import cmu.s3d.fortis.weakening.*
import cmu.s3d.ltl.learning.LTLLearningSolution
import net.automatalib.alphabet.Alphabets
import net.automatalib.automaton.fsa.NFA
import net.automatalib.word.Word
import org.slf4j.LoggerFactory

class WeakeningServiceImpl : WeakeningService {
    private var simpleSolution: SimpleInvariantSolution? = null
    private var gr1Solution: LTLLearningSolution? = null
    private val solutions: MutableList<String> = mutableListOf()
    private val logger = LoggerFactory.getLogger(javaClass)

    private fun resetSolution() {
        simpleSolution = null
        gr1Solution = null
        solutions.clear()
    }

    @Deprecated("Use generateExamplesFromTrace instead")
    override fun generateExamplesFromProgress(
        sysSpecs: List<Spec>,
        envSpecs: List<Spec>,
        progress: String,
        fluents: List<String>
    ): List<Word<String>> {
        val sys = parseSpecs(sysSpecs)
        val env = parseSpecs(envSpecs)
        val model = parallel(sys, env)
        return ProgressExampleGenerator(model, progress)
            .withFilter(InvariantExampleFilter(fluents.map { it.toFluent()?: error("Invalid fluent string") }))
            .map { it.asSerializableWord() }.toList()
    }

    override fun generateExamplesFromTrace(
        sysSpecs: List<Spec>,
        envSpecs: List<Spec>,
        trace: Word<String>,
        inputs: Collection<String>,
        fluents: List<String>,
        numOfAdditionalExamples: Int
    ): List<Word<String>> {
        val start = System.currentTimeMillis()
        val sys = parseSpecs(sysSpecs)
        val env = parseSpecs(envSpecs)
        val model = parallel(sys, env)

        logger.info("Generating examples for model with ${model.size()} states and ${model.numOfTransitions()} transitions")
        val results = TraceExampleGenerator(model, trace, Alphabets.fromCollection(inputs), numOfAdditionalExamples)
            .withFilter(InvariantExampleFilter(fluents.map { it.toFluent()?: error("Invalid fluent string") }))
            .map { it.asSerializableWord() }.toList()
        logger.info("Generation completed in ${System.currentTimeMillis() - start}ms")

        return results
    }

    override fun weakenSafetyInvariant(
        invariant: String,
        fluents: List<String>,
        positiveExamples: List<Word<String>>,
        negativeExamples: List<Word<String>>
    ): String? {
        resetSolution()

        // FIXME: This assumes that the invariant is in the form: [](a -> b) && [](c -> d), but LTSA does not support.
        val invariantPairs = SimpleInvariant.multipleFromString(invariant)
        if (invariantPairs.isEmpty())
            error("Invalid invariant format")

        val start = System.currentTimeMillis()
        val weakener = SimpleInvariantWeakener.build(
            invariant = invariantPairs,
            fluents = fluents.map { it.toFluent()?: error("Invalid fluent string") },
            positiveExamples = positiveExamples,
            negativeExamples = negativeExamples
        )
        try {
            simpleSolution = weakener.learn()
        } catch (e: Exception) {
            logger.error("Error in learning safety invariant: {}", e.stackTraceToString())
            throw RuntimeException("Error in learning safety invariant: ${e.message}")
        }
        logger.info("Weakening completed in ${System.currentTimeMillis() - start}ms")
        return simpleSolution?.getInvariant()?.joinToString(" && ")
    }

    override fun weakenGR1Invariant(
        invariant: String,
        fluents: List<String>,
        positiveExamples: List<Word<String>>,
        negativeExamples: List<Word<String>>,
        maxNumOfNode: Int
    ): String? {
        resetSolution()

        // FIXME: This assumes that the invariant is in the form: [](a -> b) && [](c -> d), but LTSA does not support.
        val invariantPairs = SimpleGR1Invariant.multipleFromString(invariant)
        if (invariantPairs.isEmpty())
            error("Invalid invariant format")

        val start = System.currentTimeMillis()
        val weakener = GR1InvariantWeakener.build(
            invariant = invariantPairs,
            fluents = fluents.map { it.toFluent()?: error("Invalid fluent string") },
            positiveExamples = positiveExamples,
            negativeExamples = negativeExamples,
            maxNumOfNode = maxNumOfNode + fluents.size
        )
        try {
            gr1Solution = weakener.learn()
        } catch (e: Exception) {
            logger.error("Error in learning GR1 invariant: {}", e.stackTraceToString())
            throw RuntimeException("Error in learning GR1 invariant: ${e.message}")
        }
        logger.info("Weakening completed in ${System.currentTimeMillis() - start}ms")
        return gr1Solution?.getGR1Invariant()
    }

    override fun nextSolution(): String? {
        try {
            val start = System.currentTimeMillis()
            val sol = when {
                simpleSolution != null -> {
                    simpleSolution = simpleSolution!!.next()
                    simpleSolution?.getInvariant()?.joinToString(" && ")
                }

                gr1Solution != null -> {
                    solutions.add(gr1Solution!!.getGR1Invariant())
                    do {
                        gr1Solution = gr1Solution!!.next()
                    } while (gr1Solution != null && gr1Solution!!.getGR1Invariant() in solutions)
                    gr1Solution?.getGR1Invariant()
                }

                else -> null
            }
            logger.info("Find next solution completed in ${System.currentTimeMillis() - start}ms")
            return sol
        } catch (e: Exception) {
            logger.error("Error in finding next solution: {}", e.stackTraceToString())
            throw RuntimeException("Error in finding next solution: ${e.message}")
        }
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