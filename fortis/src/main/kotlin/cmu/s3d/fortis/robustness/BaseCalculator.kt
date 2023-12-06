package cmu.s3d.fortis.robustness

import cmu.s3d.fortis.assumption.SubsetConstructionGenerator
import cmu.s3d.fortis.assumption.WeakestAssumptionGenerator
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
) : RobustnessCalculator<Int, String> {

    private val waGenerator: WeakestAssumptionGenerator<String> = SubsetConstructionGenerator(sys, env, safety)
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

    override fun computeUnsafeBeh(): Map<EquivClass<String>, Collection<RepTrace<String>>> {
        logger.info("Generating unsafe behavior representation traces by equivalence classes...")
        val m = waGenerator.generateUnsafe()
        val traces = shortestDeltaTraces(m)
        if (traces.isEmpty())
            logger.info("No representation traces found. The system is safe under any environment.")
        return traces
    }

    override fun computeRobustness(): Map<EquivClass<String>, Collection<RepTrace<String>>> {
        logger.info("Generating robust behavior representation traces by equivalence classes...")
        val projectedEnv = hide(env, env.alphabet() - weakestAssumption.alphabet().toSet())
        val delta = parallel(weakestAssumption, makeErrorState(projectedEnv))
        val traces = shortestDeltaTraces(delta, if (options.expand) weakestAssumption else null)
        if (traces.isEmpty())
            logger.info("No representation traces found. The weakest assumption has equal or less behavior than the environment")
        return traces
    }

    override fun compare(cal: RobustnessCalculator<*, String>): Map<EquivClass<String>, Collection<RepTrace<String>>> {
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
    ): Map<EquivClass<String>, Collection<RepTrace<String>>> {
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

    private fun acyclicRepTraces(lts: LTS<Int, String>, prefix: Word<String>): Collection<RepTrace<String>> {
        val traces = mutableListOf<RepTrace<String>>()
        TSTraversal.breadthFirst(lts, lts.alphabet(), AcyclicTracesWithPrefixVisitor(lts, prefix, traces))
        return traces
    }
}

private class PathFromInitVisitor<S, I>(
    private val targets: Set<S>,
    private val result: MutableMap<S, Word<I>>
) : TSTraversalVisitor<S, I, S, Word<I>> {
    private val visited = mutableSetOf<S>()

    override fun processInitial(state: S, outData: Holder<Word<I>>): TSTraversalAction {
        outData.value = Word.epsilon()
        if (state in targets)
            result[state] = outData.value
        return TSTraversalAction.EXPLORE
    }

    override fun startExploration(state: S, data: Word<I>): Boolean {
        return if (state !in visited) {
            visited.add(state)
            true
        } else {
            false
        }
    }

    override fun processTransition(
        source: S,
        srcData: Word<I>,
        input: I,
        transition: S,
        succ: S,
        outData: Holder<Word<I>>
    ): TSTraversalAction {
        outData.value = Word.fromWords(srcData, Word.fromLetter(input))
        if (succ in targets && succ !in result)
            result[succ] = outData.value
        return if (result.keys.containsAll(targets))
            TSTraversalAction.ABORT_TRAVERSAL
        else
            TSTraversalAction.EXPLORE
    }
}

private class PrefixTrace<S, I>(val word: Word<I>, val visited: Set<S>)

private class AcyclicTracesWithPrefixVisitor<S, I>(
    private val lts: LTS<S, I>,
    private val prefix: Word<I>,
    private val result: MutableList<RepTrace<I>>
) : TSTraversalVisitor<S, I, S, PrefixTrace<S, I>> {
    override fun processInitial(state: S, outData: Holder<PrefixTrace<S, I>>): TSTraversalAction {
        outData.value = PrefixTrace(Word.epsilon(), emptySet())
        return TSTraversalAction.EXPLORE
    }

    override fun startExploration(state: S, data: PrefixTrace<S, I>): Boolean {
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
        source: S,
        srcData: PrefixTrace<S, I>,
        input: I,
        transition: S,
        succ: S,
        outData: Holder<PrefixTrace<S, I>>
    ): TSTraversalAction {
        return if (srcData.word.length() >= prefix.length() || prefix.getSymbol(srcData.word.length()) == input) {
            outData.value = PrefixTrace(Word.fromWords(srcData.word, Word.fromLetter(input)), srcData.visited + source)
            TSTraversalAction.EXPLORE
        } else {
            TSTraversalAction.ABORT_INPUT
        }
    }

    private fun noOutputTransition(state: S): Boolean {
        for (a in lts.alphabet()) {
            if (lts.getTransitions(state, a).isNotEmpty())
                return false
        }
        return true
    }
}