package cmu.s3d.fortis.ts

import net.automatalib.automaton.fsa.DFA
import net.automatalib.automaton.fsa.MutableDFA
import net.automatalib.automaton.fsa.MutableNFA
import net.automatalib.automaton.fsa.NFA

interface LTS<S, I> : NFA<S, I> {
    val errorState: S
    fun isErrorState(state: S): Boolean
}

interface MutableLTS<S, I> : LTS<S, I>, MutableNFA<S, I>

interface DetLTS<S, I> : LTS<S, I>, DFA<S, I>

interface MutableDetLTS<S, I> : DetLTS<S, I>, MutableDFA<S, I>