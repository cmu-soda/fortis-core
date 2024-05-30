package cmu.s3d.fortis.robustify.supervisory

import cmu.s3d.fortis.common.Algorithms
import cmu.s3d.fortis.common.Priority
import cmu.s3d.fortis.robustify.BaseRobustifier
import cmu.s3d.fortis.supervisory.SupervisoryDFA
import cmu.s3d.fortis.supervisory.SupervisorySynthesizer
import cmu.s3d.fortis.supervisory.asSupDFA
import cmu.s3d.fortis.supervisory.observer
import cmu.s3d.fortis.ts.*
import net.automatalib.automaton.fsa.CompactDFA
import net.automatalib.automaton.fsa.DFA
import net.automatalib.word.Word
import org.slf4j.LoggerFactory
import java.io.Closeable

class WeightsMap(
    val preferred: Map<Word<String>, Int>,
    val controllable: Map<String, Int>,
    val observable: Map<String, Int>
)

class Solution(
    val model: DFA<Int, String>,
    val preferred: List<Word<String>>,
    val controllable: List<String>,
    val observable: List<String>
)

class SupervisoryRobustifier(
    sys: DFA<*, String>,
    devEnv: DFA<*, String>,
    safety: DFA<*, String>,
    progress: Collection<String>,
    val preferredMap: Map<Priority, Collection<Word<String>>>,
    val controllableMap: Map<Priority, Collection<String>>,
    val observableMap: Map<Priority, Collection<String>>,
    val synthesizer: SupervisorySynthesizer<Int, String>,
    val maxIter: Int = 1
) : BaseRobustifier(sys, devEnv, safety),
    Closeable {
    var optimization: Boolean = true

    private val logger = LoggerFactory.getLogger(javaClass)
    private val plant = parallel(sys, devEnv)
    private val prop: DFA<*, String>
    private val synthesisCache = mutableMapOf<Pair<Collection<String>, Collection<String>>, SupervisoryDFA<Int, String>?>()
    private val checkPreferredCache = mutableMapOf<Triple<Collection<String>, Collection<String>, Word<String>>, Boolean>()

    override var numberOfSynthesis: Int = 0

    init {
        val extendedSafety = extendAlphabet(safety, safety.alphabet(), plant.alphabet())
        val progressProp = progress.map { makeProgress(it) }
        prop = parallel(extendedSafety, *progressProp.toTypedArray())
//        val mustHaveProp = preferredMap[Priority.P0]?.map { makeMustHaveBehavior(it) } ?: emptyList()
//        prop = parallel(extendedSafety, *progressProp.toTypedArray(), *mustHaveProp.toTypedArray())
    }

    override fun close() {
        synthesizer.close()
    }

    override fun synthesize(): DFA<Int, String>? {
        return synthesize(Algorithms.Pareto).firstOrNull()
    }

    fun synthesize(alg: Algorithms): Iterable<DFA<Int, String>> {
        return synthesize2(alg).map { it.model }
    }

    fun synthesize2(alg: Algorithms): Iterable<Solution> {
        logger.info("Number of states of the system: ${sys.states.size}")
        logger.info("Number of states of the environment: ${devEnv.states.size}")
        logger.info("Number of states of the plant (S || E): ${plant.states.size}")
        logger.info("Number of transitions of the plant: ${plant.numOfTransitions()}")

        return SolutionIterator(this, alg, maxIter)
    }

    /**
     * @return the observed(Sup || G) model
     */
    fun supervisorySynthesize(controllable: Collection<String>, observable: Collection<String>): SupervisoryDFA<Int, String>? {
        val key = Pair(controllable, observable)
        if (!optimization || key !in synthesisCache) {
            val g = plant.asSupDFA(controllable, observable)
            val p = prop.asSupDFA(controllable, observable)

            logger.debug("Start supervisory controller synthesis...")
            synthesisCache[key] = synthesizer.synthesize(g, p)
            numberOfSynthesis++
            logger.debug("Controller synthesis completed.")
        } else {
            logger.debug("Synthesis cache hit: $key")
        }
        return synthesisCache[key]
    }

    /**
     * Given the priority ranking that the user provides, compute the positive utilities for preferred behavior
     * and the negative cost for making certain events controllable and/or observable.
     * @return dictionary with this information.
     */
    fun computeWeights(): WeightsMap {
        val preferred = mutableMapOf<Word<String>, Int>()
        val controllable = mutableMapOf<String, Int>()
        val observable = mutableMapOf<String, Int>()

        var totalWeight = 0
        // compute new weight in order to maintain hierarchy by sorting absolute value sum of previous weights
        for (p in listOf(Priority.P0, Priority.P1, Priority.P2, Priority.P3)) {
            val curWeight = totalWeight + 1
            if (p in preferredMap) {
                for (word in preferredMap[p]!!) {
                    if (p == Priority.P0) {
                        preferred[word] = 0
                    } else {
                        preferred[word] = curWeight
                        totalWeight += curWeight
                    }
                }
            }
            if (p in controllableMap) {
                for (a in controllableMap[p]!!) {
                    if (p == Priority.P0) {
                        controllable[a] = 0
                    } else {
                        controllable[a] = -curWeight
                        totalWeight += curWeight
                    }
                }
            }
            if (p in observableMap) {
                for (a in observableMap[p]!!) {
                    if (p == Priority.P0) {
                        observable[a] = 0
                    } else {
                        observable[a] = -curWeight
                        totalWeight += curWeight
                    }
                }
            }
        }

        return WeightsMap(preferred, controllable, observable)
    }

    /**
     * @param sup the observed(Sup || G) model from the DESops output
     */
    fun checkPreferred(sup: SupervisoryDFA<Int, String>, preferred: Collection<Word<String>>): Collection<Word<String>> {
        val ctrlPlant = parallel(plant, sup).asSupDFA(sup.controllable, sup.observable)
        return preferred.filter { checkPreferred(ctrlPlant, it) }
    }

    /**
     * @param sup the observed(Sup || G) model from the DESops output
     */
    fun satisfyPreferred(sup: SupervisoryDFA<Int, String>, preferred: Collection<Word<String>>): Boolean {
        val ctrlPlant = parallel(plant, sup).asSupDFA(sup.controllable, sup.observable)
        for (p in preferred) {
            if (!checkPreferred(ctrlPlant, p)) {
                return false
            }
        }
        return true
    }

    private fun checkPreferred(sup: SupervisoryDFA<Int, String>, p: Word<String>): Boolean {
        val key = Triple(sup.controllable, sup.observable, p)
        if (key !in checkPreferredCache) {
            logger.debug("Start checking preferred behavior: [$p]")
            val (r, how) = acceptsSubWord(sup, p)
            logger.debug("It is satisfied by $how")
            checkPreferredCache[key] = r
            logger.debug("Preferred behavior check completed: ${checkPreferredCache[key]}.")
        } else {
            logger.debug("CheckPreferred cache hit: $key")
        }
        return checkPreferredCache[key]!!
    }

    /**
     * @param sup the observed(Sup || G) model from the DESops output
     */
    fun constructSupervisor(sup: SupervisoryDFA<Int, String>): SupervisoryDFA<Int, String> {
        val observedPlant = observer(plant.asSupDFA(sup.controllable, sup.observable), plant.alphabet())
        val out = CompactDFA(sup.asDFA() as CompactDFA<String>)
        val supQueue = java.util.ArrayDeque<Int>()
        val visited = mutableSetOf<Int>()
        val plantQueue = java.util.ArrayDeque<Int>()

        supQueue.offer(sup.initialState!!)
        plantQueue.offer(observedPlant.initialState!!)

        while (supQueue.isNotEmpty()) {
            val supState = supQueue.poll()
            val plantState = plantQueue.poll()

            if (supState in visited)
                continue
            visited.add(supState)

            assert(sup.observable.toSet() == sup.alphabet().toSet())
            for (a in sup.observable) {
                val supSucc = sup.getSuccessor(supState, a)
                val plantSucc = observedPlant.getSuccessor(plantState, a)
                if (supSucc != null && plantSucc != null) {
                    supQueue.offer(supSucc)
                    plantQueue.offer(plantSucc)
                } else if (supSucc != null) {
                    // sup has more transitions meaning that this sup is probably constructed.
                    continue
                } else if (a !in sup.controllable) { // uncontrollable event, make admissible
                    out.addTransition(supState, a, supState, null)
                } else if (plantSucc == null) {
                    // controllable but not defined in plant, make redundant
                    out.addTransition(supState, a, supState, null)
                }
            }
        }

        return out.asSupDFA(sup.controllable, sup.observable)
    }

    fun buildSys(sup: SupervisoryDFA<Int, String>): DFA<Int, String> {
        logger.info("Build new design from controller...")
        logger.info("Size of the controller: ${sup.size()} states and ${sup.numOfTransitions()} transitions")
        return parallel(sys, sup)
    }
}