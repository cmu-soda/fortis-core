package cmu.s3d.fortis.service.impl

import cmu.s3d.fortis.common.*
import cmu.s3d.fortis.robustness.BaseCalculator
import cmu.s3d.fortis.robustness.explanation.BaseExplanationGenerator
import cmu.s3d.fortis.service.RobustnessComputationService
import cmu.s3d.fortis.supervisory.asDetLTS
import cmu.s3d.fortis.supervisory.asLTS
import cmu.s3d.fortis.supervisory.desops.parseFSM
import cmu.s3d.fortis.ts.DetLTS
import cmu.s3d.fortis.ts.LTS
import cmu.s3d.fortis.ts.alphabet
import cmu.s3d.fortis.ts.lts.CompactLTS
import cmu.s3d.fortis.ts.lts.asLTS
import cmu.s3d.fortis.ts.lts.ltsa.LTSACall
import cmu.s3d.fortis.ts.lts.ltsa.LTSACall.asDetLTS
import cmu.s3d.fortis.ts.lts.ltsa.LTSACall.asLTS
import cmu.s3d.fortis.ts.lts.ltsa.LTSACall.compose
import cmu.s3d.fortis.ts.lts.ltsa.writeFSP
import cmu.s3d.fortis.ts.parallel
import net.automatalib.automaton.fsa.CompactNFA
import net.automatalib.serialization.aut.AUTWriter
import net.automatalib.word.Word
import org.slf4j.LoggerFactory
import java.io.ByteArrayOutputStream

class RobustnessComputationServiceImpl : RobustnessComputationService {
    private val logger = LoggerFactory.getLogger(javaClass)

    override fun compareRobustnessOfTwoProps(
        sysSpecs: List<Spec>,
        envSpecs: List<Spec>,
        prop1Specs: List<Spec>,
        prop2Specs: List<Spec>,
        devSpecs: List<Spec>,
        options: RobustnessOptions
    ): List<EquivClassRep> {
        val start = System.currentTimeMillis()
        val sys = parseSpecs(sysSpecs)
        val env = parseSpecs(envSpecs)
        val dev = if (devSpecs.isEmpty()) null else parseSpecs(devSpecs)

        val cal1 = BaseCalculator(
            sys,
            env,
            parseSpecs(prop1Specs, true) as DetLTS<Int, String>,
            options
        )
        val cal2 = BaseCalculator(
            sys,
            env,
            parseSpecs(prop2Specs, true) as DetLTS<Int, String>,
            options
        )
        val explainer = if (dev != null) BaseExplanationGenerator(sys, dev) else null
        val equivClassMap = cal1.compare(cal2)
        logger.info("Found ${equivClassMap.size} equivalence classes in ${System.currentTimeMillis() - start}ms")
        return equivClassMap.map { (_, reps) ->
            reps.map {
                RepWithExplain(
                    it.copy(word = it.word.asSerializableWord()),
                    explainer?.generate(it.word, cal1.weakestAssumption.alphabet())?.asSerializableWord()
                )
            }
        }
    }

    override fun compareRobustnessOfTwoSystems(
        sys1Specs: List<Spec>,
        sys2Specs: List<Spec>,
        envSpecs: List<Spec>,
        propSpecs: List<Spec>,
        devSpecs: List<Spec>,
        options: RobustnessOptions
    ): List<EquivClassRep> {
        val start = System.currentTimeMillis()
        val sys1 = parseSpecs(sys1Specs)
        val sys2 = parseSpecs(sys2Specs)
        val env = parseSpecs(envSpecs)
        val prop = parseSpecs(propSpecs, true) as DetLTS<Int, String>
        val dev = if (devSpecs.isEmpty()) null else parseSpecs(devSpecs)

        val cal1 = BaseCalculator(
            sys1,
            env,
            prop,
            options
        )
        val cal2 = BaseCalculator(
            sys2,
            env,
            prop,
            options
        )
        val explainer = if (dev != null) BaseExplanationGenerator(sys1, dev) else null
        val equivClassMap = cal1.compare(cal2)
        logger.info("Found ${equivClassMap.size} equivalence classes in ${System.currentTimeMillis() - start}ms")
        return equivClassMap.map { (_, reps) ->
            reps.map {
                RepWithExplain(
                    it.copy(word = it.word.asSerializableWord()),
                    explainer?.generate(it.word, cal1.weakestAssumption.alphabet())?.asSerializableWord()
                )
            }
        }
    }

    override fun computeIntolerableBeh(
        sysSpecs: List<Spec>,
        envSpecs: List<Spec>,
        propSpecs: List<Spec>,
        devSpecs: List<Spec>,
        options: RobustnessOptions
    ): List<EquivClassRep> {
        val start = System.currentTimeMillis()
        val sys = parseSpecs(sysSpecs)
        val env = parseSpecs(envSpecs)
        val prop = parseSpecs(propSpecs, true) as DetLTS<Int, String>
        val dev = if (devSpecs.isEmpty()) null else parseSpecs(devSpecs)

        val cal = BaseCalculator(
            sys,
            env,
            prop,
            options
        )
        val explainer = if (dev != null) BaseExplanationGenerator(sys, dev) else null
        val equivClassMap = cal.computeUnsafeBeh()
        logger.info("Found ${equivClassMap.size} equivalence classes in ${System.currentTimeMillis() - start}ms")
        return equivClassMap.map { (_, reps) ->
            reps.map {
                RepWithExplain(
                    it.copy(word = it.word.asSerializableWord()),
                    explainer?.generate(it.word, cal.weakestAssumption.alphabet())?.asSerializableWord()
                )
            }
        }
    }

