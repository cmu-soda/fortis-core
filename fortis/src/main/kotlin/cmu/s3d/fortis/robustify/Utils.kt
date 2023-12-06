package cmu.s3d.fortis.robustify

import net.automatalib.alphabet.Alphabets
import net.automatalib.automaton.fsa.CompactDFA
import net.automatalib.util.automaton.builder.AutomatonBuilders

fun <I> makeProgress(input: I): CompactDFA<I> {
    val inputs = Alphabets.fromArray(input)
    return AutomatonBuilders.newDFA(inputs)
        .withInitial(0)
        .from(0).on(input).to(1)
        .from(1).on(input).to(1)
        .withAccepting(1)
        .create()
}