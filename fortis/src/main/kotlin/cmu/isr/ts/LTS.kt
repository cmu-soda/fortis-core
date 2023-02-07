package cmu.isr.ts

import net.automatalib.automata.fsa.DFA
import net.automatalib.automata.fsa.MutableDFA
import net.automatalib.automata.fsa.MutableNFA
import net.automatalib.automata.fsa.NFA

interface LTS<S, I> : NFA<S, I> {
  val errorState: S
  fun isErrorState(state: S): Boolean
}

interface MutableLTS<S, I> : LTS<S, I>, MutableNFA<S, I>

interface DetLTS<S, I> : LTS<S, I>, DFA<S, I>

interface MutableDetLTS<S, I> : DetLTS<S, I>, MutableDFA<S, I>