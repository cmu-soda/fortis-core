package cmu.s3d.fortis.weakening

import cmu.s3d.fortis.ts.lts.ltsa.LTSACall
import cmu.s3d.fortis.ts.lts.ltsa.LTSACall.asLTS
import cmu.s3d.fortis.ts.lts.ltsa.LTSACall.compose
import cmu.s3d.fortis.ts.parallel
import net.automatalib.alphabet.Alphabets
import net.automatalib.util.automaton.builder.AutomatonBuilders
import net.automatalib.word.Word
import org.junit.jupiter.api.Test
import kotlin.test.assertEquals

class ExampleGeneratorTests {

    @Test
    fun testProgress() {
        val model = AutomatonBuilders.newDFA(Alphabets.fromArray("a", "b", "c"))
            .withInitial(0)
            .from(0).on("a").to(1).on("b").to(2)
            .from(1).on("b").to(3)
            .from(2).on("a").to(3)
            .from(3).on("c").to(0)
            .create()
        val examples = ProgressExampleGenerator(model, "c")
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
        val env = LTSACall
            .compile(ClassLoader.getSystemResource("specs/therac25-2/env.lts").readText())
            .compose()
            .asLTS()
        val examples = TraceExampleGenerator(
            parallel(sys, env),
            Word.fromSymbols("x", "up", "e", "enter", "b"),
            Alphabets.fromArray("x", "up", "e", "enter", "b")
        )
        assertEquals(
            setOf(
                Word.fromList("x,set_xray,up,e,set_ebeam,enter,b,fire_ebeam,reset".split(",")),
                Word.fromList("x,set_xray,up,e,enter,set_ebeam,b,fire_ebeam,reset".split(",")),
                Word.fromList("x,set_xray,up,e,enter,b,fire_xray,reset".split(",")),
                Word.fromList("x,up,set_xray,e,set_ebeam,enter,b,fire_ebeam,reset".split(",")),
            ),
            examples.toSet()
        )
    }

    @Test
    fun testVoting() {
        val sys = LTSACall
            .compile(ClassLoader.getSystemResource("specs/voting-2/sys.lts").readText())
            .compose()
            .asLTS()
        val env = LTSACall
            .compile(ClassLoader.getSystemResource("specs/voting-2/env2.lts").readText())
            .compose()
            .asLTS()
        val examples = TraceExampleGenerator(
            parallel(sys, env),
            Word.fromSymbols("password", "select", "vote", "confirm"),
            Alphabets.fromArray("password", "select", "vote", "confirm")
        )
        assertEquals(
            setOf(
                Word.fromList(
                    "v.enter,password,v.password,v.exit,eo.enter,select,eo.select,eo.exit,v.enter,vote,v.vote,v.exit,eo.enter,confirm,eo.confirm,eo.exit,eo.enter".split(
                        ','
                    )
                ),
                Word.fromList(
                    "v.enter,password,v.password,v.exit,eo.enter,select,eo.select,eo.exit,v.enter,vote,v.vote,v.exit,eo.enter,confirm,eo.confirm,eo.exit,v.enter,v.exit".split(
                        ','
                    )
                ),
                Word.fromList(
                    "v.enter,password,v.password,v.exit,eo.enter,select,eo.select,eo.exit,v.enter,vote,v.vote,confirm,v.confirm,v.exit,v.enter".split(
                        ','
                    )
                ),
                Word.fromList(
                    "v.enter,password,v.password,v.exit,eo.enter,select,eo.select,eo.exit,v.enter,vote,v.vote,confirm,v.confirm,v.exit,eo.enter,eo.exit".split(
                        ','
                    )
                ),
                Word.fromList(
                    "v.enter,password,v.password,v.exit,eo.enter,select,eo.select,vote,eo.vote,eo.exit,v.enter,confirm,v.confirm,v.exit,v.enter".split(
                        ','
                    )
                ),
                Word.fromList(
                    "v.enter,password,v.password,v.exit,eo.enter,select,eo.select,vote,eo.vote,eo.exit,v.enter,confirm,v.confirm,v.exit,eo.enter,eo.exit".split(
                        ','
                    )
                ),
                Word.fromList(
                    "v.enter,password,v.password,v.exit,eo.enter,select,eo.select,vote,eo.vote,confirm,eo.confirm,eo.exit,eo.enter".split(
                        ','
                    )
                ),
                Word.fromList(
                    "v.enter,password,v.password,v.exit,eo.enter,select,eo.select,vote,eo.vote,confirm,eo.confirm,eo.exit,v.enter,v.exit".split(
                        ','
                    )
                ),
                Word.fromList(
                    "v.enter,password,v.password,select,v.select,v.exit,eo.enter,vote,eo.vote,eo.exit,v.enter,confirm,v.confirm,v.exit,v.enter".split(
                        ','
                    )
                ),
            ),
            examples.toSet()
        )
    }
}