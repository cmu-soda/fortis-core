package cmu.s3d.fortis.robustify.oasis

import cmu.s3d.fortis.robustify.BaseRobustifier
import cmu.s3d.fortis.supervisory.SupervisorySynthesizer
import cmu.s3d.fortis.supervisory.asSupDFA
import cmu.s3d.fortis.supervisory.controlledEvents
import cmu.s3d.fortis.ts.acceptsSubWord
import cmu.s3d.fortis.ts.alphabet
import cmu.s3d.fortis.ts.makeProgress
import cmu.s3d.fortis.ts.nfa.hide
import cmu.s3d.fortis.ts.parallel
import cmu.s3d.fortis.utils.OrderedPowerSetIterator
import cmu.s3d.fortis.utils.pretty
import net.automatalib.automaton.fsa.DFA
import net.automatalib.word.Word
import org.slf4j.LoggerFactory
import java.time.Duration

class OASISRobustifier(
    sys: DFA<*, String>,
    devEnv: DFA<*, String>,
    safety: DFA<*, String>,
    progress: Collection<String>,
    val preferred: Collection<Word<String>>,
    val synthesizer: SupervisorySynthesizer<Int, String>
) : BaseRobustifier(sys, devEnv, safety) {
    private val logger = LoggerFactory.getLogger(javaClass)
    private val prop: DFA<*, String>


    override var numberOfSynthesis: Int = 0

    init {
        val progressProp = progress.map { makeProgress(it) }
        prop = parallel(safety, *progressProp.toTypedArray())
    }

    override fun synthesize(): DFA<Int, String>? {
        return synthesize(sys.alphabet(), sys.alphabet())
    }

    fun synthesize(controllable: Collection<String>, observable: Collection<String>): DFA<Int, String>? {
        if (!observable.containsAll(controllable))
            error("The controllable events should be a subset of the observable events.")

        logger.info("Number of controllable events: ${controllable.size}")
        logger.info("Controllable: $controllable")
        logger.info("Number of observable events: ${observable.size}")
        logger.info("Observable: $observable")

        val startTime = System.currentTimeMillis()
        val iter = OrderedPowerSetIterator((controllable union observable).toList())
        for (abs in iter) {
            logger.info("Abstract the system by $abs")
            val abstracted = abstracter(abs)
            assert(abstracted.alphabet().toSet() == sys.alphabet().toSet())

            val g = parallel(abstracted, devEnv).asSupDFA(controllable, observable)
            val p = prop.asSupDFA(
                prop.alphabet() intersect controllable.toSet(),
                prop.alphabet() intersect observable.toSet()
            )

            val sup = synthesizer.synthesize(g, p)
            numberOfSynthesis++

            if (sup != null) {
                val ctrlPlant = parallel(g, sup).asSupDFA(sup.controllable, sup.observable)

                logger.info("Found solution!")
                logger.info("Controlled events: ${controlledEvents(g, ctrlPlant, g.alphabet())}")

                val satisfiedPreferred = preferred.filter {
                    val (r, how) = acceptsSubWord(ctrlPlant, it)
                    logger.debug("Preferred behavior [$it] is satisfied by $how")
                    r
                }
                if (satisfiedPreferred.isEmpty()) {
                    logger.info("No preferred behaviors are satisfied!")
                } else {
                    logger.info("Number of satisfied preferred behavior: ${satisfiedPreferred.size}")
                    logger.info("Satisfied preferred behaviors:")
                    satisfiedPreferred.forEach { logger.info("\t$it") }
                }

                logger.info("Termination time: ${Duration.ofMillis(System.currentTimeMillis() - startTime).pretty()}")
                return sup
            }
        }

        logger.info("Termination time: ${Duration.ofMillis(System.currentTimeMillis() - startTime).pretty()}")
        return null
    }

    private fun abstracter(abs: Collection<String>): DFA<*, String> {
        val m = hide(sys, abs)
        val n = hide(sys, sys.alphabet() - abs.toSet())
        return parallel(m, n)
    }

}

