package cmu.isr.robustness.explanation

import cmu.isr.ts.LTS
import net.automatalib.words.Alphabet
import net.automatalib.words.Word

interface DeviationModelGenerator<S, I> {
  fun fromDeviations(traces: Collection<Word<I>>, inputs: Alphabet<I>): LTS<S, I>
}