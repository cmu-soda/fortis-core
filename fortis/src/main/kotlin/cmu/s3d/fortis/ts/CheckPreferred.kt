package cmu.s3d.fortis.ts

import cmu.s3d.fortis.ts.nfa.NFAParallelComposition
import net.automatalib.alphabet.Alphabets
import net.automatalib.automaton.fsa.DFA
import net.automatalib.common.util.Holder
import net.automatalib.util.automaton.builder.AutomatonBuilders
import net.automatalib.util.ts.traversal.TSTraversal
import net.automatalib.util.ts.traversal.TSTraversalAction
import net.automatalib.util.ts.traversal.TSTraversalVisitor
import net.automatalib.word.Word


/**
 *
 */
fun <I> acceptsSubWord(dfa: DFA<*, I>, word: Word<I>, inputs: Collection<I>? = null): Pair<Boolean, List<I>> {
    // build automata from the word
    val builder = AutomatonBuilders.newDFA(Alphabets.fromCollection(inputs ?: word.distinct()))
        .withInitial(0)
        .withAccepting(0)
    var s = 0
    for (input in word) {
        builder.from(s).on(input).to(++s).withAccepting(s)
    }
    val wordDFA = builder.create()

    val composition = NFAParallelComposition(dfa, wordDFA)
    val result = booleanArrayOf(false)
    val trace = mutableListOf<I>()
    TSTraversal.depthFirst(
        composition, dfa.alphabet() union wordDFA.inputAlphabet,
        AcceptsSubWordVisitor(wordDFA, result, trace)
    )

    return Pair(result[0], trace)
}


private class AcceptsSubWordVisitor<S1, S2, I, T>(
    private val wordDFA: DFA<S2, I>,
    private val result: BooleanArray,
    private val how: MutableList<I>
) : TSTraversalVisitor<Pair<S1, S2>, I, T, List<I>> {

    private val visited = mutableSetOf<Pair<S1, S2>>()
    private val visitedS2 = mutableSetOf<S2>()

    override fun processInitial(state: Pair<S1, S2>, outData: Holder<List<I>>): TSTraversalAction {
        outData.value = emptyList()
        return TSTraversalAction.EXPLORE
    }

    override fun startExploration(state: Pair<S1, S2>, data: List<I>): Boolean {
        return if (state !in visited) {
            visited.add(state)
            visitedS2.add(state.second)
            true
        } else {
            false
        }
    }

    override fun processTransition(
        source: Pair<S1, S2>,
        srcData: List<I>,
        input: I,
        transition: T,
        succ: Pair<S1, S2>,
        outData: Holder<List<I>>
    ): TSTraversalAction {
        visitedS2.add(succ.second)
        outData.value = srcData + input
        return if (visitedS2.size == wordDFA.size()) {
            result[0] = true
            how.addAll(outData.value)
            TSTraversalAction.ABORT_TRAVERSAL
        } else {
            TSTraversalAction.EXPLORE
        }
    }

}