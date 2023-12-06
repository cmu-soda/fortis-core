package cmu.s3d.fortis.robustify

import net.automatalib.automaton.fsa.DFA


/**
 * @param sys The LTS of the system specification.
 * @param devEnv The deviated environment.
 * @param safety The LTS of the safety property. It does not need to be complete.
 */
abstract class BaseRobustifier(val sys: DFA<*, String>, val devEnv: DFA<*, String>, val safety: DFA<*, String>) {

    abstract var numberOfSynthesis: Int
        protected set

    /**
     * Synthesize a new system model such that it satisfies the safety property under the deviated environment.
     */
    abstract fun synthesize(): DFA<Int, String>?
}
