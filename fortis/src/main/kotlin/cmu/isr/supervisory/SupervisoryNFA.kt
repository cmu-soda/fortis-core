package cmu.isr.supervisory

import cmu.isr.ts.alphabet
import net.automatalib.automata.concepts.InputAlphabetHolder
import net.automatalib.automata.fsa.NFA
import net.automatalib.words.Alphabet

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