package cmu.isr.ts.lts

import cmu.isr.ts.lts.ltsa.LTSACall
import cmu.isr.ts.lts.ltsa.LTSACall.asDetLTS
import cmu.isr.ts.lts.ltsa.LTSACall.compose
import net.automatalib.util.automata.Automata
import net.automatalib.util.automata.builders.AutomatonBuilders
import net.automatalib.words.impl.Alphabets
import org.junit.jupiter.api.Test

class LTSATests {

  @Test
  fun testParseError() {
    val spec = "A = (a -> b -> A | b -> ERROR)."
    val m = LTSACall.compile(spec).compose().asDetLTS()
    val e = AutomatonBuilders.newDFA(Alphabets.fromArray("a", "b"))
      .withInitial(0)
      .from(0).on("a").to(1)
      .from(1).on("b").to(0)
      .from(0).on("b").to(-1)
      .withAccepting(0, 1)
      .create()
    assert(Automata.testEquivalence(e, m, e.inputAlphabet))
  }

  @Test
  fun testParseNoError() {
    val spec = "A = (a -> b -> A)."
    val m = LTSACall.compile(spec).compose().asDetLTS()
    val e = AutomatonBuilders.newDFA(Alphabets.fromArray("a", "b"))
      .withInitial(0)
      .from(0).on("a").to(1)
      .from(1).on("b").to(0)
      .withAccepting(0, 1)
      .create()
    assert(Automata.testEquivalence(e, m, e.inputAlphabet))
  }
}