    fun computeSTPARob(
        sysSpecs: List<Spec>,
        envSpecs: List<Spec>,
        propSpecs: List<Spec>,
        devSpecs: List<Spec>,
        options: RobustnessOptions
    ): List<EquivClassRep> {
        val start = System.currentTimeMillis()
        val sys = parseSpecs(sysSpecs)
        val env = parseSpecs(envSpecs)
        val prop = parseSpecs(propSpecs, true) as DetLTS<Int, String>
        val dev = if (devSpecs.isEmpty()) null else parseSpecs(devSpecs)

        // not providing causes an error
        val notProvidingCausesError = mutableSetOf<Pair<String, Word<String>>>()
        val causalAlphabet = env.alphabet().filter { !it.equals("wait") }
        val safeStates = env.states - env.errorState
        for (state in safeStates) {
            val deviatedEnv = env as CompactLTS<String>
            if (state in deviatedEnv.getTransitions(state,"wait")) {
                continue
            }
            val outgoingActs = causalAlphabet.filter { !deviatedEnv.getTransitions(state,it).isEmpty() }
            deviatedEnv.addTransition(state,"wait",state)
            val cal = BaseCalculator(
                sys,
                deviatedEnv,
                prop,
                options
            )
            val equivClassMap = cal.computeEnvUnsafeBeh()
            logger.info("Found ${equivClassMap.size} equivalence classes in ${System.currentTimeMillis() - start}ms")
            equivClassMap.map { (_, reps) ->
                reps.forEach {
                    for (act in outgoingActs) {
                        notProvidingCausesError.add(Pair(act,it.word))
                    }
                }
            }
            deviatedEnv.removeTransition(state,"wait",state)
        }

        notProvidingCausesError.forEach {
            // <safeTrace> is the maximum trace accepted by <env> and takes the safe action <it.first>
            // we only care about this safe trace when it is also accepted by <env>
            val safeTrace = maxTraceAccpeted(env, it.second).append(it.first)
            if (env.accepts(safeTrace)) {
                println("Not providing \"${it.first}\" can cause an error!")
                println("  error: ${it.second}")
                println("  safe when provided: $safeTrace")
            }
        }

        return listOf()
    }

    fun maxTraceAccpeted(lts : LTS<Int,String>, trace: Word<String>) : Word<String> {
        for (i in trace.size() downTo 0) {
            val maxWord = trace.prefix(i)
            if (lts.accepts(maxWord)) {
                return maxWord
            }
        }
        return Word.epsilon()
    }

    override fun computeRobustness(
        sysSpecs: List<Spec>,
        envSpecs: List<Spec>,
        propSpecs: List<Spec>,
        devSpecs: List<Spec>,
        options: RobustnessOptions
    ): List<EquivClassRep> {
        val start = System.currentTimeMillis()
        val sys = parseSpecs(sysSpecs)
        val env = parseSpecs(envSpecs)
        val prop = parseSpecs(propSpecs, true) as DetLTS<Int, String>
        val dev = if (devSpecs.isEmpty()) null else parseSpecs(devSpecs)

        val cal = BaseCalculator(
            sys,
            env,
            prop,
            options
        )
        val explainer = if (dev != null) BaseExplanationGenerator(sys, dev) else null
        val equivClassMap = cal.computeRobustness()
        logger.info("Found ${equivClassMap.size} equivalence classes in ${System.currentTimeMillis() - start}ms")
        return equivClassMap.map { (_, reps) ->
            reps.map {
                RepWithExplain(
                    it.copy(word = it.word.asSerializableWord()),
                    explainer?.generate(it.word, cal.weakestAssumption.alphabet())?.asSerializableWord()
                )
            }
        }
    }

    override fun computeWeakestAssumption(
        sysSpecs: List<Spec>,
        envSpecs: List<Spec>,
        propSpecs: List<Spec>,
        options: RobustnessOptions,
        outputFormat: SpecType
    ): String {
        val start = System.currentTimeMillis()
        val sys = parseSpecs(sysSpecs)
        val env = parseSpecs(envSpecs)
        val prop = parseSpecs(propSpecs, true) as DetLTS<Int, String>

        val cal = BaseCalculator(
            sys,
            env,
            prop,
            options
        )
        val wa = cal.weakestAssumption
        logger.info("Found weakest assumption in ${System.currentTimeMillis() - start}ms")
        return ByteArrayOutputStream().use {
            when (outputFormat) {
                SpecType.FSP -> writeFSP(it, wa, wa.alphabet())
                SpecType.AUT -> AUTWriter.writeAutomaton(wa, wa.alphabet(), it)
                else -> error("Unsupported output format")
            }
            it.toString()
        }
    }

    private fun parseSpec(spec: Spec, deterministic: Boolean = false): LTS<Int, String> {
        return when (spec.type) {
            SpecType.FSP -> {
                LTSACall.compile(spec.content).compose().let {
                    if (deterministic) it.asDetLTS() else it.asLTS()
                }
            }
            SpecType.FSM -> {
                parseFSM(spec.content).let { if (deterministic) it.asDetLTS() else it.asLTS() }
            }
            SpecType.FLTL -> {
                val fltlRegex = "assert\\s+(\\w+)\\s*=".toRegex()
                val name = fltlRegex.find(spec.content)?.groupValues?.get(1)
                    ?: error("FLTL spec must have an assert name")
                LTSACall.compileSafetyLTL(spec.content, name).asDetLTS()
            }
            else -> error("Unsupported spec type")
        }
    }

    private fun parseSpecs(specs: List<Spec>, deterministic: Boolean = false): LTS<Int, String> {
        if (specs.isEmpty()) error("Specs cannot be empty")
        if (specs.size == 1) return parseSpec(specs.first(), deterministic)
        return parallel(*specs.map { parseSpec(it, deterministic) }.toTypedArray())
    }
}