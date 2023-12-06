package cmu.s3d.fortis.supervisory

import cmu.s3d.fortis.ts.alphabet
import net.automatalib.alphabet.Alphabet
import net.automatalib.automaton.concept.InputAlphabetHolder
import net.automatalib.automaton.fsa.NFA

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