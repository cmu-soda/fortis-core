package cmu.s3d.fortis.robustify.supervisory

import cmu.s3d.fortis.robustify.acceptsSubWord
import cmu.s3d.fortis.supervisory.asSupDFA
import cmu.s3d.fortis.ts.alphabet
import cmu.s3d.fortis.ts.lts.asLTS
import net.automatalib.alphabet.Alphabets
import net.automatalib.serialization.aut.AUTWriter
import net.automatalib.util.automaton.Automata
import net.automatalib.util.automaton.builder.AutomatonBuilders
import net.automatalib.word.Word
import org.junit.jupiter.api.Test
import kotlin.test.assertContentEquals
import kotlin.test.assertEquals

class UtilsTests {

    @Test
    fun testExtendAlphabet() {
        val a = AutomatonBuilders.newDFA(Alphabets.fromArray('a', 'b'))
            .withInitial(0)
            .from(0).on('a').to(1)
            .from(1).on('b').to(0)
            .withAccepting(0, 1)
            .create()
            .asLTS()
        val inputs = Alphabets.fromArray('a', 'b', 'c')

        val extended = extendAlphabet(a, a.alphabet(), inputs)
        assertEquals(inputs, extended.inputAlphabet)

        val b = AutomatonBuilders.newDFA(Alphabets.fromArray('a', 'b', 'c'))
            .withInitial(0)
            .from(0).on('a').to(1).on('c').to(0)
            .from(1).on('b').to(0).on('c').to(1)
            .withAccepting(0, 1)
            .create()
            .asLTS()

        assert(Automata.testEquivalence(extended, b, inputs)) {
            println(
                Automata.findSeparatingWord(
                    extended,
                    b,
                    inputs
                )
            )
        }
    }

    @Test
    fun testObserver() {
        val a = AutomatonBuilders.newDFA(Alphabets.fromArray('a', 'b', 'c'))
            .withInitial(0)
            .from(0).on('a').to(1)
            .from(1).on('b').to(2)
            .from(2).on('c').to(0)
            .withAccepting(0, 1, 2)
            .create()
            .asSupDFA(listOf('a', 'b', 'c'), listOf('a', 'b'))

        val b = AutomatonBuilders.newDFA(Alphabets.fromArray('a', 'b'))
            .withInitial(0)
            .from(0).on('a').to(1)
            .from(1).on('b').to(0)
            .withAccepting(0, 1)
            .create()
            .asSupDFA(listOf('a', 'b'), listOf('a', 'b'))

        val observed = observer(a, a.alphabet())
        assertContentEquals(b.controllable, observed.controllable)
        assertContentEquals(b.observable, observed.observable)
        assert(Automata.testEquivalence(b, observed, b.alphabet())) {
            println("Expected:")
            AUTWriter.writeAutomaton(b, b.alphabet(), System.out)
            println("\nActual:")
            AUTWriter.writeAutomaton(observed, observed.alphabet(), System.out)
            "The models are not equivalent"
        }
    }

    @Test
    fun testAcceptsSubWord() {
        val a = AutomatonBuilders.newDFA(Alphabets.fromArray('a', 'b', 'c'))
            .withInitial(0)
            .from(0).on('a').to(1)
            .from(1).on('b').to(2)
            .from(2).on('c').to(0)
            .withAccepting(0, 1, 2)
            .create()
        val word = Word.fromSymbols('a', 'c')
        assertEquals(true, acceptsSubWord(a, word).first)
    }

    @Test
    fun testAcceptsSubWord2() {
        val a = AutomatonBuilders.newDFA(Alphabets.fromArray('a', 'b', 'c', 'd'))
            .withInitial(0)
            .from(0).on('a').to(1)
            .from(1).on('b').to(2)
            .from(2).on('c').to(3)
            .from(3).on('d').to(0)
            .create()
        val word = Word.fromSymbols('b', 'c')
        assertEquals(true, acceptsSubWord(a, word).first)
    }

    @Test
    fun testAcceptsSubWord3() {
        val a = AutomatonBuilders.newDFA(Alphabets.fromArray('a', 'b', 'c'))
            .withInitial(0)
            .from(0).on('a').to(1)
            .from(1).on('b').to(2)
            .from(2).on('c').to(0)
            .withAccepting(0, 1, 2)
            .create()
        val word = Word.fromSymbols('b', 'd', 'c')
        assertEquals(true, acceptsSubWord(a, word).first)
    }

}