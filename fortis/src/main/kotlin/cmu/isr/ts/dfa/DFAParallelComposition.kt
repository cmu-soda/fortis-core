package cmu.isr.ts.dfa

import cmu.isr.ts.alphabet
import cmu.isr.ts.nfa.NFAParallelComposition
import net.automatalib.automata.fsa.DFA
import net.automatalib.automata.fsa.impl.compact.CompactDFA
import net.automatalib.util.ts.copy.TSCopy
import net.automatalib.util.ts.traversal.TSTraversal
import net.automatalib.util.ts.traversal.TSTraversalMethod
import net.automatalib.words.impl.Alphabets

fun <I> parallelComposition(dfa1: DFA<*, I>, dfa2: DFA<*, I>): DFA<Int, I> {
  val inputs = Alphabets.fromCollection(dfa1.alphabet() union dfa2.alphabet())
  val out = CompactDFA(inputs)
  val composition = NFAParallelComposition(dfa1, dfa2)

  TSCopy.copy(TSTraversalMethod.DEPTH_FIRST, composition, TSTraversal.NO_LIMIT, inputs, out)
  return out
}