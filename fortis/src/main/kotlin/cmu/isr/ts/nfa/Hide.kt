package cmu.isr.ts.nfa

import cmu.isr.ts.alphabet
import cmu.isr.utils.forEachSetBit
import net.automatalib.automata.fsa.DFA
import net.automatalib.automata.fsa.MutableDFA
import net.automatalib.automata.fsa.NFA
import net.automatalib.automata.fsa.impl.compact.CompactDFA
import net.automatalib.words.impl.Alphabets
import java.util.*

fun <S, I> reachableSet(nfa: NFA<S, I>, hidden: Collection<I>): Map<Int, BitSet> {
  val reachable = mutableMapOf<Int, BitSet>()
  val stateIDs = nfa.stateIDs()
  for (state in nfa) {
    val id = stateIDs.getStateId(state)
    reachable[id] = BitSet()
    for (uo in hidden) {
      for (succ in nfa.getSuccessors(state, uo))
        reachable[id]!!.set(stateIDs.getStateId(succ))
    }
  }

  var fixpoint = false
  while (!fixpoint) {
    fixpoint = true
    for (bs in reachable.values) {
      val oldSize = bs.cardinality()
      var i = bs.nextSetBit(0)
      while (i >= 0) {
        if (i == Int.MAX_VALUE)
          break
        bs.or(reachable[i]!!)
        i = bs.nextSetBit(i+1)
      }
      fixpoint = fixpoint && bs.cardinality() == oldSize
    }
  }

  return reachable
}

fun <S, I> hide(nfa: NFA<S, I>, hidden: Collection<I>): DFA<Int, I> {
  return hide(nfa, hidden) { CompactDFA(Alphabets.fromCollection(it)) }
}

fun <S, I, SO> hide(nfa: NFA<S, I>, hidden: Collection<I>, builder: (Collection<I>) -> MutableDFA<SO, I>): DFA<SO, I> {
  val observable = nfa.alphabet() - hidden.toSet()
  val out = builder(observable)
  val outStateMap = mutableMapOf<BitSet, SO>()
  val reachable = reachableSet(nfa, hidden)
  val stateIDs = nfa.stateIDs()
  val stack = ArrayDeque<BitSet>()

  // create initial bitset
  val initBs = BitSet()
  for (s in nfa.initialStates) {
    initBs.set(stateIDs.getStateId(s))
    initBs.or(reachable[stateIDs.getStateId(s)]!!)
  }

  // create output initial state
  var initAccept = true
  initBs.forEachSetBit { initAccept = initAccept && nfa.isAccepting(stateIDs.getState(it)) }
  val initOut = out.addInitialState(initAccept)

  outStateMap[initBs] = initOut
  stack.push(initBs)

  while (stack.isNotEmpty()) {
    val currBs = stack.pop()

    for (a in observable) {
      val succBs = BitSet()

      currBs.forEachSetBit {
        for (succState in nfa.getSuccessors(stateIDs.getState(it), a)) {
          val succStateId = stateIDs.getStateId(succState)
          succBs.set(succStateId)
          succBs.or(reachable[succStateId]!!)
        }
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