package cmu.isr.supervisory.desops

import cmu.isr.supervisory.*
import net.automatalib.util.automata.Automata
import net.automatalib.util.automata.builders.AutomatonBuilders
import net.automatalib.words.impl.Alphabets
import org.junit.jupiter.api.Disabled
import org.junit.jupiter.api.Test
import java.io.ByteArrayOutputStream
import kotlin.test.assertEquals

@Disabled
class DESopsTests : SynthesisTests() {

  private val _synthesizer = DESopsRunner()

  override val synthesizer: SupervisorySynthesizer<Int, String> = _synthesizer

  @Test
  fun testWriter() {
    val alphabets = Alphabets.characters('a', 'c')
    val a = AutomatonBuilders.newDFA(alphabets)
      .withInitial(0)
      .from(0).on('a').to(1)
      .from(1)
      .on('b').to(2)
      .on('a').to(1)
      .from(2).on('c').to(0)
      .withAccepting(0, 1)
      .create()
      .asSupDFA(Alphabets.fromArray('a', 'c'), Alphabets.fromArray('b', 'c'))

    val output = ByteArrayOutputStream()
    write(output, a)
    assertEquals("3\n\n" +
        "0\t1\t1\n" +
        "a\t1\tc\tuo\n\n" +
        "1\t1\t2\n" +
        "a\t1\tc\tuo\n" +
        "b\t2\tuc\to\n\n" +
        "2\t0\t1\n" +
        "c\t0\tc\to\n\n", output.toString())
  }

  @Test
  fun testWriter2() {
    val alphabets = Alphabets.characters('a', 'c')
    val a = AutomatonBuilders.newNFA(alphabets)
      .withInitial(0)
      .from(0).on('a').to(1)
      .from(1)
      .on('b').to(2)
      .on('a').to(1)
      .on('a').to(2)
      .from(2)
      .on('c').to(0)
      .on('c').to(2)
      .withAccepting(0, 1)
      .create()
      .asSupNFA(Alphabets.fromArray('a', 'c'), Alphabets.fromArray('b', 'c'))

    val output = ByteArrayOutputStream()
    write(output, a)
    assertEquals("3\n\n" +
        "0\t1\t1\n" +
        "a\t1\tc\tuo\n\n" +
        "1\t1\t3\n" +
        "a\t1\tc\tuo\n" +
        "a\t2\tc\tuo\n" +
        "b\t2\tuc\to\n\n" +
        "2\t0\t2\n" +
        "c\t0\tc\to\n" +
        "c\t2\tc\to\n\n", output.toString())
  }

  @Test
  fun testParser() {
    val alphabets = Alphabets.fromArray("a", "b", "c")
    val controllable = Alphabets.fromArray("a", "c")
    val observable = Alphabets.fromArray("b", "c")
    val fsm = "3\n\n" +
        "0\t1\t1\n" +
        "a\t1\tc\tuo\n\n" +
        "1\t1\t2\n" +
        "a\t1\tc\tuo\n" +
        "b\t2\tuc\to\n\n" +
        "2\t0\t1\n" +
        "c\t0\tc\to\n\n"
    val a = parse(fsm.byteInputStream().bufferedReader(), alphabets, controllable, observable) as SupervisoryDFA

    val b = AutomatonBuilders.newDFA(alphabets)
      .withInitial(0)
      .from(0).on("a").to(1)
      .from(1)
      .on("b").to(2)
      .on("a").to(1)
      .from(2).on("c").to(0)
      .withAccepting(0, 1)
      .create()
      .asSupDFA(controllable, observable)

    assert(Automata.testEquivalence(a, b, alphabets))
    assertEquals(a.controllable, b.controllable)
    assertEquals(a.observable, b.observable)
  }
}