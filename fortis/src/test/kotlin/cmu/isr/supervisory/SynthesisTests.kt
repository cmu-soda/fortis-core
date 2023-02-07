package cmu.isr.supervisory

import cmu.isr.ts.alphabet
import net.automatalib.serialization.aut.AUTWriter
import net.automatalib.util.automata.Automata
import net.automatalib.util.automata.builders.AutomatonBuilders
import net.automatalib.words.impl.Alphabets
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.Test
import kotlin.test.assertContentEquals
import kotlin.test.assertNotNull

abstract class SynthesisTests {

  abstract val synthesizer: SupervisorySynthesizer<Int, String>

  @AfterEach
  private fun close() {
    synthesizer.close()
  }

  private fun assertSynthesisResult(c: SupervisoryDFA<Int, String>, sup: SupervisoryDFA<Int, String>) {
    assertNotNull(sup)
    assertContentEquals(c.alphabet(), sup.alphabet())
    assertContentEquals(c.controllable, sup.controllable)
    assertContentEquals(c.observable, sup.observable)
    assert(Automata.testEquivalence(c, sup, c.alphabet())) {
      println("Expected:")
      AUTWriter.writeAutomaton(c, c.alphabet(), System.out)
      println("\nActual:")
      AUTWriter.writeAutomaton(sup, sup.alphabet(), System.out)
      "The models are not equivalent"
    }
  }

  @Test
  fun synthesisTest() {
    val a = AutomatonBuilders.newDFA(Alphabets.fromArray("a", "b", "c"))
      .withInitial(0)
      .from(0).on("a").to(1)
      .from(1).on("b").to(2).on("a").to(1)
      .from(2).on("c").to(0)
      .withAccepting(0, 1, 2)
      .create()
      .asSupDFA(listOf("a"), listOf("a"))

    val b = AutomatonBuilders.newDFA(Alphabets.fromArray("a", "c"))
      .withInitial(0)
      .from(0).on("a").to(1)
      .from(1).on("c").to(0)
      .withAccepting(0)
      .create()
      .asSupDFA(listOf("a"), listOf("a"))

    val c = AutomatonBuilders.newDFA(Alphabets.fromArray("a"))
      .withInitial(0)
      .from(0).on("a").to(1)
      .withAccepting(0)
      .create()
      .asSupDFA(listOf("a"), listOf("a"))

    val sup = synthesizer.synthesize(a, b)

    assertNotNull(sup)
    assertSynthesisResult(c, sup)
  }

  @Test
  fun synthesisTest2() {
    val a = AutomatonBuilders.newDFA(Alphabets.fromArray("a", "b", "c"))
      .withInitial(0)
      .from(0).on("a").to(1)
      .from(1).on("b").to(2).on("a").to(1)
      .from(2).on("c").to(0)
      .withAccepting(0, 1, 2)
      .create()
      .asSupDFA(listOf("a", "b", "c"), listOf("a", "b", "c"))

    val b = AutomatonBuilders.newDFA(Alphabets.fromArray("a", "c"))
      .withInitial(0)
      .from(0).on("a").to(1)
      .from(1).on("c").to(0)
      .withAccepting(0)
      .create()
      .asSupDFA(listOf("a", "c"), listOf("a", "c"))

    val c = AutomatonBuilders.newDFA(Alphabets.fromArray("a", "b", "c"))
      .withInitial(0)
      .from(0).on("a").to(1)
      .from(1).on("b").to(2)
      .from(2).on("c").to(0)
      .withAccepting(0)
      .create()
      .asSupDFA(listOf("a", "b", "c"), listOf("a", "b", "c"))

    val sup = synthesizer.synthesize(a, b)

    assertNotNull(sup)
    assertSynthesisResult(c, sup)
  }

  @Test
  fun synthesisTest3() {
    val inputs = Alphabets.fromArray("a", "b", "c")
    val controllable = Alphabets.fromArray("a", "b", "c")
    val observable = Alphabets.fromArray("a", "b", "c")
    val a = AutomatonBuilders.newDFA(inputs)
      .withInitial(0)
      .from(0).on("a").to(1)
      .from(1)
      .on("a").to(1)
      .on("b").to(2)
      .from(2).on("c").to(0)
      .withAccepting(0, 1, 2)
      .create()
      .asSupDFA(controllable, observable)

    val b = AutomatonBuilders.newDFA(inputs)
      .withInitial(0)
      .from(0).on("a").to(1)
      .from(1).on("b").to(2)
      .from(2).on("c").to(0)
      .withAccepting(0, 1, 2)
      .create()
      .asSupDFA(controllable, observable)

    val c = AutomatonBuilders.newDFA(inputs)
      .withInitial(0)
      .from(0).on("a").to(1)
      .from(1).on("b").to(2)
      .from(2).on("c").to(0)
      .withAccepting(0, 1, 2)
      .create()
      .asSupDFA(controllable, observable)

    val sup = synthesizer.synthesize(a, b)

    assertNotNull(sup)
    assertSynthesisResult(c, sup)
  }
}