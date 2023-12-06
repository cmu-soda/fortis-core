package cmu.s3d.fortis.robustness.explanation

import cmu.s3d.fortis.ts.LTS
import net.automatalib.alphabet.Alphabet
import net.automatalib.word.Word

interface DeviationModelGenerator {
    fun fromDeviations(traces: Collection<Word<String>>, inputs: Alphabet<String>): LTS<Int, String>
}