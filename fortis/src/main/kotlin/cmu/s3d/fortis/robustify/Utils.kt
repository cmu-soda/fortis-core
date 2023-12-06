package cmu.s3d.fortis.robustify

import net.automatalib.automata.fsa.impl.compact.CompactDFA
import net.automatalib.util.automata.builders.AutomatonBuilders
import net.automatalib.words.impl.Alphabets

fun <I> makeProgress(input: I): CompactDFA<I> {
    val inputs = Alphabets.fromArray(input)
    return AutomatonBuilders.newDFA(inputs)
        .withInitial(0)
        .from(0).on(input).to(1)
        .from(1).on(input).to(1)
        .withAccepting(1)
        .create()
}