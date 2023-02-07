package cmu.isr.ts

import net.automatalib.automata.concepts.InputAlphabetHolder
import net.automatalib.automata.fsa.NFA
import net.automatalib.commons.util.Holder
import net.automatalib.util.ts.traversal.TSTraversal
import net.automatalib.util.ts.traversal.TSTraversalAction
import net.automatalib.util.ts.traversal.TSTraversalVisitor
import net.automatalib.words.Alphabet

fun <I> NFA<*, I>.alphabet(): Alphabet<I> {
  if (this is InputAlphabetHolder<*>) {
    return this.inputAlphabet as Alphabet<I>
  } else {
    error("Instance '${this.javaClass}' does not support getting alphabet")
  }
}

fun <S, I> NFA<S, I>.numOfTransitions(): Int {

  class TransitionVisitor(val result: Array<Int>): TSTraversalVisitor<S, I, S, Void?> {
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