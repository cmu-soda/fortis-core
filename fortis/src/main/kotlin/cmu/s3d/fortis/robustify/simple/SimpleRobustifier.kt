package cmu.s3d.fortis.robustify.simple

import cmu.s3d.fortis.robustify.BaseRobustifier
import cmu.s3d.fortis.robustify.acceptsSubWord
import cmu.s3d.fortis.robustify.makeProgress
import cmu.s3d.fortis.robustify.oasis.controlledEvents
import cmu.s3d.fortis.supervisory.SupervisorySynthesizer
import cmu.s3d.fortis.supervisory.asSupDFA
import cmu.s3d.fortis.ts.alphabet
import cmu.s3d.fortis.ts.parallel
import cmu.s3d.fortis.utils.pretty
import net.automatalib.automata.fsa.DFA
import net.automatalib.words.Word
import org.slf4j.LoggerFactory
import java.time.Duration

class SimpleRobustifier<S, I>(
    sys: DFA<*, I>,
    devEnv: DFA<*, I>,
    safety: DFA<*, I>,
    progress: Collection<I>,
    val preferred: Collection<Word<I>>,
    val synthesizer: SupervisorySynthesizer<S, I>
) : BaseRobustifier<S, I>(sys, devEnv, safety) {
    private val logger = LoggerFactory.getLogger(javaClass)
    private val plant = parallel(sys, devEnv)
    private val prop: DFA<*, I>


    override var numberOfSynthesis: Int = 0

    init {
        val progressProp = progress.map { makeProgress(it) }
        prop = parallel(safety, *progressProp.toTypedArray())
    }

    override fun synthesize(): DFA<S, I>? {
        return synthesize(sys.alphabet(), sys.alphabet())
    }

    fun synthesize(controllable: Collection<I>, observable: Collection<I>): DFA<S, I>? {
        if (!observable.containsAll(controllable))
            error("The controllable events should be a subset of the observable events.")

        logger.info("Number of controllable events: ${controllable.size}")
        logger.info("Controllable: $controllable")
        logger.info("Number of observable events: ${observable.size}")
        logger.info("Observable: $observable")

        val startTime = System.currentTimeMillis()
        val g = plant.asSupDFA(controllable, observable)
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
        } else {
            logger.info("No solution found!")
        }
        logger.info("Termination time: ${Duration.ofMillis(System.currentTimeMillis() - startTime).pretty()}")

        return sup
    }

}