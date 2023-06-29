package cmu.isr.assumption

import cmu.isr.ts.alphabet
import cmu.isr.ts.lts.asLTS
import cmu.isr.ts.lts.ltsa.LTSACall
import cmu.isr.ts.lts.ltsa.LTSACall.asDetLTS
import cmu.isr.ts.lts.ltsa.LTSACall.compose
import cmu.isr.ts.lts.ltsa.write
import net.automatalib.util.automata.Automata
import net.automatalib.util.automata.builders.AutomatonBuilders
import net.automatalib.words.impl.Alphabets
import org.junit.jupiter.api.Test
import java.io.ByteArrayOutputStream
import kotlin.test.assertContentEquals
import kotlin.test.assertEquals

class SubsetGenTests {

  @Test
  fun testWA() {
    val a = AutomatonBuilders.newDFA(Alphabets.fromArray("a", "b", "c"))
      .withInitial(0)
      .from(0).on("a").to(1)
      .from(1).on("b").to(2).on("a").to(1)
      .from(2).on("c").to(0)
      .withAccepting(0, 1, 2)
      .create()
      .asLTS()

    val b = AutomatonBuilders.newDFA(Alphabets.fromArray("a", "b"))
      .withInitial(0)
      .from(0).on("a").to(0).on("b").to(0)
      .withAccepting(0)
      .create()
      .asLTS()

    val p = AutomatonBuilders.newDFA(Alphabets.fromArray("a", "c"))
      .withInitial(0)
      .from(0).on("a").to(1)
      .from(1).on("c").to(0)
      .withAccepting(0, 1)
      .create()
      .asLTS()

    val w = SubsetConstructionGenerator(a, b, p).generate(false)

    val c = AutomatonBuilders.newDFA(Alphabets.fromArray("a", "b"))
      .withInitial(0)
      .from(0).on("a").to(1)
      .from(1).on("b").to(0)
      .withAccepting(0, 1)
      .create()

    assertContentEquals(c.alphabet(), w.alphabet())
    assert(Automata.testEquivalence(c, w, c.alphabet())) {
      write(System.err, w, w.alphabet())
    }
  }

  @Test
  fun testSimpleProtocol() {
    val sys = LTSACall.compile(ClassLoader.getSystemResource("specs/abp/perfect.lts").readText())
      .compose().asDetLTS()
    val env = LTSACall.compile(ClassLoader.getSystemResource("specs/abp/abp_env.lts").readText())
      .compose().asDetLTS()
    val safety = LTSACall.compile(ClassLoader.getSystemResource("specs/abp/p.lts").readText())
      .compose().asDetLTS()

    val w = SubsetConstructionGenerator(sys, env, safety).generate(false)

    val c = LTSACall.compile(ClassLoader.getSystemResource("specs/abp/perfect_wa.lts").readText())
      .compose().asDetLTS()

    assertEquals(c.alphabet().toSet(), w.alphabet().toSet())
    assert(Automata.testEquivalence(c, w, c.alphabet())) {
      write(System.err, w, w.alphabet())
    }
  }

  @Test
  fun testTherac() {
    val sys = LTSACall.compile(ClassLoader.getSystemResource("specs/therac25/sys.lts").readText())
      .compose().asDetLTS()
    val env = LTSACall.compile(ClassLoader.getSystemResource("specs/therac25/env_perfect.lts").readText())
      .compose().asDetLTS()
    val safety = LTSACall.compile(ClassLoader.getSystemResource("specs/therac25/p.lts").readText())
      .compose().asDetLTS()

    val w = SubsetConstructionGenerator(sys, env, safety).generate(false)
    val out = ByteArrayOutputStream()
    out.use { write(out, w, w.alphabet()) }
    assertEquals("S1 = (x -> S2 | e -> S3),\n" +
        "S2 = (up -> S14 | enter -> S15),\n" +
        "S3 = (up -> S4 | enter -> S5),\n" +
        "S14 = (x -> S2 | e -> S11),\n" +
        "S15 = (up -> S2 | b -> S16),\n" +
        "S4 = (x -> S7 | e -> S3),\n" +
        "S5 = (up -> S3 | b -> S6),\n" +
        "S11 = (up -> S12 | enter -> S13),\n" +
        "S16 = (enter -> S14),\n" +
        "S7 = (up -> S8 | enter -> S9),\n" +
        "S6 = (enter -> S4),\n" +
        "S12 = (x -> S7 | e -> S11),\n" +
        "S13 = (up -> S11),\n" +
        "S8 = (x -> S7 | e -> S11),\n" +
        "S9 = (up -> S7 | b -> S10),\n" +
        "S10 = (enter -> S8).\n",
      out.toString())
  }

  @Test
  fun testTheracR() {
    val sys = LTSACall.compile(ClassLoader.getSystemResource("specs/therac25/sys_r.lts").readText())
      .compose().asDetLTS()
    val env = LTSACall.compile(ClassLoader.getSystemResource("specs/therac25/env_perfect.lts").readText())
      .compose().asDetLTS()
    val safety = LTSACall.compile(ClassLoader.getSystemResource("specs/therac25/p.lts").readText())
      .compose().asDetLTS()

    val w = SubsetConstructionGenerator(sys, env, safety).generate(false)
    val out = ByteArrayOutputStream()
    out.use { write(out, w, w.alphabet()) }
    assertEquals("S1 = (x -> S2 | e -> S3),\n" +
        "S2 = (up -> S11 | enter -> S9),\n" +
        "S3 = (up -> S4 | enter -> S5),\n" +
        "S11 = (x -> S2 | e -> S12),\n" +
        "S9 = (up -> S2 | b -> S10),\n" +
        "S4 = (x -> S7 | e -> S3),\n" +
        "S5 = (up -> S3 | b -> S6),\n" +
        "S12 = (up -> S13 | enter -> S5),\n" +
        "S10 = (enter -> S11),\n" +
        "S7 = (up -> S8 | enter -> S9),\n" +
        "S6 = (enter -> S4),\n" +
        "S13 = (x -> S7 | e -> S12),\n" +
        "S8 = (x -> S7 | e -> S12).\n",
      out.toString())
  }

}