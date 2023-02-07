package cmu.isr.supervisory.desops

import cmu.isr.supervisory.SupervisoryDFA
import cmu.isr.supervisory.SupervisoryNFA
import cmu.isr.ts.alphabet
import java.io.OutputStream

fun <S, I> write(output: OutputStream, dfa: SupervisoryDFA<S, I>) {
  write(output, dfa as SupervisoryNFA<S, I>)
}

fun <S, I> write(output: OutputStream, nfa: SupervisoryNFA<S, I>) {
  val writer = output.bufferedWriter()
  val states = nfa.states.toMutableList()
  // assume that the NFA should have only one initial state
  if (nfa.initialStates.size > 1)
    error("The NFA should have at most one initial state!")

  // Remove initial and insert it into the beginning
  states.remove(nfa.initialStates.single())
  states.add(0, nfa.initialStates.single())

  writer.appendLine(nfa.size().toString()).appendLine()
  for (state in states) {
    writer
      .append(state.toString())
      .append('\t')
      .append("${if (nfa.isAccepting(state)) 1 else 0}")
      .append('\t')
    val builder = StringBuilder()
    var counter = 0
    for (input in nfa.alphabet()) {
      for (trans in nfa.getTransitions(state, input)) {
        counter++
        builder
          .append(input)
          .append('\t')
          .append(nfa.getSuccessor(trans))
          .append('\t')
          .append(if (input in nfa.controllable) "c" else "uc")
          .append('\t')
          .appendLine(if (input in nfa.observable) "o" else "uo")
      }
    }
    writer.appendLine(counter.toString())
    writer.appendLine(builder.toString())
    writer.flush()
  }
  writer.flush()
}