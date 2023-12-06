package cmu.s3d.fortis.ts

import net.automatalib.common.util.Holder
import net.automatalib.util.ts.traversal.TSTraversalAction
import net.automatalib.util.ts.traversal.TSTraversalVisitor
import net.automatalib.word.Word

class PathFromInitVisitor<S, I>(
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