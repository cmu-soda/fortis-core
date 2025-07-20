package cmu.s3d.fortis.ts

import net.automatalib.common.util.Holder
import net.automatalib.util.ts.traversal.TSTraversalAction
import net.automatalib.util.ts.traversal.TSTraversalVisitor
import net.automatalib.word.Word

class BoundedPathsFromInitVisitor(
    private val bound: Int,
    private val targets: Set<Int>,
    private val result: MutableMap<Int, MutableSet<Word<String>>>
) : TSTraversalVisitor<Int, String, Int, Word<String>> {
    private fun addResult(state: Int, trace: Word<String>) {
        if (state !in result) {
            result[state] = mutableSetOf()
        }
        result[state]?.add(trace)
    }

    override fun processInitial(state: Int, outData: Holder<Word<String>>): TSTraversalAction {
        outData.value = Word.epsilon()
        if (state in targets)
            addResult(state, outData.value)
        return TSTraversalAction.EXPLORE
    }

    override fun startExploration(state: Int, data: Word<String>): Boolean {
        return data.size() < bound
    }

    override fun processTransition(
        source: Int,
        srcData: Word<String>,
        input: String,
        transition: Int,
        succ: Int,
        outData: Holder<Word<String>>
    ): TSTraversalAction {
        //val transitionInLTS = succ in lts.getTransitions(source, input)
        val selfLoop = succ == source
        if (!selfLoop) {
            outData.value = Word.fromWords(srcData, Word.fromLetter(input))
        }
        if (outData.value == null) {
            return TSTraversalAction.IGNORE
        }
        if (succ in targets)
            addResult(succ, outData.value)
        return if (outData.value.size() < bound && !selfLoop)
            TSTraversalAction.EXPLORE
        else
            TSTraversalAction.IGNORE
    }
}
