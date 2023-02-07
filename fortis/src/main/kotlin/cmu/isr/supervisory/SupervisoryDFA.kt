package cmu.isr.supervisory

import cmu.isr.ts.alphabet
import net.automatalib.automata.fsa.DFA

class SupervisoryDFA<S, I>(
  private val dfa: DFA<S, I>,
  controllable: Collection<I>,
  observable: Collection<I>
) : SupervisoryNFA<S, I>(dfa, controllable, observable), DFA<S, I> by dfa {
  override fun getInitialStates(): Set<S> {
    return super<SupervisoryNFA>.getInitialStates()
  }

  override fun getTransitions(state: S, input: I): Collection<S> {
    return super<SupervisoryNFA>.getTransitions(state, input)
  }

  fun asDFA(): DFA<S, I> = dfa

  override fun getStateProperty(state: S): Boolean {
    return dfa.getStateProperty(state)
  }

  override fun getStates(): MutableCollection<S> {
    return dfa.states
  }

  override fun getSuccessor(transition: S): S {
    return dfa.getSuccessor(transition)
  }

  override fun getSuccessor(state: S, input: I): S? {
    return dfa.getSuccessor(state, input)
  }

  override fun getTransitionProperty(transition: S): Void? {
    return dfa.getTransitionProperty(transition)
  }

  override fun isAccepting(state: S): Boolean {
    return dfa.isAccepting(state)
  }
}

fun <S, I> DFA<S, I>.asSupDFA(controllable: Collection<I>, observable: Collection<I>): SupervisoryDFA<S, I> {
  if (!alphabet().containsAll(controllable) || !alphabet().containsAll(observable))
    error("controllable and observable should be subsets of the alphabet")
  return SupervisoryDFA(this, controllable, observable)
}