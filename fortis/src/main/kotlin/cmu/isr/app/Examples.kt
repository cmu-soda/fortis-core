package cmu.isr.app

import cmu.isr.supervisory.SupervisoryDFA
import cmu.isr.ts.MutableDetLTS
import cmu.isr.ts.MutableLTS
import cmu.isr.ts.lts.ltsa.LTSACall
import cmu.isr.ts.lts.ltsa.LTSACall.asDetLTS
import cmu.isr.ts.lts.ltsa.LTSACall.asLTS
import cmu.isr.ts.lts.ltsa.LTSACall.compose
import net.automatalib.automata.fsa.MutableDFA
import net.automatalib.automata.fsa.MutableNFA
import java.io.File
import cmu.isr.supervisory.desops.parse as parseFSM

/**
 * Read FSM file
 */
fun readFSM(path: String) {
  val f = File(path)
  val out = f.inputStream()
  val nfa = parseFSM(out.bufferedReader())
  out.close()

  // it returns a supervisory NFA, but if you are sure that the FSM is a DFA, you can cast it to a DFA
  // i.e., supervisory DFA extends NFA
  val dfa = nfa as SupervisoryDFA

  // by default NFAs and DFAs are immutable, if you want to get a mutable nfa or dfa
  // the current implementation would not make a (deep) copy of the model, so it
  // might cause troubles when this is not desired.
  val mutableNFA = nfa.asNFA() as MutableNFA
  val mutableDFA = dfa.asDFA() as MutableDFA
}

/**
 * Read FSP file. I make LTS a type that extends NFA because in NFA we have the notion of acceptable states but
 * in LTS we don't. So in LTS all states are made acceptable, and there's always one unacceptable state, the error
 * state (which may not be reachable).
 */
fun readFSP(path: String) {
  val spec = File(path).readText()
  val composite = LTSACall.compile(spec).compose()

  // if the model is a NFA
  val nfa = composite.asLTS()
  // or the model is a DFA
  val dfa = composite.asDetLTS()

  // to mutable NFA or DFA
  val mutableNFA = nfa as MutableLTS
  val mutableDFA = dfa as MutableDetLTS

  // LTS interface always has an error state (which may not be reachable)
  nfa.errorState
  dfa.errorState
}

/**
 * In ts/ParallelComposition.kt file, there are four overloads of the parallel function,
 * corresponding to different types of models.
 */
fun parallelComposition() {
  // val composite = parallel(fa1, fa2, fa3)
}

/**
 * the weakest assumption is always a DetLTS
 */
fun weakestAssumption() {
//  val w = SubsetConstructionGenerator(sys, env, safety).generate()
}