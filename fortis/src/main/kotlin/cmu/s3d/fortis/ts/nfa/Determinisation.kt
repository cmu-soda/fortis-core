package cmu.s3d.fortis.ts.nfa

import cmu.s3d.fortis.ts.alphabet
import cmu.s3d.fortis.utils.forEachSetBit
import net.automatalib.automaton.fsa.CompactDFA
import net.automatalib.automaton.fsa.DFA
import net.automatalib.automaton.fsa.MutableDFA
import net.automatalib.automaton.fsa.NFA
import java.util.*

// TODO: unit tests for this function
fun <S, I> determinise(nfa: NFA<S, I>): DFA<Int, I> {
    return determinise(nfa, CompactDFA(nfa.alphabet()))
}

fun <S, I, SO> determinise(nfa: NFA<S, I>, out: MutableDFA<SO, I>): DFA<SO, I> {
    val outStateMap = mutableMapOf<BitSet, SO>()
    val stateIDs = nfa.stateIDs()
    val stack = ArrayDeque<BitSet>()

    // create initial bitset
    val initBs = BitSet()
    for (s in nfa.initialStates) {
        initBs.set(stateIDs.getStateId(s))
    }

    // create output initial state
    var initAccept = true
    initBs.forEachSetBit { initAccept = initAccept && nfa.isAccepting(stateIDs.getState(it)) }
    val initOut = out.addInitialState(initAccept)

    outStateMap[initBs] = initOut
    stack.push(initBs)

    while (stack.isNotEmpty()) {
        val currBs = stack.pop()

        for (a in nfa.alphabet()) {
            val succBs = BitSet()

            currBs.forEachSetBit {
                val succStates = nfa.getSuccessors(stateIDs.getState(it), a)
                for (s in succStates)
                    succBs.set(stateIDs.getStateId(s))
            }

            if (!succBs.isEmpty) {
                var outSucc = outStateMap[succBs]
                if (outSucc == null) {
                    var outSuccAccept = true
                    succBs.forEachSetBit { outSuccAccept = outSuccAccept && nfa.isAccepting(stateIDs.getState(it)) }
                    outSucc = out.addState(outSuccAccept)
                    outStateMap[succBs] = outSucc
                    stack.push(succBs)
                }
                out.addTransition(outStateMap[currBs], a, outSucc, null)
            }
        }
    }

    return out
}