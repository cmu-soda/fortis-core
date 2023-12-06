package cmu.s3d.fortis.robustness

import cmu.s3d.fortis.assumption.SubsetConstructionGenerator
import cmu.s3d.fortis.assumption.WeakestAssumptionGenerator
import cmu.s3d.fortis.common.EquivClass
import cmu.s3d.fortis.common.RepTrace
import cmu.s3d.fortis.common.RobustnessOptions
import cmu.s3d.fortis.ts.*
import cmu.s3d.fortis.ts.lts.hide
import cmu.s3d.fortis.ts.lts.makeErrorState
import net.automatalib.common.util.Holder
import net.automatalib.util.ts.traversal.TSTraversal
import net.automatalib.util.ts.traversal.TSTraversalAction
import net.automatalib.util.ts.traversal.TSTraversalVisitor
import net.automatalib.word.Word
import org.slf4j.LoggerFactory

class BaseCalculator(
    private val sys: LTS<*, String>,
    private val env: LTS<*, String>,
    private val safety: DetLTS<*, String>,
    override val options: RobustnessOptions,
) : RobustnessCalculator {

    private val waGenerator: WeakestAssumptionGenerator = SubsetConstructionGenerator(sys, env, safety)
    private var wa: DetLTS<Int, String>? = null

    private val logger = LoggerFactory.getLogger(javaClass)

    override val weakestAssumption: DetLTS<Int, String>
        get() {
            if (wa == null) {
                logger.info("Generating the weakest assumption...")
                wa = waGenerator.generate(options)
            }
            return wa!!
        }

    override fun computeUnsafeBeh(): Map<EquivClass, Collection<RepTrace>> {
        logger.info("Generating unsafe behavior representation traces by equivalence classes...")
        val m = waGenerator.generateUnsafe()
        val traces = shortestDeltaTraces(m)
        if (traces.isEmpty())
            logger.info("No representation traces found. The system is safe under any environment.")
        return traces
    }

    override fun computeRobustness(): Map<EquivClass, Collection<RepTrace>> {
        logger.info("Generating robust behavior representation traces by equivalence classes...")
        val projectedEnv = hide(env, env.alphabet() - weakestAssumption.alphabet().toSet())
        val delta = parallel(weakestAssumption, makeErrorState(projectedEnv))
        val traces = shortestDeltaTraces(delta, if (options.expand) weakestAssumption else null)
        if (traces.isEmpty())
            logger.info("No representation traces found. The weakest assumption has equal or less behavior than the environment")
        return traces
    }

    override fun compare(cal: RobustnessCalculator): Map<EquivClass, Collection<RepTrace>> {
        if (weakestAssumption.alphabet().toSet() != cal.weakestAssumption.alphabet().toSet())
            error("The two weakest assumption should have the same alphabets")
        logger.info("Generating robust behavior representation traces by equivalence classes...")
        val delta = parallel(weakestAssumption, makeErrorState(cal.weakestAssumption))
        val traces = shortestDeltaTraces(delta, if (options.expand) weakestAssumption else null)
        if (traces.isEmpty())
            logger.info("No representation traces found. The weakest assumption of this model has equal or less behavior than the other model.")
        return traces
    }

    private fun shortestDeltaTraces(
        delta: DetLTS<Int, String>,
        lts: LTS<Int, String>? = null
    ): Map<EquivClass, Collection<RepTrace>> {
        val predecessors = Predecessors(delta)
        val transToError = delta.alphabet().flatMap { predecessors.getPredecessors(delta.errorState, it) }
        val statesToError = transToError.map { it.source }.toSet()
        if (statesToError.isEmpty())
            return emptyMap()
        val traces = mutableMapOf<Int, Word<String>>()
        TSTraversal.breadthFirst(delta, delta.alphabet(), PathFromInitVisitor(statesToError, traces))
        return transToError.associate { (_, source, a) ->
            EquivClass(source, a) to Word.fromWords(traces[source], Word.fromLetter(a)).let {
                if (lts != null) acyclicRepTraces(lts, it) else listOf(RepTrace(it, false))
            }
        }
    }

    private fun acyclicRepTraces(lts: LTS<Int, String>, prefix: Word<String>): Collection<RepTrace> {
        val traces = mutableListOf<RepTrace>()
        TSTraversal.breadthFirst(lts, lts.alphabet(), AcyclicTracesWithPrefixVisitor(lts, prefix, traces))
        return traces
    }
}

private class PrefixTrace(val word: Word<String>, val visited: Set<Int>)

private class AcyclicTracesWithPrefixVisitor(
    private val lts: LTS<Int, String>,
    private val prefix: Word<String>,
    private val result: MutableList<RepTrace>
) : TSTraversalVisitor<Int, String, Int, PrefixTrace> {
    override fun processInitial(state: Int, outData: Holder<PrefixTrace>): TSTraversalAction {
        outData.value = PrefixTrace(Word.epsilon(), emptySet())
        return TSTraversalAction.EXPLORE
    }

    override fun startExploration(state: Int, data: PrefixTrace): Boolean {
        val matched = data.word.length() >= prefix.length()
        return if (matched && state in data.visited) {
            result.add(RepTrace(data.word, false))
            false
        } else if (matched && noOutputTransition(state)) {
            result.add(RepTrace(data.word, true))
            false
        } else {
            true
        }
    }

    override fun processTransition(
        source: Int,
        srcData: PrefixTrace,
        input: String,
        transition: Int,
        succ: Int,
        outData: Holder<PrefixTrace>
    ): TSTraversalAction {
        return if (srcData.word.length() >= prefix.length() || prefix.getSymbol(srcData.word.length()) == input) {
            outData.value = PrefixTrace(Word.fromWords(srcData.word, Word.fromLetter(input)), srcData.visited + source)
            TSTraversalAction.EXPLORE
        } else {
            TSTraversalAction.ABORT_INPUT
        }
    }

    private fun noOutputTransition(state: Int): Boolean {
        for (a in lts.alphabet()) {
            if (lts.getTransitions(state, a).isNotEmpty())
                return false
        }
        return true
    }
}