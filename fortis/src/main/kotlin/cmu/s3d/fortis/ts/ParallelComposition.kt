package cmu.s3d.fortis.ts

import net.automatalib.automaton.fsa.DFA
import net.automatalib.automaton.fsa.NFA
import cmu.s3d.fortis.ts.dfa.parallelComposition as DFAParallelComposition
import cmu.s3d.fortis.ts.lts.parallelComposition as LTSParallelComposition
import cmu.s3d.fortis.ts.nfa.parallelComposition as NFAParallelComposition

fun <T, TO : T> parallel(composition: (T, T) -> TO, vararg fas: T): TO {
    if (fas.size >= 2) {
        var c = composition(fas[0], fas[1])
        for (i in 2 until fas.size)
            c = composition(c, fas[i])
        return c
    }
    error("Need at least two automata to compose")
}

fun <I> parallel(vararg fas: NFA<*, I>): NFA<Int, I> {
    return parallel(::NFAParallelComposition, *fas)
}

fun <I> parallel(vararg fas: DFA<*, I>): DFA<Int, I> {
    return parallel(::DFAParallelComposition, *fas)
}

fun <I> parallel(vararg fas: LTS<*, I>): LTS<Int, I> {
    return parallel(::LTSParallelComposition, *fas)
}

fun <I> parallel(vararg fas: DetLTS<*, I>): DetLTS<Int, I> {
    return parallel(::LTSParallelComposition, *fas)
}