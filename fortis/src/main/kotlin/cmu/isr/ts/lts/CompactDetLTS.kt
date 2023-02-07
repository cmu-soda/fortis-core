package cmu.isr.ts.lts

import cmu.isr.ts.MutableDetLTS
import net.automatalib.automata.concepts.InputAlphabetHolder
import net.automatalib.automata.fsa.impl.compact.CompactDFA
import net.automatalib.words.Alphabet

class CompactDetLTS<I>(private val dfa: CompactDFA<I>) : MutableDetLTS<Int, I>, InputAlphabetHolder<I> {

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
    return dfa.states
  }

  override fun getInitialState(): Int? {
    return dfa.initialState
  }

  override fun clear() {
    dfa.clear()
  }

  override fun addState(accepting: Boolean): Int {
    return dfa.addState(accepting)
  }

  override fun setAccepting(state: Int?, accepting: Boolean) {
    dfa.setAccepting(state, accepting)
  }

  override fun setTransition(state: Int?, input: I, transition: Int?) {
    dfa.setTransition(state, input, transition)
  }

  override fun setInitialState(state: Int?) {
    dfa.initialState = state
  }

  override fun removeAllTransitions(state: Int?) {
    dfa.removeAllTransitions(state)
  }

  override fun getTransition(state: Int?, input: I): Int? {
    return dfa.getTransition(state, input)
  }

  override fun isAccepting(state: Int?): Boolean {
    return dfa.isAccepting(state)
  }

  override fun isErrorState(state: Int): Boolean {
    return !isAccepting(state)
  }

  override fun getInputAlphabet(): Alphabet<I> {
    return dfa.inputAlphabet
  }

}

/**
 * Convert a CompactDFA to a CompactDetLTS.
 */
fun <I> CompactDFA<I>.asLTS(): CompactDetLTS<I> {
  return CompactDetLTS(this)
}
