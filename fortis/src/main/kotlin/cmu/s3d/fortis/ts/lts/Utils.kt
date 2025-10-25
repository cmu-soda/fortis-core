package cmu.s3d.fortis.ts.lts

import cmu.s3d.fortis.ts.DetLTS
import cmu.s3d.fortis.ts.LTS
import cmu.s3d.fortis.ts.alphabet
import cmu.s3d.fortis.ts.lts.ltsa.writeFSP
import net.automatalib.alphabet.Alphabet
import net.automatalib.automaton.fsa.CompactNFA
import net.automatalib.util.automaton.builder.AutomatonBuilders
import net.automatalib.word.Word
import tlc2.LTSBuilder
import kotlin.system.exitProcess

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

fun <I> addAllTransitions(lts: LTS<Int,I>): LTS<Int,I> {
    val nfaAll = CompactNFA(lts.alphabet())
    for (s in lts.states) {
        if (s in lts.initialStates) {
            nfaAll.addInitialState(lts.isAccepting(s))
        } else {
            nfaAll.addState(lts.isAccepting(s))
        }
    }
    for (s1 in nfaAll.states) {
        for (s2 in nfaAll.states) {
            for (a in nfaAll.alphabet()) {
                if (nfaAll.isAccepting(s1) && nfaAll.isAccepting(s2)) {
                    nfaAll.addTransition(s1, a, s2)
                }
            }
        }
    }
    return CompactLTS(nfaAll)
}