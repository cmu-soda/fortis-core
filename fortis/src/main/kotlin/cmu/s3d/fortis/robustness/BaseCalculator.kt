package cmu.s3d.fortis.robustness

import cmu.s3d.fortis.assumption.SubsetConstructionGenerator
import cmu.s3d.fortis.assumption.WeakestAssumptionGenerator
import cmu.s3d.fortis.common.EquivClass
import cmu.s3d.fortis.common.RepTrace
import cmu.s3d.fortis.common.RobustnessOptions
import cmu.s3d.fortis.ts.*
import cmu.s3d.fortis.ts.lts.hide
import cmu.s3d.fortis.ts.lts.makeErrorState
import net.automatalib.alphabet.Alphabets
import net.automatalib.automaton.fsa.CompactNFA
import net.automatalib.automaton.fsa.NFA
import net.automatalib.common.util.Holder
import net.automatalib.util.automaton.builder.AutomatonBuilders
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

    fun computeBoundedUnsafeBeh(
        bound: Int
    ): Set<Word<String>> {
        logger.info("Generating unsafe behavior representation traces by equivalence classes...")
        val m = waGenerator.generateUnsafe()
        val traces = boundedDeltaTraces(bound, m)
        if (traces.isEmpty())
            logger.info("No representation traces found. The system is safe under any environment.")
        return traces
    }

    fun computeAllStatesUnsafeBeh(): Set<Word<String>> {
        logger.info("Generating unsafe behavior representation traces by equivalence classes...")
        val m = waGenerator.generateUnsafe()
        val traces = deltaTracesAllStates(m)
        if (traces.isEmpty())
            logger.info("No representation traces found. The system is safe under any environment.")
        return traces
    }

    override fun computeEnvUnsafeBeh(): Map<EquivClass, Collection<RepTrace>> {
        logger.info("Generating env unsafe behavior representation traces by equivalence classes...")
        val m = waGenerator.generateEnvUnsafe()
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


    private fun boundedDeltaTraces(
        bound: Int,
        delta: DetLTS<Int, String>
    ): Set<Word<String>> {
        val predecessors = Predecessors(delta)
        val transToError = delta.alphabet().flatMap { predecessors.getPredecessors(delta.errorState, it) }
        val statesToError = transToError.map { it.source }.toSet()
        if (statesToError.isEmpty())
            return emptySet()
        val traces = mutableMapOf<Int, MutableSet<Word<String>>>()
        TSTraversal.breadthFirst(delta, delta.alphabet(), BoundedPathsFromInitVisitor(bound, statesToError, traces))
        val traceSet = mutableSetOf<Word<String>>()
        transToError.forEach { (_, source, a) ->
            traces[source]?.forEach { t -> traceSet.add(Word.fromWords(t, Word.fromLetter(a))) }
        }
        return traceSet
    }

    private fun reverseNFA(orig: NFA<Int, String>, initStates: Set<Int>) : NFA<Int,String> {
        val reversed = AutomatonBuilders.newNFA(orig.alphabet()).create()
        val allStates = orig.states.sorted()
        val correct = (0..allStates.last()).toList()
        assert(allStates == correct)
        for (state in allStates) {
            if (state in initStates) {
                reversed.addInitialState()
            } else {
                reversed.addState()
            }
        }
        for (src in allStates) {
            for (a in orig.alphabet()) {
                for (dst in orig.getTransitions(src, a)) {
                    reversed.addTransition(dst, a, src)
                }
            }
        }
        return reversed
    }

    private fun predFix(init: Set<Int>, lts: LTS<Int,String>, pred: Predecessors<Int,String>) : Set<Int> {
        val set = init.toMutableSet()
        while (true) {
            val predecessors = set.flatMap { state ->
                lts.alphabet().flatMap { a -> pred.getPredecessors(state, a) }
            }.map { it.source }.toSet()
            if (set.containsAll(predecessors)) {
                // reached a fix point
                return set
            }
            set.addAll(predecessors)
        }
    }

    private fun deltaTracesAllStates(
        delta: DetLTS<Int, String>
    ): Set<Word<String>> {
        val predecessors = Predecessors(delta)
        val transToError = delta.alphabet().flatMap { predecessors.getPredecessors(delta.errorState, it) }
        val statesToError = transToError.map { it.source }.toSet()
        if (statesToError.isEmpty())
            return emptySet()
        val errorReach = predFix(statesToError, delta, predecessors)
        val traceClasses = mutableMapOf<Int, MutableSet<Word<String>>>()
        TSTraversal.breadthFirst(delta, delta.alphabet(), AllStatesPathsFromInitVisitor(statesToError, errorReach, traceClasses))
        val traces = mutableSetOf<Word<String>>()
        transToError.forEach { (_, source, a) ->
            traceClasses[source]?.forEach { t -> traces.add(Word.fromWords(t, Word.fromLetter(a))) }
        }
        return traces
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