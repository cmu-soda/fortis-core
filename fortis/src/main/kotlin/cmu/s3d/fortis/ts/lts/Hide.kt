package cmu.s3d.fortis.ts.lts

import cmu.s3d.fortis.ts.DetLTS
import cmu.s3d.fortis.ts.LTS
import cmu.s3d.fortis.ts.MutableDetLTS
import cmu.s3d.fortis.ts.alphabet
import cmu.s3d.fortis.ts.nfa.reachableSet
import cmu.s3d.fortis.utils.forEachSetBit
import net.automatalib.automata.fsa.impl.compact.CompactDFA
import net.automatalib.words.impl.Alphabets
import java.util.*

fun <S, I> hide(lts: LTS<S, I>, hidden: Collection<I>): DetLTS<Int, I> {
    return hide(lts, hidden) { CompactDetLTS(CompactDFA(Alphabets.fromCollection(it))) }
}

fun <S, I, SO> hide(
    lts: LTS<S, I>, hidden: Collection<I>,
    builder: (Collection<I>) -> MutableDetLTS<SO, I>
): DetLTS<SO, I> {
    val observable = lts.alphabet() - hidden.toSet()
    val out = builder(observable)
    val outStateMap = mutableMapOf<BitSet, SO>()
    val reachable = reachableSet(lts, hidden)
    val stateIDs = lts.stateIDs()
    val stack = ArrayDeque<BitSet>()

    // create error state
    val errId = stateIDs.getStateId(lts.errorState)
    val errBs = BitSet()
    errBs.set(errId)
    outStateMap[errBs] = out.errorState

    // create initial bitset
    val initBs = BitSet()
    for (s in lts.initialStates) {
        initBs.set(stateIDs.getStateId(s))
        initBs.or(reachable[stateIDs.getStateId(s)]!!)
    }

    // create output initial state
    if (initBs.get(errId)) {
        out.initialState = outStateMap[errBs]
    } else {
        val initOut = out.addInitialState(true)
        outStateMap[initBs] = initOut
        stack.push(initBs)
    }

    while (stack.isNotEmpty()) {
        val currBs = stack.pop()

        for (a in observable) {
            val succBs = BitSet()

            currBs.forEachSetBit {
                for (succState in lts.getSuccessors(stateIDs.getState(it), a)) {
                    val succStateId = stateIDs.getStateId(succState)
                    succBs.set(succStateId)
                    succBs.or(reachable[succStateId]!!)
                }
            }

            if (!succBs.isEmpty) {
                var outSucc = if (succBs.get(errId)) {
                    outStateMap[errBs]
                } else {
                    outStateMap[succBs]
                }
                if (outSucc == null) {
                    outSucc = out.addState(true)
                    outStateMap[succBs] = outSucc
                    stack.push(succBs)
                }
                out.addTransition(outStateMap[currBs], a, outSucc, null)
            }
        }
    }

    return out
}