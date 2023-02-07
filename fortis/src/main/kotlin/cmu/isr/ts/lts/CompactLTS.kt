package cmu.isr.ts.lts

import cmu.isr.ts.MutableLTS
import net.automatalib.automata.concepts.InputAlphabetHolder
import net.automatalib.automata.fsa.impl.compact.CompactNFA
import net.automatalib.words.Alphabet

class CompactLTS<I>(private val nfa: CompactNFA<I>) : MutableLTS<Int, I>, InputAlphabetHolder<I> {

  private val _errorState: Int

  init {
    // Check that there should be at most one error state, that is marked as unacceptable.
    val unacceptable = states.filter { !isAccepting(it) }
    if (unacceptable.size > 1)
      error("There should be one error state in LTS which might be unreachable.")
    _errorState = if (unacceptable.isEmpty())
      addState(false)
    else
      unacceptable[0]
  }

  override val errorState: Int
    get() = _errorState

  override fun getStates(): Collection<Int> {
    return nfa.states
  }

  override fun getInitialStates(): Set<Int> {
    return nfa.initialStates
  }

  override fun clear() {
    nfa.clear()
  }

  override fun addState(accepting: Boolean): Int {
    return nfa.addState(accepting)
  }

  override fun setAccepting(state: Int?, accepting: Boolean) {
    nfa.setAccepting(state, accepting)
  }

  override fun removeAllTransitions(state: Int?) {
    nfa.removeAllTransitions(state)
  }

  override fun setTransitions(state: Int?, input: I, transitions: MutableCollection<out Int>?) {
    nfa.setTransitions(state, input, transitions)
  }

  override fun setInitial(state: Int?, initial: Boolean) {
    nfa.setInitial(state, initial)
  }

  override fun isAccepting(state: Int?): Boolean {
    return nfa.isAccepting(state)
  }

  override fun getTransitions(state: Int?, input: I): Collection<Int> {
    return nfa.getTransitions(state, input)
  }

  override fun isErrorState(state: Int): Boolean {
    return !isAccepting(state)
  }

  override fun getInputAlphabet(): Alphabet<I> {
    return nfa.inputAlphabet
  }

}

fun <I> CompactNFA<I>.asLTS(): CompactLTS<I> {
  return CompactLTS(this)
}