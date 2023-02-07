package cmu.isr.robustify.simple

import cmu.isr.robustify.BaseRobustifier
import cmu.isr.robustify.oasis.controlledEvents
import cmu.isr.robustify.supervisory.acceptsSubWord
import cmu.isr.robustify.supervisory.makeProgress
import cmu.isr.supervisory.SupervisorySynthesizer
import cmu.isr.supervisory.asSupDFA
import cmu.isr.ts.alphabet
import cmu.isr.ts.parallel
import cmu.isr.utils.pretty
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
) : BaseRobustifier<S, I>(sys, devEnv, safety)
{
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