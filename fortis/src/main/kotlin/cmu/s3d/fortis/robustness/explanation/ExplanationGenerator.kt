package cmu.s3d.fortis.robustness.explanation

import net.automatalib.alphabet.Alphabet
import net.automatalib.word.Word

interface ExplanationGenerator<I> {

    fun generate(trace: Word<I>, inputs: Alphabet<I>): Word<I>?
}