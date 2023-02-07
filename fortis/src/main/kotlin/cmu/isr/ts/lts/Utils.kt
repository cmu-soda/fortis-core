package cmu.isr.ts.lts

import cmu.isr.ts.DetLTS
import net.automatalib.util.automata.builders.AutomatonBuilders
import net.automatalib.words.Alphabet
import net.automatalib.words.Word

fun <I> traceToLTS(trace: Word<I>, inputs: Alphabet<I>): DetLTS<*, I> {
  val builder = AutomatonBuilders.newDFA(inputs).withInitial(0)
  for (i in 0 until trace.length()) {
    builder.from(i).on(trace.getSymbol(i)).to(i+1).withAccepting(i)
    // last state is the error state
  }
  return builder.create().asLTS()
}