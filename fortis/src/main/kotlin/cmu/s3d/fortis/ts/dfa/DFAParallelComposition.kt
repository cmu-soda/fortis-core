package cmu.s3d.fortis.ts.dfa

import cmu.s3d.fortis.ts.alphabet
import cmu.s3d.fortis.ts.nfa.NFAParallelComposition
import net.automatalib.alphabet.Alphabets
import net.automatalib.automaton.fsa.CompactDFA
import net.automatalib.automaton.fsa.DFA
import net.automatalib.util.ts.copy.TSCopy
import net.automatalib.util.ts.traversal.TSTraversal
import net.automatalib.util.ts.traversal.TSTraversalMethod

fun <I> parallelComposition(dfa1: DFA<*, I>, dfa2: DFA<*, I>): DFA<Int, I> {
    val inputs = Alphabets.fromCollection(dfa1.alphabet() union dfa2.alphabet())
    val out = CompactDFA(inputs)
    val composition = NFAParallelComposition(dfa1, dfa2)

    TSCopy.copy(TSTraversalMethod.DEPTH_FIRST, composition, TSTraversal.NO_LIMIT, inputs, out)
    return out
}