package cmu.s3d.fortis.ts

import net.automatalib.common.util.Holder
import net.automatalib.util.ts.traversal.TSTraversalAction
import net.automatalib.util.ts.traversal.TSTraversalVisitor
import net.automatalib.word.Word

class AllStatesPathsFromInitVisitor(
    private val targets: Set<Int>,
    private val errReach: Set<Int>,
    private val result: MutableMap<Int, MutableSet<Word<String>>>
) : TSTraversalVisitor<Int, String, Int, Pair<Word<String>,Set<Int>>> {
    private val resultTracesVisited: MutableSet<Int> = mutableSetOf()

    private fun addResult(state: Int, trace: Word<String>, traceVisited: Set<Int>) {
        assert(state in traceVisited)
        if (state !in result) {
            assert(state !in resultTracesVisited)
            result[state] = mutableSetOf()
        }
        // only add the trace if it adds new information (new, unexplored states)
        if (!resultTracesVisited.containsAll(traceVisited)) {
            result[state]?.add(trace)
            resultTracesVisited.addAll(traceVisited)
        }
    }

    override fun processInitial(state: Int, outData: Holder<Pair<Word<String>,Set<Int>>>): TSTraversalAction {
        outData.value = Pair(Word.epsilon(), setOf(state))
        if (state in targets)
            addResult(state, outData.value.first, outData.value.second)
        return TSTraversalAction.EXPLORE
    }

    override fun startExploration(state: Int, data: Pair<Word<String>,Set<Int>>): Boolean {
        return state in errReach
    }

    override fun processTransition(
        source: Int,
        srcData: Pair<Word<String>,Set<Int>>,
        input: String,
        transition: Int,
        succ: Int,
        outData: Holder<Pair<Word<String>,Set<Int>>>
    ): TSTraversalAction {
        // speed optimization: ignore self loops
        if (succ == source) {
            return TSTraversalAction.IGNORE
        }

        outData.value = Pair(Word.fromWords(srcData.first, Word.fromLetter(input)), srcData.second.plus(succ))
        if (succ in targets)
            addResult(succ, outData.value.first, outData.value.second)
        return if (resultTracesVisited.containsAll(errReach))
            TSTraversalAction.ABORT_TRAVERSAL
        else
            TSTraversalAction.EXPLORE
    }
}
