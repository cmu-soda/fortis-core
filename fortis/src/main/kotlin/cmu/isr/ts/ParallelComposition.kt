package cmu.isr.ts

import net.automatalib.automata.fsa.DFA
import net.automatalib.automata.fsa.NFA
import cmu.isr.ts.dfa.parallelComposition as DFAParallelComposition
import cmu.isr.ts.lts.parallelComposition as LTSParallelComposition
import cmu.isr.ts.nfa.parallelComposition as NFAParallelComposition

fun <T, TO: T> parallel(composition: (T, T) -> TO, vararg fas: T): TO {
  if (fas.size >= 2) {
    var c = composition(fas[0], fas[1])
    for (i in 2 until fas.size)
      c = composition(c, fas[i])
    return c
  }
  error("Need at least two automata to compose")
}

fun <I> parallel(vararg fas: NFA<*, I>): NFA<Int, I> {
  return parallel(::NFAParallelComposition, *fas)
}

fun <I> parallel(vararg fas: DFA<*, I>): DFA<Int, I> {
  return parallel(::DFAParallelComposition, *fas)
}

fun <I> parallel(vararg fas: LTS<*, I>): LTS<Int, I> {
  return parallel(::LTSParallelComposition, *fas)
}

fun <I> parallel(vararg fas: DetLTS<*, I>): DetLTS<Int, I> {
  return parallel(::LTSParallelComposition, *fas)
}