package cmu.isr.robustness.explanation

import cmu.isr.ts.LTS
import cmu.isr.ts.lts.checkSafety
import cmu.isr.ts.lts.traceToLTS
import net.automatalib.words.Alphabet
import net.automatalib.words.Word

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