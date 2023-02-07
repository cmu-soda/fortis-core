package cmu.isr.robustness.explanation

import net.automatalib.words.Alphabet
import net.automatalib.words.Word

interface ExplanationGenerator<I> {

  fun generate(trace: Word<I>, inputs: Alphabet<I>): Word<I>?
}