package cmu.s3d.fortis.supervisory.supremica

import cmu.s3d.fortis.supervisory.SupervisorySynthesizer
import cmu.s3d.fortis.supervisory.SynthesisTests
import cmu.s3d.fortis.supervisory.asSupDFA
import cmu.s3d.fortis.ts.alphabet
import net.automatalib.alphabet.Alphabets
import net.automatalib.util.automaton.Automata
import net.automatalib.util.automaton.builder.AutomatonBuilders
import org.junit.jupiter.api.Test
import kotlin.test.assertContentEquals

class SupremicaTests : SynthesisTests() {

    private val _synthesizer = SupremicaRunner()

    override val synthesizer: SupervisorySynthesizer<Int, String> = _synthesizer

    @Test
    fun testWriteRead() {
        val inputs = Alphabets.fromArray("a", "b", "c")
        val controllable = Alphabets.fromArray("a", "c")
        val observable = Alphabets.fromArray("a", "b")
        val a = AutomatonBuilders.newDFA(inputs)
            .withInitial(0)
            .from(0).on("a").to(1)
            .from(1).on("b").to(2)
            .from(2).on("c").to(0)
            .withAccepting(0, 1, 2)
            .create()
            .asSupDFA(controllable, observable)

        val b = parseSupremica(writeSupremica(a, ""))

        assertContentEquals(inputs, b.alphabet())
        assertContentEquals(controllable, b.controllable)
        assertContentEquals(observable, b.observable)
        assert(Automata.testEquivalence(a, b, inputs))
    }

    @Test
    fun testWriteRead2() {
        val inputs = Alphabets.fromArray("a", "b", "c")
        val controllable = Alphabets.fromArray("a", "c")
        val observable = Alphabets.fromArray("a", "b")
        val a = AutomatonBuilders.newDFA(inputs)
            .withInitial(0)
            .from(0).on("a").to(1)
            .from(1).on("b").to(2)
            .from(2).on("c").to(0)
            .withAccepting(0, 2)
            .create()
            .asSupDFA(controllable, observable)

        val b = parseSupremica(writeSupremica(a, ""))

        assertContentEquals(inputs, b.alphabet())
        assertContentEquals(controllable, b.controllable)
        assertContentEquals(observable, b.observable)
        assert(Automata.testEquivalence(a, b, inputs))
    }

}