package cmu.s3d.fortis.ts.lts

import cmu.s3d.fortis.ts.DetLTS
import cmu.s3d.fortis.ts.LTS
import cmu.s3d.fortis.ts.alphabet
import cmu.s3d.fortis.ts.nfa.NFAParallelComposition
import net.automatalib.alphabet.Alphabets
import net.automatalib.automaton.fsa.CompactDFA
import net.automatalib.automaton.fsa.CompactNFA
import net.automatalib.util.ts.copy.TSCopy
import net.automatalib.util.ts.traversal.TSTraversal
import net.automatalib.util.ts.traversal.TSTraversalMethod

class LTSParallelComposition<S1, S2, I>(
    private val lts1: LTS<S1, I>,
    private val lts2: LTS<S2, I>
) : NFAParallelComposition<S1, S2, I>(lts1, lts2) {

    override fun getSuccessor(transition: Pair<S1, S2>): Pair<S1, S2> {
        return if (getStateProperty(transition)) transition else Pair(lts1.errorState, lts2.errorState)
    }

}

fun <I> parallelComposition(lts1: LTS<*, I>, lts2: LTS<*, I>): LTS<Int, I> {
    val inputs = Alphabets.fromCollection(lts1.alphabet() union lts2.alphabet())
    val out = CompactNFA(inputs)
    val composition = LTSParallelComposition(lts1, lts2)

    TSCopy.copy(TSTraversalMethod.DEPTH_FIRST, composition, TSTraversal.NO_LIMIT, inputs, out)
    return out.asLTS()
}

fun <I> parallelComposition(lts1: DetLTS<*, I>, lts2: DetLTS<*, I>): DetLTS<Int, I> {
    val inputs = Alphabets.fromCollection(lts1.alphabet() union lts2.alphabet())
    val out = CompactDFA(inputs)
    val composition = LTSParallelComposition(lts1, lts2)

    TSCopy.copy(TSTraversalMethod.DEPTH_FIRST, composition, TSTraversal.NO_LIMIT, inputs, out)
    return out.asLTS()
}