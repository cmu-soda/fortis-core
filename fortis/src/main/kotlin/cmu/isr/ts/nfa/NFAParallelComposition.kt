package cmu.isr.ts.nfa

import cmu.isr.ts.alphabet
import net.automatalib.automata.fsa.NFA
import net.automatalib.automata.fsa.impl.compact.CompactNFA
import net.automatalib.ts.UniversalTransitionSystem
import net.automatalib.util.ts.copy.TSCopy
import net.automatalib.util.ts.traversal.TSTraversal
import net.automatalib.util.ts.traversal.TSTraversalMethod
import net.automatalib.words.impl.Alphabets

open class NFAParallelComposition<S1, S2, I>(
  private val nfa1: NFA<S1, I>,
  private val nfa2: NFA<S2, I>
) : UniversalTransitionSystem<Pair<S1, S2>, I, Pair<S1, S2>, Boolean, Void?> {

  override fun getInitialStates(): Set<Pair<S1, S2>> {
    return nfa1.initialStates.flatMap { s1 -> nfa2.initialStates.map { s2 -> Pair(s1, s2) } }.toSet()
  }

  override fun getTransitionProperty(transition: Pair<S1, S2>?): Void? {
    return null
  }

  override fun getStateProperty(state: Pair<S1, S2>): Boolean {
    return nfa1.getStateProperty(state.first) && nfa2.getStateProperty(state.second)
  }

  override fun getSuccessor(transition: Pair<S1, S2>): Pair<S1, S2> {
    return transition
  }

  override fun getTransitions(state: Pair<S1, S2>, input: I): Collection<Pair<S1, S2>> {
    val s1 = state.first
    val s2 = state.second
    val inputs1 = nfa1.alphabet()
    val inputs2 = nfa2.alphabet()
    return when {
      (input in inputs1 && input in inputs2) -> {
        val t1s = nfa1.getTransitions(s1, input)
        val t2s = nfa2.getTransitions(s2, input)
        if (t1s.isEmpty() || t2s.isEmpty())
          emptyList()
        else
          t1s.flatMap { t1 -> t2s.map { t2 -> Pair(t1, t2) } }
      }
      (input in inputs1) -> {
        val t1s = nfa1.getTransitions(s1, input)
        if (t1s.isEmpty())
          emptyList()
        else
          t1s.map { t1 -> Pair(t1, s2) }
      }
      else -> {
        val t2s = nfa2.getTransitions(s2, input)
        if (t2s.isEmpty())
          emptyList()
        else
          t2s.map { t2 -> Pair(s1, t2) }
      }
    }
  }

}

fun <I> parallelComposition(nfa1: NFA<*, I>, nfa2: NFA<*, I>): NFA<Int, I> {
  val inputs = Alphabets.fromCollection(nfa1.alphabet() union nfa2.alphabet())
  val out = CompactNFA(inputs)
  val composition = NFAParallelComposition(nfa1, nfa2)

  TSCopy.copy(TSTraversalMethod.DEPTH_FIRST, composition, TSTraversal.NO_LIMIT, inputs, out)
  return out
}