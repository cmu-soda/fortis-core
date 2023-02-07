package cmu.isr.robustify.oasis

import net.automatalib.automata.fsa.DFA
import net.automatalib.commons.util.Holder
import net.automatalib.util.ts.traversal.TSTraversal
import net.automatalib.util.ts.traversal.TSTraversalAction
import net.automatalib.util.ts.traversal.TSTraversalVisitor
import net.automatalib.words.Alphabet

fun <S1, S2, I> controlledEvents(plant: DFA<S1, I>, sup: DFA<S2, I>, inputs: Alphabet<I>): Collection<I> {
  val events = mutableSetOf<I>()
  TSTraversal.depthFirst(plant, inputs, ControlledEventsVisitor(sup, events))
  return events
}


private class ControlledEventsVisitor<S1, S2, I>(
  private val sup: DFA<S2, I>,
  private val events: MutableSet<I>
): TSTraversalVisitor<S1, I, S1, S2> {

  private val visited = mutableSetOf<S1>()

  override fun processInitial(state: S1, outData: Holder<S2>): TSTraversalAction {
    outData.value = sup.initialState
    return TSTraversalAction.EXPLORE
  }

  override fun startExploration(state: S1, data: S2): Boolean {
    return if (state !in visited) {
      visited.add(state)
      true
    } else {
      false
    }
  }

  override fun processTransition(
    source: S1,
    srcData: S2,
    input: I,
    transition: S1,
    succ: S1,
    outData: Holder<S2>
  ): TSTraversalAction {
    val supSucc = sup.getSuccessor(srcData, input)
    return if (supSucc != null) {
      outData.value = supSucc
      TSTraversalAction.EXPLORE
    } else {
      events.add(input)
      TSTraversalAction.IGNORE
    }
  }

}