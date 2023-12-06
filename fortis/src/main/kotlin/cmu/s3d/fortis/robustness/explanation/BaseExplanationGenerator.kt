package cmu.s3d.fortis.robustness.explanation

import cmu.s3d.fortis.ts.LTS
import cmu.s3d.fortis.ts.lts.checkSafety
import cmu.s3d.fortis.ts.lts.traceToLTS
import net.automatalib.alphabet.Alphabet
import net.automatalib.word.Word

class BaseExplanationGenerator<I>(
    private val sys: LTS<*, I>,
    private val errModel: LTS<*, I>
) : ExplanationGenerator<I> {
    override fun generate(trace: Word<I>, inputs: Alphabet<I>): Word<I>? {
//    val c = parallel(sys, errModel)
        val r = checkSafety(errModel, traceToLTS(trace, inputs))
        return r.trace
    }
}