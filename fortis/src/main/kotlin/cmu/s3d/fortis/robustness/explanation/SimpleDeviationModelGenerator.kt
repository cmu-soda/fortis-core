package cmu.s3d.fortis.robustness.explanation

import cmu.s3d.fortis.ts.LTS
import cmu.s3d.fortis.ts.alphabet
import cmu.s3d.fortis.ts.lts.asLTS
import net.automatalib.alphabet.Alphabet
import net.automatalib.automaton.fsa.CompactNFA
import net.automatalib.common.util.Holder
import net.automatalib.util.ts.copy.TSCopy
import net.automatalib.util.ts.traversal.TSTraversal
import net.automatalib.util.ts.traversal.TSTraversalAction
import net.automatalib.util.ts.traversal.TSTraversalMethod
import net.automatalib.util.ts.traversal.TSTraversalVisitor
import net.automatalib.word.Word

class SimpleDeviationModelGenerator(
    private val errModel: LTS<Int, String>,
    private val faults: Collection<String>
) : DeviationModelGenerator {
    override fun fromDeviations(traces: Collection<Word<String>>, inputs: Alphabet<String>): LTS<Int, String> {
        if (!inputs.containsAll(errModel.alphabet()))
            error("The alphabet of the deviations should be a superset of the alphabet of the deviation model.")

        // Make an mutable copy
        val copy = CompactNFA(errModel.alphabet())
        TSCopy.copy(TSTraversalMethod.DEPTH_FIRST, errModel, TSTraversal.NO_LIMIT, errModel.alphabet(), copy)

        // Find all visited faulty transitions given the traecs
        val visitedFaultyTransition = mutableSetOf<Int>()
        for (trace in traces) {
            TSTraversal.breadthFirst(
                copy, copy.alphabet(),
                DeviationModelGenVisitor(trace, copy.alphabet(), faults, visitedFaultyTransition)
            )
        }
        // Remove all the faulty transitions
        for (state in copy) {
            for (fault in faults) {
                val faultyTransitions = copy.getTransitions(state, fault)
                for (t in faultyTransitions) {
                    if (t !in visitedFaultyTransition)
                        copy.removeTransition(state, fault, t)
                }
            }
        }
        return copy.asLTS()
    }
}

private class DeviationModelGenVisitor(
    val trace: Word<String>,
    val inputs: Alphabet<String>,
    val faults: Collection<String>,
    val visitedFaultyTransition: MutableSet<Int>,
) : TSTraversalVisitor<Int, String, Int, Word<String>> {
    override fun processInitial(state: Int, outData: Holder<Word<String>>): TSTraversalAction {
        var cur = trace
        while (!cur.isEmpty && cur.getSymbol(0) !in inputs)
            cur = cur.subWord(1)
        outData.value = cur
        return TSTraversalAction.EXPLORE
    }

    override fun startExploration(state: Int, data: Word<String>): Boolean {
        return !data.isEmpty
    }

    override fun processTransition(
        source: Int,
        srcData: Word<String>,
        input: String,
        transition: Int,
        succ: Int,
        outData: Holder<Word<String>>
    ): TSTraversalAction {
        if (srcData.getSymbol(0) !in inputs)
            error("Unexpected State: the input symbol is not in the alphabet of the deviation model.")

        if (srcData.getSymbol(0) == input) {
            if (input in faults)
                visitedFaultyTransition.add(transition)

            var cur = srcData.subWord(1)
            while (!cur.isEmpty && cur.getSymbol(0) !in inputs)
                cur = cur.subWord(1)

            outData.value = cur
            return TSTraversalAction.EXPLORE
        }

        return TSTraversalAction.ABORT_INPUT
    }

}