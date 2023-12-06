package cmu.s3d.fortis.ts

import net.automatalib.alphabet.Alphabet
import net.automatalib.alphabet.Alphabets
import net.automatalib.automaton.concept.InputAlphabetHolder
import net.automatalib.automaton.fsa.CompactDFA
import net.automatalib.automaton.fsa.NFA
import net.automatalib.common.util.Holder
import net.automatalib.ts.UniversalDTS
import net.automatalib.util.automaton.builder.AutomatonBuilders
import net.automatalib.util.ts.copy.TSCopy
import net.automatalib.util.ts.traversal.TSTraversal
import net.automatalib.util.ts.traversal.TSTraversalAction
import net.automatalib.util.ts.traversal.TSTraversalMethod
import net.automatalib.util.ts.traversal.TSTraversalVisitor

fun <I> NFA<*, I>.alphabet(): Alphabet<I> {
    if (this is InputAlphabetHolder<*>) {
        return this.inputAlphabet as Alphabet<I>
    } else {
        error("Instance '${this.javaClass}' does not support getting alphabet")
    }
}

fun <S, I> NFA<S, I>.numOfTransitions(): Int {

    class TransitionVisitor(val result: Array<Int>) : TSTraversalVisitor<S, I, S, Void?> {
        private val visited = mutableSetOf<S>()
        override fun processInitial(state: S, outData: Holder<Void?>): TSTraversalAction {
            result[0] = 0
            return TSTraversalAction.EXPLORE
        }

        override fun startExploration(state: S, data: Void?): Boolean {
            return if (state !in visited) {
                visited.add(state)
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
            outData: Holder<Void?>
        ): TSTraversalAction {
            result[0]++
            return TSTraversalAction.EXPLORE
        }

    }

    val result = arrayOf(0)
    TSTraversal.depthFirst(this, this.alphabet(), TransitionVisitor(result))
    return result[0]
}

fun <I> makeProgress(input: I): CompactDFA<I> {
    val inputs = Alphabets.fromArray(input)
    return AutomatonBuilders.newDFA(inputs)
        .withInitial(0)
        .from(0).on(input).to(1)
        .from(1).on(input).to(1)
        .withAccepting(1)
        .create()
}

fun <S, I, T> extendAlphabet(
    lts: UniversalDTS<S, I, T, Boolean, Void?>,
    old: Alphabet<I>,
    extended: Alphabet<I>
): CompactDFA<I> {
    val out = CompactDFA(extended)
    TSCopy.copy(TSTraversalMethod.DEPTH_FIRST, lts, TSTraversal.NO_LIMIT, old, out)

    for (state in out) {
        for (a in extended - old) {
            out.addTransition(state, a, state, null)
        }
    }
    return out
}