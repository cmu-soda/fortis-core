package cmu.isr.ts.lts

import cmu.isr.ts.LTS
import cmu.isr.ts.alphabet
import net.automatalib.commons.util.Holder
import net.automatalib.util.ts.traversal.TSTraversal
import net.automatalib.util.ts.traversal.TSTraversalAction
import net.automatalib.util.ts.traversal.TSTraversalVisitor
import net.automatalib.words.Word


class DeadlockResult<I> {
  var violation: Boolean = false
  var trace: Word<I>? = null

  override fun toString(): String {
    return if (violation) "Found deadlock: $trace" else "No deadlock"
  }
}

private class DeadlockVisitor<S, I>(
  private val lts: LTS<S, I>,
  private val result: DeadlockResult<I>
) : TSTraversalVisitor<S, I, S, Word<I>> {
  private val visited = mutableSetOf<S>()

  override fun processInitial(state: S, outData: Holder<Word<I>>?): TSTraversalAction {
    outData!!.value = Word.epsilon()
    return TSTraversalAction.EXPLORE
  }

  override fun startExploration(state: S, data: Word<I>?): Boolean {
    return if (state !in visited) {
      visited.add(state)
      true
    } else {
      false
    }
  }

  override fun processTransition(
    source: S,
    srcData: Word<I>?,
    input: I,
    transition: S,
    succ: S,
    outData: Holder<Word<I>>?
  ): TSTraversalAction {
    outData!!.value = srcData!!.append(input)
    if (!lts.isErrorState(succ) && noOutputTransition(succ)) {
      result.violation = true
      result.trace = outData.value
      return TSTraversalAction.ABORT_TRAVERSAL
    }
    return TSTraversalAction.EXPLORE
  }

  private fun noOutputTransition(state: S): Boolean {
    val res = true
    for (a in lts.alphabet()) {
      if (lts.getTransitions(state, a).isNotEmpty())
        return false
    }
    return res
  }

}

/**
 * Check the deadlock of a given LTS.
 */
fun <S, I> checkDeadlock(lts: LTS<S, I>): DeadlockResult<I> {
  val result = DeadlockResult<I>()
  val vis = DeadlockVisitor(lts, result)
  TSTraversal.breadthFirst(lts, lts.alphabet(), vis)
  return result
}
