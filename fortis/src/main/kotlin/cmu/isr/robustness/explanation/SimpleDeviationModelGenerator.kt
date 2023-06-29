package cmu.isr.robustness.explanation

import cmu.isr.ts.LTS
import cmu.isr.ts.alphabet
import cmu.isr.ts.lts.asLTS
import net.automatalib.automata.fsa.impl.compact.CompactNFA
import net.automatalib.commons.util.Holder
import net.automatalib.util.ts.copy.TSCopy
import net.automatalib.util.ts.traversal.TSTraversal
import net.automatalib.util.ts.traversal.TSTraversalAction
import net.automatalib.util.ts.traversal.TSTraversalMethod
import net.automatalib.util.ts.traversal.TSTraversalVisitor
import net.automatalib.words.Alphabet
import net.automatalib.words.Word

class SimpleDeviationModelGenerator<I>(
  val errModel: LTS<Int, I>,
  val faults: Collection<I>
) : DeviationModelGenerator<Int, I> {
  override fun fromDeviations(traces: Collection<Word<I>>, inputs: Alphabet<I>): LTS<Int, I> {
    if (!inputs.containsAll(errModel.alphabet()))
      error("The alphabet of the deviations should be a superset of the alphabet of the deviation model.")

    // Make an mutable copy
    val copy = CompactNFA(errModel.alphabet())
    TSCopy.copy(TSTraversalMethod.DEPTH_FIRST, errModel, TSTraversal.NO_LIMIT, errModel.alphabet(), copy)

    // Find all visited faulty transitions given the traecs
    val visitedFaultyTransition = mutableSetOf<Int>()
    for (trace in traces) {
      TSTraversal.breadthFirst(
        copy, copy.alphabet(),
        DeviationModelGenVisitor(trace, copy.alphabet(), faults, visitedFaultyTransition)
      )
    }
    // Remove all the faulty transitions
    for (state in copy) {
      for (fault in faults) {
        val faultyTransitions = copy.getTransitions(state, fault)
        for (t in faultyTransitions) {
          if (t !in visitedFaultyTransition)
            copy.removeTransition(state, fault, t)
        }
      }
    }
    return copy.asLTS()
  }
}

private class DeviationModelGenVisitor<S, I>(
  val trace: Word<I>,
  val inputs: Alphabet<I>,
  val faults: Collection<I>,
  val visitedFaultyTransition: MutableSet<S>,
) : TSTraversalVisitor<S, I, S, Word<I>> {
  override fun processInitial(state: S, outData: Holder<Word<I>>): TSTraversalAction {
    var cur = trace
    while (!cur.isEmpty && cur.getSymbol(0) !in inputs)
      cur = cur.subWord(1)
    outData.value = cur
    return TSTraversalAction.EXPLORE
  }

  override fun startExploration(state: S, data: Word<I>): Boolean {
    return !data.isEmpty
  }

  override fun processTransition(
    source: S,
    srcData: Word<I>,
    input: I,
    transition: S,
    succ: S,
    outData: Holder<Word<I>>
  ): TSTraversalAction {
    if (srcData.getSymbol(0) !in inputs)
      error("Unexpected State: the input symbol is not in the alphabet of the deviation model.")

    if (srcData.getSymbol(0) == input) {
      if (input in faults)
        visitedFaultyTransition.add(transition)

      var cur = srcData.subWord(1)
      while (!cur.isEmpty && cur.getSymbol(0) !in inputs)
        cur = cur.subWord(1)

      outData.value = cur
      return TSTraversalAction.EXPLORE
    }

    return TSTraversalAction.ABORT_INPUT
  }

}