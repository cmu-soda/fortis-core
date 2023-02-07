package cmu.isr.supervisory.supremica

import cmu.isr.supervisory.SupervisoryDFA
import cmu.isr.supervisory.asSupDFA
import net.automatalib.util.automata.builders.AutomatonBuilders
import net.automatalib.words.impl.Alphabets
import org.supremica.automata.Automaton


fun parse(automaton: Automaton): SupervisoryDFA<Int, String> {
  val inputs = Alphabets.fromCollection(automaton.alphabet.map { it.label })
  val controllable = automaton.alphabet.controllableAlphabet.map { it.label }
  val observable = automaton.observableAlphabet.map { it.label }
  val builder = AutomatonBuilders.newDFA(inputs).withInitial(automaton.initialState)

  for (arc in automaton.arcIterator()) {
    builder.from(arc.fromState).on(arc.label).to(arc.toState)
  }

  for (state in automaton.stateIterator()) {
    if (state.isAccepting)
      builder.withAccepting(state)
  }

  return builder.create().asSupDFA(controllable, observable)
}