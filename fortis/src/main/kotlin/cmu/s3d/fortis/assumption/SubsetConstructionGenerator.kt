package cmu.s3d.fortis.assumption

import cmu.s3d.fortis.common.RobustnessOptions
import cmu.s3d.fortis.ts.*
import cmu.s3d.fortis.ts.lts.hide
import cmu.s3d.fortis.ts.lts.ltsa.LTSACall
import cmu.s3d.fortis.ts.lts.ltsa.LTSACall.asDetLTS
import cmu.s3d.fortis.ts.lts.ltsa.LTSACall.compose
import cmu.s3d.fortis.ts.lts.ltsa.LTSACall.minimize
import cmu.s3d.fortis.ts.lts.ltsa.writeFSP
import cmu.s3d.fortis.ts.lts.makeErrorState
import org.slf4j.LoggerFactory
import java.io.ByteArrayOutputStream

class SubsetConstructionGenerator(
    private val sys: LTS<*, String>,
    private val env: LTS<*, String>,
    private val safety: DetLTS<*, String>
) : WeakestAssumptionGenerator {
    private val assumptionInputs: Collection<String>
    private val logger = LoggerFactory.getLogger(javaClass)

    init {
//        val common = sys.alphabet() intersect env.alphabet()
//        val internal = sys.alphabet() - common
//        assumptionInputs = common union (safety.alphabet() - internal.toSet())
        assumptionInputs = sys.alphabet() intersect env.alphabet()
    }

    override fun generate(options: RobustnessOptions): DetLTS<Int, String> {
        // 1. compose sys || safety_err
        val comp = composeSysAndProp()
        // 2. prune the error state by backtracking from the initial error state
        val hidden = comp.alphabet().toSet() - assumptionInputs.toSet()
//    pruneError(comp)
        // 3. hide and determinise
        logger.info("S||P: #states = ${comp.size()}, #transitions: ${comp.numOfTransitions()}")
        logger.info("Pruning and determinising the model...")
        var wa = hide(comp, hidden) as MutableDetLTS

        // minimize
        if (options.minimized) {
            logger.info("Minimizing the model...")
            val out = ByteArrayOutputStream()
            writeFSP(out, wa, wa.alphabet())
            out.close()
            wa = LTSACall.compile(out.toString()).compose().minimize().asDetLTS() as MutableDetLTS
        }

        // 4. make sink
        if (options.disables) {
            val theta = wa.addState(true)
            for (state in wa) {
                if (wa.isErrorState(state))
                    continue
                for (input in wa.alphabet()) {
                    if (wa.getSuccessor(state, input) == null)
                        wa.addTransition(state, input, theta, null)
                }
            }
        }
        // 5. remove error state
        val waPredecessors = Predecessors(wa)
        for (input in wa.alphabet()) {
            for ((transition, source) in waPredecessors.getPredecessors(wa.errorState, input)) {
                wa.removeTransition(source, input, transition)
            }
        }

        return wa
    }

    private fun pruneError(comp: MutableLTS<Int, String>): LTS<Int, String> {
        logger.info("Prune reachable error of S||P through hidden events by backtracking")
        logger.info("S||P: #states = ${comp.size()}, #transitions: ${comp.numOfTransitions()}")
        val predecessors = Predecessors(comp)
        val queue = ArrayDeque<Int>()
        val hidden = comp.alphabet().toSet() - assumptionInputs.toSet()

        for (input in hidden)
            queue.addAll(predecessors.getPredecessors(comp.errorState, input).map { it.source })

        while (queue.isNotEmpty()) {
            // make this state an error state
            val state = queue.removeFirst()
            if (state in comp.initialStates)
                error("Initial state becomes the error state, no environment can prevent the system from reaching error state")

            // Remove out-going transitions
            comp.removeAllTransitions(state)
            // For all predecessors of this state, redirect them to the error state
            for (input in comp.alphabet()) {
                for ((transition, source) in predecessors.getPredecessors(state, input)) {
                    comp.removeTransition(source, input, transition)
                    comp.addTransition(source, input, comp.errorState, null)

                    if (input in hidden)
                        queue.addLast(source)
                }
            }
        }
        return comp
    }

    override fun generateUnsafe(): DetLTS<Int, String> {
        // 1. compose sys || safety_err
        val comp = composeSysAndProp()
        val hidden = comp.alphabet().toSet() - assumptionInputs.toSet()
//    pruneError(comp)
        return hide(comp, hidden)
    }

    private fun composeSysAndProp(): MutableLTS<Int, String> {
        logger.info("Compose System and Property...")
        logger.info("System: #states = ${sys.size()}, #transitions: ${sys.numOfTransitions()}")
        return parallel(sys, makeErrorState(safety)) as MutableLTS
    }
}