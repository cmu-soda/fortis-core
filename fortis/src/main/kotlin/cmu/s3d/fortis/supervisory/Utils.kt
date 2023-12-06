package cmu.s3d.fortis.supervisory

import cmu.s3d.fortis.ts.nfa.hide
import net.automatalib.alphabet.Alphabet


/**
 * @param dfa the DFA to be observed
 * @param inputs the alphabet of the DFA
 */
fun <S, I> observer(dfa: SupervisoryDFA<S, I>, inputs: Alphabet<I>): SupervisoryDFA<Int, I> {
    val out = hide(dfa, inputs - dfa.observable.toSet())
    return out.asSupDFA(dfa.controllable intersect dfa.observable.toSet(), dfa.observable)
}
