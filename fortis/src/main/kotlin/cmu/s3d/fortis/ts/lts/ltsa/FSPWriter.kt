package cmu.s3d.fortis.ts.lts.ltsa

import cmu.s3d.fortis.ts.LTS
import net.automatalib.alphabet.Alphabet
import net.automatalib.automaton.fsa.NFA
import net.automatalib.common.util.Holder
import net.automatalib.util.ts.traversal.TSTraversal
import net.automatalib.util.ts.traversal.TSTraversalAction
import net.automatalib.util.ts.traversal.TSTraversalVisitor
import java.io.OutputStream

fun <S, I> writeFSP(output: OutputStream, nfa: NFA<S, I>, inputs: Alphabet<I>) {
    val builder = StringBuilder()
    val writer = output.writer()
    TSTraversal.breadthFirst(nfa, inputs, FSPWriterVisitor(builder, nfa, inputs))
    if (builder.endsWith(" | ")) {
        builder.setLength(builder.length - 3)
        builder.appendLine(").")
    }
    writer.write(builder.toString())
    writer.flush()
}

private class FSPWriterVisitor<S, I>(
    val builder: StringBuilder,
    val nfa: NFA<S, I>,
    val inputs: Alphabet<I>
) : TSTraversalVisitor<S, I, S, Void?> {
    private val visited = mutableSetOf<S>()

    override fun processInitial(state: S, outData: Holder<Void?>): TSTraversalAction {
        return TSTraversalAction.EXPLORE
    }

    override fun startExploration(state: S, data: Void?): Boolean {
        return if (state !in visited) {
            visited.add(state)
            if (builder.endsWith(" | ")) {
                builder.setLength(builder.length - 3)
                builder.appendLine("),")
            }
            builder.append("S$state = (")
            true
        } else {
            false
        }
    }

    override fun processTransition(
        source: S,
        srcData: Void?,
        input: I,
        transition: S,
        succ: S,
        outData: Holder<Void?>?
    ): TSTraversalAction {
        // check deadlock state
        var isDeadlock = true
        for (a in inputs) {
            if (nfa.getTransitions(succ, a).isNotEmpty()) {
                isDeadlock = false
                break
            }
        }
        val action = LTSACall.escapeEvent(input.toString())
        return if (nfa is LTS<*, *> && !nfa.isAccepting(succ)) {
            builder.append("$action -> ERROR | ")
            TSTraversalAction.IGNORE
        } else if (isDeadlock) {
            builder.append("$action -> STOP | ")
            TSTraversalAction.IGNORE
        } else {
            builder.append("$action -> S$succ | ")
            TSTraversalAction.EXPLORE
        }
    }

}