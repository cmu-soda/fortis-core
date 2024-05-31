package cmu.s3d.fortis.robustness

import cmu.s3d.fortis.common.RobustnessOptions
import cmu.s3d.fortis.robustness.explanation.BaseExplanationGenerator
import cmu.s3d.fortis.robustness.explanation.ExplanationGenerator
import cmu.s3d.fortis.ts.alphabet
import cmu.s3d.fortis.ts.lts.ltsa.LTSACall
import cmu.s3d.fortis.ts.lts.ltsa.LTSACall.asDetLTS
import cmu.s3d.fortis.ts.lts.ltsa.LTSACall.asLTS
import cmu.s3d.fortis.ts.lts.ltsa.LTSACall.compose
import net.automatalib.word.Word
import org.junit.jupiter.api.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class RobustnessCalculatorTests {

    private fun hasPrefix(prefix: Word<String>, actual: Collection<Word<String>>): Boolean {
        for (t in actual) {
            if (prefix.isPrefixOf(t))
                return true
        }
        return false
    }

    private fun buildABP(): Pair<RobustnessCalculator, ExplanationGenerator> {
        val sys = LTSACall
            .compile(ClassLoader.getSystemResource("specs/abp/abp.lts").readText())
            .compose()
            .asLTS()
        val env = LTSACall
            .compile(ClassLoader.getSystemResource("specs/abp/abp_env.lts").readText())
            .compose()
            .asLTS()
        val safety = LTSACall
            .compile(ClassLoader.getSystemResource("specs/abp/p.lts").readText())
            .compose()
            .asDetLTS()
        val dev = LTSACall
            .compile(ClassLoader.getSystemResource("specs/abp/abp_env_lossy.lts").readText())
            .compose()
            .asLTS()

        return Pair(BaseCalculator(sys, env, safety, RobustnessOptions()), BaseExplanationGenerator(sys, dev))
    }

    private fun buildSimpleProtocol(expand: Boolean = false): Pair<RobustnessCalculator, ExplanationGenerator> {
        val sys = LTSACall
            .compile(ClassLoader.getSystemResource("specs/abp/perfect.lts").readText())
            .compose()
            .asLTS()
        val env = LTSACall
            .compile(ClassLoader.getSystemResource("specs/abp/abp_env.lts").readText())
            .compose()
            .asLTS()
        val safety = LTSACall
            .compile(ClassLoader.getSystemResource("specs/abp/p.lts").readText())
            .compose()
            .asDetLTS()
        val dev = LTSACall
            .compile(ClassLoader.getSystemResource("specs/abp/abp_env_lossy.lts").readText())
            .compose()
            .asLTS()

        return Pair(
            BaseCalculator(sys, env, safety, RobustnessOptions(expand = expand)),
            BaseExplanationGenerator(sys, dev)
        )
    }

    @Test
    fun testSimpleProtocol() {
        val (cal, explain) = buildSimpleProtocol()
        val expected = setOf(
            Word.fromSymbols("send.0", "rec.0", "ack.1", "getack.0"),
            Word.fromSymbols("send.0", "rec.0", "ack.0", "getack.1"),
            Word.fromSymbols("send.1", "rec.1", "ack.0", "getack.1"),
            Word.fromSymbols("send.1", "rec.1", "ack.1", "getack.0"),
            Word.fromSymbols("send.0", "rec.1"),
            Word.fromSymbols("send.1", "rec.0")
        )
        val actual = cal.computeRobustness().values.flatten().map { it.word }.toSet()
        assertEquals(expected = expected, actual = actual)

        val expectedExplain = setOf(
            Word.fromSymbols("send.0", "rec.0", "ack.1", "ack.corrupt", "getack.0"),
            Word.fromSymbols("send.0", "rec.0", "ack.0", "ack.corrupt", "getack.1"),
            Word.fromSymbols("send.1", "rec.1", "ack.0", "ack.corrupt", "getack.1"),
            Word.fromSymbols("send.1", "rec.1", "ack.1", "ack.corrupt", "getack.0"),
            Word.fromSymbols("send.0", "trans.corrupt", "rec.1"),
            Word.fromSymbols("send.1", "trans.corrupt", "rec.0")
        )
        assertEquals(expectedExplain, actual.map { explain.generate(it, cal.weakestAssumption.alphabet()) }.toSet())
    }

    @Test
    fun testSimpleProtocolExpand() {
        val (cal, explain) = buildSimpleProtocol(expand = true)
        val expected = setOf(
            Word.fromSymbols("send.0", "rec.0", "ack.1", "getack.0"),
            Word.fromSymbols("send.0", "rec.0", "ack.0", "getack.1"),
            Word.fromSymbols("send.1", "rec.1", "ack.0", "getack.1"),
            Word.fromSymbols("send.1", "rec.1", "ack.1", "getack.0"),
            Word.fromSymbols("send.0", "rec.1", "ack.1", "getack.0"),
            Word.fromSymbols("send.0", "rec.1", "ack.1", "getack.1"),
            Word.fromSymbols("send.0", "rec.1", "ack.0", "getack.0"),
            Word.fromSymbols("send.0", "rec.1", "ack.0", "getack.1"),
            Word.fromSymbols("send.1", "rec.0", "ack.0", "getack.1"),
            Word.fromSymbols("send.1", "rec.0", "ack.0", "getack.0"),
            Word.fromSymbols("send.1", "rec.0", "ack.1", "getack.1"),
            Word.fromSymbols("send.1", "rec.0", "ack.1", "getack.0")
        )
        val actual = cal.computeRobustness().values.flatten().map { it.word }.toSet()
        assertEquals(expected = expected, actual = actual)

        val expectedExplain = setOf(
            Word.fromSymbols("send.0", "rec.0", "ack.1", "ack.corrupt", "getack.0"),
            Word.fromSymbols("send.0", "rec.0", "ack.0", "ack.corrupt", "getack.1"),
            Word.fromSymbols("send.1", "rec.1", "ack.0", "ack.corrupt", "getack.1"),
            Word.fromSymbols("send.1", "rec.1", "ack.1", "ack.corrupt", "getack.0"),

            Word.fromSymbols("send.0", "trans.corrupt", "rec.1", "ack.1", "ack.corrupt", "getack.0"),
            Word.fromSymbols("send.0", "trans.corrupt", "rec.1", "ack.0", "getack.0"),
            Word.fromSymbols("send.0", "trans.corrupt", "rec.1", "ack.0", "ack.corrupt", "getack.1"),
            Word.fromSymbols("send.0", "trans.corrupt", "rec.1", "ack.1", "getack.1"),

            Word.fromSymbols("send.1", "trans.corrupt", "rec.0", "ack.1", "ack.corrupt", "getack.0"),
            Word.fromSymbols("send.1", "trans.corrupt", "rec.0", "ack.0", "getack.0"),
            Word.fromSymbols("send.1", "trans.corrupt", "rec.0", "ack.0", "ack.corrupt", "getack.1"),
            Word.fromSymbols("send.1", "trans.corrupt", "rec.0", "ack.1", "getack.1"),
        )
//    for (t in actual) {
//      println(explain.generate(t, cal.weakestAssumption.alphabet()))
//    }
        assertEquals(expectedExplain, actual.map { explain.generate(it, cal.weakestAssumption.alphabet()) }.toSet())
    }

    @Test
    fun testABP() {
        val (cal, explain) = buildABP()
        val actual = cal.computeRobustness().values.flatten().map { it.word }.toSet()
        for (t in actual) {
            val explanations = explain.generate(t, cal.weakestAssumption.alphabet())
//      println(explanations)
        }
    }

    private fun buildTherac(expand: Boolean = false): Pair<RobustnessCalculator, ExplanationGenerator> {
        val sys = LTSACall
            .compile(ClassLoader.getSystemResource("specs/therac25/sys.lts").readText())
            .compose()
            .asLTS()
        val env = LTSACall
            .compile(ClassLoader.getSystemResource("specs/therac25/env_perfect.lts").readText())
            .compose()
            .asLTS()
        val safety = LTSACall
            .compile(ClassLoader.getSystemResource("specs/therac25/p.lts").readText())
            .compose()
            .asDetLTS()
        val dev = LTSACall
            .compile(ClassLoader.getSystemResource("specs/therac25/env.lts").readText())
            .compose()
            .asLTS()

        return Pair(
            BaseCalculator(sys, env, safety, RobustnessOptions(expand = expand)),
            BaseExplanationGenerator(sys, dev)
        )
    }

    private fun buildTheracR(expand: Boolean = false): Pair<RobustnessCalculator, ExplanationGenerator> {
        val sys = LTSACall
            .compile(ClassLoader.getSystemResource("specs/therac25/sys_r.lts").readText())
            .compose()
            .asLTS()
        val env = LTSACall
            .compile(ClassLoader.getSystemResource("specs/therac25/env_perfect.lts").readText())
            .compose()
            .asLTS()
        val safety = LTSACall
            .compile(ClassLoader.getSystemResource("specs/therac25/p.lts").readText())
            .compose()
            .asDetLTS()
        val dev = LTSACall
            .compile(ClassLoader.getSystemResource("specs/therac25/env.lts").readText())
            .compose()
            .asLTS()

        return Pair(
            BaseCalculator(sys, env, safety, RobustnessOptions(expand = expand)),
            BaseExplanationGenerator(sys, dev)
        )
    }

    private fun buildTheracP2(): Pair<RobustnessCalculator, ExplanationGenerator> {
        val sys = LTSACall
            .compile(ClassLoader.getSystemResource("specs/therac25/sys.lts").readText())
            .compose()
            .asLTS()
        val env = LTSACall
            .compile(ClassLoader.getSystemResource("specs/therac25/env_perfect.lts").readText())
            .compose()
            .asLTS()
        val safety = LTSACall
            .compile(ClassLoader.getSystemResource("specs/therac25/p2.lts").readText())
            .compose()
            .asDetLTS()
        val dev = LTSACall
            .compile(ClassLoader.getSystemResource("specs/therac25/env.lts").readText())
            .compose()
            .asLTS()

        return Pair(BaseCalculator(sys, env, safety, RobustnessOptions()), BaseExplanationGenerator(sys, dev))
    }

    @Test
    fun testTherac() {
        val (cal, _) = buildTherac()
        val actual = cal.computeRobustness().values.flatten().map { it.word }.toSet()
        val expected = setOf(
            Word.fromSymbols("x", "up"),
            Word.fromSymbols("e", "up"),
            Word.fromSymbols("x", "enter", "up"),
            Word.fromSymbols("e", "enter", "up"),
            Word.fromSymbols("x", "enter", "b", "enter", "e", "up"),
            Word.fromSymbols("e", "enter", "b", "enter", "x", "up"),
            Word.fromSymbols("x", "enter", "b", "enter", "e", "enter", "up"),
            Word.fromSymbols("e", "enter", "b", "enter", "x", "enter", "up"),
        )
        assertEquals(expected = expected, actual = actual)
    }

    @Test
    fun testTheracExpand() {
        val (cal, _) = buildTherac(expand = true)
        val actual = cal.computeRobustness().values.flatten().map { it.word }.toSet()
        assertTrue(hasPrefix(Word.fromSymbols("x", "up", "x"), actual))
        assertFalse(hasPrefix(Word.fromSymbols("x", "up", "e", "enter", "b"), actual))
        assertTrue(hasPrefix(Word.fromSymbols("e", "up", "e"), actual))
        assertTrue(hasPrefix(Word.fromSymbols("e", "up", "x", "enter", "b"), actual))
    }

    @Test
    fun testTheracR() {
        val (cal, _) = buildTheracR()
        val actual = cal.computeRobustness().values.flatten().map { it.word }.toSet()
        val expected = setOf(
            Word.fromSymbols("x", "up"),
            Word.fromSymbols("e", "up"),
            Word.fromSymbols("x", "enter", "up"),
            Word.fromSymbols("e", "enter", "up"),
            Word.fromSymbols("x", "enter", "b", "enter", "e", "up"),
            Word.fromSymbols("e", "enter", "b", "enter", "x", "up"),
        )
        assertEquals(expected = expected, actual = actual)
    }

    @Test
    fun testTheracRExpand() {
        val (cal, _) = buildTheracR(expand = true)
        val actual = cal.computeRobustness().values.flatten().map { it.word }.toSet()
        assertTrue(hasPrefix(Word.fromSymbols("x", "up", "x"), actual))
        assertTrue(hasPrefix(Word.fromSymbols("x", "up", "e", "enter", "b"), actual))
        assertTrue(hasPrefix(Word.fromSymbols("e", "up", "e"), actual))
        assertTrue(hasPrefix(Word.fromSymbols("e", "up", "x", "enter", "b"), actual))
    }

    @Test
    fun testCompareTherac() {
        val (cal1, _) = buildTherac()
        val (cal2, _) = buildTheracR()
        assert(cal1.compare(cal2).isEmpty())
        assertEquals(
            setOf(Word.fromSymbols("x", "up", "e", "enter", "b")),
            cal2.compare(cal1).values.flatten().map { it.word }.toSet()
        )
    }

    @Test
    fun testCompareTheracExpand() {
        val (cal1, _) = buildTherac(expand = true)
        val (cal2, _) = buildTheracR(expand = true)

        val actual = cal2.compare(cal1).values.flatten().map { it.word }.toSet()
        assertTrue(hasPrefix(Word.fromSymbols("x", "up", "e", "enter", "b"), actual))
    }

    @Test
    fun testCompareTheracP() {
        val (cal1, _) = buildTherac()
        val (cal2, _) = buildTheracP2()
        assert(cal2.compare(cal1).isEmpty())
        assertEquals(
            setOf(Word.fromSymbols("e", "up", "x", "enter", "b")),
            cal1.compare(cal2).values.flatten().map { it.word }.toSet()
        )
    }

    private fun buildVoting(): Pair<RobustnessCalculator, ExplanationGenerator> {
        val sys = LTSACall
            .compile(ClassLoader.getSystemResource("specs/voting/sys.lts").readText())
            .compose()
            .asLTS()
        val env = LTSACall
            .compile(ClassLoader.getSystemResource("specs/voting/env0.lts").readText())
            .compose()
            .asLTS()
        val safety = LTSACall
            .compile(ClassLoader.getSystemResource("specs/voting/p.lts").readText())
            .compose()
            .asDetLTS()
        val dev = LTSACall
            .compile(ClassLoader.getSystemResource("specs/voting/env1.lts").readText())
            .compose()
            .asLTS()

        return Pair(BaseCalculator(sys, env, safety, RobustnessOptions()), BaseExplanationGenerator(sys, dev))
    }

    @Test
    fun testVoting() {
        val (cal, explain) = buildVoting()
        val actual = cal.computeRobustness().values.flatten().map { it.word }.toSet()
        for (t in actual) {
            println("$t => ${explain.generate(t, cal.weakestAssumption.alphabet())}")
        }
        assertEquals(emptySet(), actual.map { explain.generate(it, cal.weakestAssumption.alphabet()) }.toSet())
    }
}