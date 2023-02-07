package cmu.isr.ts

import net.automatalib.automata.fsa.NFA
import net.automatalib.commons.util.Holder
import net.automatalib.util.ts.traversal.TSTraversal
import net.automatalib.util.ts.traversal.TSTraversalAction
import net.automatalib.util.ts.traversal.TSTraversalVisitor

data class Predecessor<S, T, I>(val transition: T, val source: S, val input: I)

class Predecessors<S, I>(nfa: NFA<S, I>) {

  private val map: MutableMap<Pair<S, I>, MutableList<Predecessor<S, S, I>>> = mutableMapOf()

  private inner class PredecessorsVisitor : TSTraversalVisitor<S, I, S, Void?> {
    private val visited = mutableSetOf<S>()

    override fun processInitial(state: S, outData: Holder<Void?>): TSTraversalAction {
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
      if (Pair(succ, input) in map) {
        map[Pair(succ, input)]!!.add(Predecessor(transition, source, input))
      } else {
        map[Pair(succ, input)] = mutableListOf(Predecessor(transition, source, input))
      }
      return TSTraversalAction.EXPLORE
    }

  }

  init {
    TSTraversal.breadthFirst(nfa, nfa.alphabet(), PredecessorsVisitor())
  }

  fun getPredecessors(state: S, input: I): Collection<Predecessor<S, S, I>> {
    return map[Pair(state, input)]?: emptyList()
  }
}