package cmu.isr.ts.dfa

import cmu.isr.supervisory.asSupDFA
import cmu.isr.ts.alphabet
import cmu.isr.ts.nfa.reachableSet
import net.automatalib.util.automata.Automata
import net.automatalib.util.automata.builders.AutomatonBuilders
import net.automatalib.words.impl.Alphabets
import org.junit.jupiter.api.Test
import java.util.*
import kotlin.test.assertEquals

class DFATests {

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

    val d = AutomatonBuilders.newDFA(Alphabets.fromArray('a', 'b', 'c'))
      .withInitial(0)
      .from(0).on('c').to(1).on('a').to(3)
      .from(1).on('c').to(1).on('a').to(2)
      .from(2).on('c').to(2).on('b').to(1)
      .from(3).on('c').to(2).on('b').to(0)
      .withAccepting(1, 2)
      .create()

    assertEquals(Alphabets.fromArray('a', 'b', 'c'), c.alphabet())
    assert(Automata.testEquivalence(c, d, d.inputAlphabet))
  }

  @Test
  fun testReachableSet() {
    val a = AutomatonBuilders.newDFA(Alphabets.fromArray('a', 'b', 'c'))
      .withInitial(0)
      .from(0).on('a').to(1)
      .from(1).on('b').to(2)
      .from(2).on('c').to(0)
      .withAccepting(0, 1, 2)
      .create()
      .asSupDFA(listOf('a', 'b', 'c'), listOf('a', 'b'))

    val reachable = reachableSet(a, a.alphabet() - a.observable.toSet())
    assertEquals(BitSet(), reachable[0])
    assertEquals(BitSet(), reachable[1])
    assertEquals(let { val s = BitSet(); s.set(0); s }, reachable[2])
  }

  @Test
  fun testReachableSet2() {
    val a = AutomatonBuilders.newDFA(Alphabets.fromArray('a', 'b', 'c'))
      .withInitial(0)
      .from(0).on('a').to(1)
      .from(1).on('b').to(2)
      .from(2).on('c').to(3).on('b').to(1)
      .from(3).on('a').to(1)
      .create()
      .asSupDFA(listOf('a', 'b', 'c'), listOf('a', 'c'))

    val reachable = reachableSet(a, a.alphabet() - a.observable.toSet())
    assertEquals(BitSet(), reachable[0])
    assertEquals(let { val s = BitSet(); s.set(2); s.set(1); s }, reachable[1])
    assertEquals(let { val s = BitSet(); s.set(1); s.set(2); s }, reachable[2])
    assertEquals(BitSet(), reachable[3])
  }
}