package cmu.s3d.fortis.supervisory

import cmu.s3d.fortis.ts.DetLTS
import cmu.s3d.fortis.ts.LTS
import cmu.s3d.fortis.ts.alphabet
import cmu.s3d.fortis.ts.lts.asLTS
import net.automatalib.alphabet.Alphabet
import net.automatalib.automaton.concept.InputAlphabetHolder
import net.automatalib.automaton.fsa.CompactDFA
import net.automatalib.automaton.fsa.CompactNFA
import net.automatalib.automaton.fsa.NFA
import net.automatalib.util.ts.copy.TSCopy
import net.automatalib.util.ts.traversal.TSTraversal
import net.automatalib.util.ts.traversal.TSTraversalMethod

open class SupervisoryNFA<S, I>(
    private val nfa: NFA<S, I>,
    val controllable: Collection<I>,
    val observable: Collection<I>
) : NFA<S, I> by nfa, InputAlphabetHolder<I> {
    fun asNFA(): NFA<S, I> = nfa

    override fun getInputAlphabet(): Alphabet<I> {
        return nfa.alphabet()
    }
}

fun <S, I> NFA<S, I>.asSupNFA(controllable: Collection<I>, observable: Collection<I>): SupervisoryNFA<S, I> {
    if (!alphabet().containsAll(controllable) || !alphabet().containsAll(observable))
        error("controllable and observable should be subsets of the alphabet")
    return SupervisoryNFA(this, controllable, observable)
}

fun <S, I> SupervisoryNFA<S, I>.asDetLTS(): DetLTS<Int, I> {
    val out = CompactDFA(this.inputAlphabet)
    TSCopy.copy(TSTraversalMethod.DEPTH_FIRST, this, TSTraversal.NO_LIMIT, this.inputAlphabet, out)
    return out.asLTS()
}

fun <S, I> SupervisoryNFA<S, I>.asLTS(): LTS<Int, I> {
    val out = CompactNFA(this.inputAlphabet)
    TSCopy.copy(TSTraversalMethod.DEPTH_FIRST, this, TSTraversal.NO_LIMIT, this.inputAlphabet, out)
    return out.asLTS()
}