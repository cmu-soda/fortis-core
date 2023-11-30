package cmu.isr.weakening

import cmu.isr.ts.lts.ltsa.LTSACall
import cmu.isr.ts.lts.ltsa.LTSACall.asLTS
import cmu.isr.ts.lts.ltsa.LTSACall.compose
import cmu.isr.ts.lts.traceToLTS
import cmu.isr.ts.parallel
import net.automatalib.util.automata.builders.AutomatonBuilders
import net.automatalib.words.Word
import net.automatalib.words.impl.Alphabets
import org.junit.jupiter.api.Test
import kotlin.test.assertEquals

class LearningExampleGeneratorTests {

    @Test
    fun testProgress() {
        val model = AutomatonBuilders.newDFA(Alphabets.fromArray("a", "b", "c"))
            .withInitial(0)
            .from(0).on("a").to(1).on("b").to(2)
            .from(1).on("b").to(3)
            .from(2).on("a").to(3)
            .from(3).on("c").to(0)
            .create()
        val progress = AutomatonBuilders.newDFA(Alphabets.fromArray("a", "b", "c"))
            .withInitial(0)
            .from(0).on("a").loop().on("b").loop().on("c").to(1)
            .create()

        val examples = LearningExampleGenerator(parallel(model, progress)).toList()
        assertEquals(
            setOf(
                Word.fromSymbols("a", "b", "c"),
                Word.fromSymbols("b", "a", "c"),
            ),
            examples.toSet()
        )
    }

    @Test
    fun testTherac25() {
        val sys = LTSACall
            .compile(ClassLoader.getSystemResource("specs/therac25-2/sys.lts").readText())
            .compose()
            .asLTS()
        val t = traceToLTS(
            Word.fromSymbols("x", "up", "e", "enter", "b"),
            Alphabets.fromArray("x", "up", "e", "enter", "b"),
            makeError = false)
        val examples = LearningExampleGenerator(parallel(sys, t)).toList()
        assertEquals(
            setOf(
                Word.fromList("x,set_xray,up,e,set_ebeam,enter,b,fire_ebeam,reset".split(",")),
                Word.fromList("x,set_xray,up,e,enter,set_ebeam,b,fire_ebeam,reset".split(",")),
                Word.fromList("x,set_xray,up,e,enter,b,fire_xray,reset".split(",")),
                Word.fromList("x,up,set_xray,e,set_ebeam,enter,b,fire_ebeam,reset".split(",")),
                Word.fromList("x,up,set_xray,e,enter,set_ebeam,b,fire_ebeam,reset".split(",")),
                Word.fromList("x,up,set_xray,e,enter,b,fire_xray,reset".split(",")),
            ),
            examples.toSet()
        )
    }
}