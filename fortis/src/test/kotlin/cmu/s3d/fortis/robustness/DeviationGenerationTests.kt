package cmu.s3d.fortis.robustness

import cmu.s3d.fortis.robustness.explanation.BaseExplanationGenerator
import cmu.s3d.fortis.robustness.explanation.SimpleDeviationModelGenerator
import cmu.s3d.fortis.ts.alphabet
import cmu.s3d.fortis.ts.lts.ltsa.LTSACall
import cmu.s3d.fortis.ts.lts.ltsa.LTSACall.asDetLTS
import cmu.s3d.fortis.ts.lts.ltsa.LTSACall.asLTS
import cmu.s3d.fortis.ts.lts.ltsa.LTSACall.compose
import cmu.s3d.fortis.ts.nfa.determinise
import net.automatalib.alphabet.Alphabets
import net.automatalib.util.automaton.Automata
import org.junit.jupiter.api.Test

class DeviationGenerationTests {
    @Test
    fun testTherac() {
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
            .compile(ClassLoader.getSystemResource("specs/therac25/env_with_err.lts").readText())
            .compose()
            .asLTS()

        val cal = BaseCalculator(sys, env, safety, RobustnessOptions())
        val exp = BaseExplanationGenerator(sys, dev)
        val unsafe = cal.computeUnsafeBeh().values.flatten().map { it.word }
        val unsafeExplanations = unsafe.mapNotNull { exp.generate(it, cal.weakestAssumption.alphabet()) }
        val devGen = SimpleDeviationModelGenerator(dev, listOf("commission", "repetition"))
        val devAlphabet = Alphabets.fromCollection(cal.weakestAssumption.alphabet() union dev.alphabet())
        val newDev = devGen.fromDeviations(unsafeExplanations, devAlphabet)

        val expected = LTSACall
            .compile(
                "ENV = (x -> ENV_1 | e -> ENV_1),\n" +
                        "ENV_1 = (enter -> ENV_2 | commission -> up -> ENV),\n" +
                        "ENV_2 = (b -> enter -> ENV)+{repetition}."
            )
            .compose()
            .asDetLTS()

        assert(Automata.testEquivalence(expected, determinise(newDev), devAlphabet))
//    write(System.out, newDev, newDev.alphabet())
    }

}