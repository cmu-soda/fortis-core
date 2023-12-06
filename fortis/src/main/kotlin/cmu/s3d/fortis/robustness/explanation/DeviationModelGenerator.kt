package cmu.s3d.fortis.robustness.explanation

import cmu.s3d.fortis.ts.LTS
import net.automatalib.alphabet.Alphabet
import net.automatalib.word.Word

interface DeviationModelGenerator<S, I> {
    fun fromDeviations(traces: Collection<Word<I>>, inputs: Alphabet<I>): LTS<S, I>
}