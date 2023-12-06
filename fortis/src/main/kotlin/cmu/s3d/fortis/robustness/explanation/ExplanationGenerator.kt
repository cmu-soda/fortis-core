package cmu.s3d.fortis.robustness.explanation

import net.automatalib.alphabet.Alphabet
import net.automatalib.word.Word

interface ExplanationGenerator {

    fun generate(trace: Word<String>, inputs: Alphabet<String>): Word<String>?
}