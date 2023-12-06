package cmu.s3d.fortis.ts.lts

import cmu.s3d.fortis.ts.DetLTS
import net.automatalib.alphabet.Alphabet
import net.automatalib.util.automaton.builder.AutomatonBuilders
import net.automatalib.word.Word

fun <I> traceToLTS(trace: Word<I>, inputs: Alphabet<I>, makeError: Boolean = true): DetLTS<*, I> {
    val builder = AutomatonBuilders.newDFA(inputs).withInitial(0)
    for (i in 0 until trace.length()) {
        builder.from(i).on(trace.getSymbol(i)).to(i + 1).withAccepting(i)
    }
    if (!makeError) {
        builder.withAccepting(trace.length())
    }
    return builder.create().asLTS()
}