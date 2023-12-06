package cmu.s3d.fortis.robustness.explanation

import cmu.s3d.fortis.ts.LTS
import cmu.s3d.fortis.ts.lts.checkSafety
import cmu.s3d.fortis.ts.lts.traceToLTS
import net.automatalib.alphabet.Alphabet
import net.automatalib.word.Word

class BaseExplanationGenerator(
    private val sys: LTS<*, String>,
    private val errModel: LTS<*, String>
) : ExplanationGenerator {
    override fun generate(trace: Word<String>, inputs: Alphabet<String>): Word<String>? {
//    val c = parallel(sys, errModel)
        val r = checkSafety(errModel, traceToLTS(trace, inputs))
        return r.trace
    }
}