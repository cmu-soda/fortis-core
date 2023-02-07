package cmu.isr.ts.nfa

import cmu.isr.ts.alphabet
import net.automatalib.serialization.aut.AUTWriter
import net.automatalib.util.automata.Automata
import net.automatalib.util.automata.builders.AutomatonBuilders
import net.automatalib.words.impl.Alphabets
import org.junit.jupiter.api.Test
import kotlin.test.assertEquals

class NFATests {

  @Test
  fun testParallelComposition() {
    val a = AutomatonBuilders.newDFA(Alphabets.fromArray('a', 'b'))
      .withInitial(0)
      .from(0).on('a').to(1)
      .from(1).on('b').to(0)
      .withAccepting(0, 1)
      .create()

    val b = AutomatonBuilders.newDFA(Alphabets.fromArray('c'))
      .withInitial(0)
      .from(0).on('c').to(1)
      .from(1).on('c').to(1)
      .withAccepting(1)
      .create()

    val c = parallelComposition(a, b)
    val dc = determinise(c)

    val d = AutomatonBuilders.newDFA(Alphabets.fromArray('a', 'b', 'c'))
      .withInitial(0)
      .from(0).on('c').to(1).on('a').to(3)
      .from(1).on('c').to(1).on('a').to(2)
      .from(2).on('c').to(2).on('b').to(1)
      .from(3).on('c').to(2).on('b').to(0)
      .withAccepting(1, 2)
      .create()

    assertEquals(Alphabets.fromArray('a', 'b', 'c'), c.alphabet())
    assert(Automata.testEquivalence(dc, d, d.inputAlphabet)) {
      println("Expected:")
      AUTWriter.writeAutomaton(d, d.inputAlphabet, System.out)
      println("Actual:")
      AUTWriter.writeAutomaton(c, c.alphabet(), System.out)
      "The two models are not equivalent by [${Automata.findSeparatingWord(dc, d, d.inputAlphabet)}]!"
    }
  }

  @Test
  fun testParallelComposition2() {
    val a = AutomatonBuilders.newNFA(Alphabets.fromArray('a', 'b'))
      .withInitial(0)
      .from(0).on('a').to(1)
      .from(1).on('b').to(0).on('b').to(1)
      .withAccepting(0, 1)
      .create()

    val b = AutomatonBuilders.newNFA(Alphabets.fromArray('b', 'c'))
      .withInitial(0)
      .from(0).on('b').to(1)
      .from(1).on('c').to(0)
      .withAccepting(1)
      .create()

    val c = parallelComposition(a, b)
    val dc = determinise(c)

    val d = AutomatonBuilders.newNFA(Alphabets.fromArray('a', 'b', 'c'))
      .withInitial(0)
      .from(0).on('a').to(1)
      .from(1).on('b').to(2).on('b').to(3)
      .from(2).on('c').to(1)
      .from(3).on('a').to(2).on('c').to(0)
      .withAccepting(2, 3)
      .create()
    val dd = determinise(d)

    assertEquals(Alphabets.fromArray('a', 'b', 'c'), c.alphabet())
    assert(Automata.testEquivalence(dc, dd, d.inputAlphabet)) {
      println("Expected:")
      AUTWriter.writeAutomaton(dd, d.inputAlphabet, System.out)
      println("Actual:")
      AUTWriter.writeAutomaton(dc, c.alphabet(), System.out)
      "The two models are not equivalent by [${Automata.findSeparatingWord(dc, dd, d.inputAlphabet)}]!"
    }
  }

}