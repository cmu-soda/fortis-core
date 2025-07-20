package cmu.s3d.fortis.service.impl

import cmu.s3d.fortis.common.*
import cmu.s3d.fortis.robustness.BaseCalculator
import cmu.s3d.fortis.robustness.explanation.BaseExplanationGenerator
import cmu.s3d.fortis.service.RobustnessComputationService
import cmu.s3d.fortis.supervisory.asDetLTS
import cmu.s3d.fortis.supervisory.asLTS
import cmu.s3d.fortis.supervisory.desops.parseFSM
import cmu.s3d.fortis.ts.*
import cmu.s3d.fortis.ts.lts.CompactLTS
import cmu.s3d.fortis.ts.lts.asLTS
import cmu.s3d.fortis.ts.lts.hide
import cmu.s3d.fortis.ts.lts.ltsa.LTSACall
import cmu.s3d.fortis.ts.lts.ltsa.LTSACall.asDetLTS
import cmu.s3d.fortis.ts.lts.ltsa.LTSACall.asLTS
import cmu.s3d.fortis.ts.lts.ltsa.LTSACall.compose
import cmu.s3d.fortis.ts.lts.ltsa.writeFSP
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

    class TracePair(private val goodTrace : List<String>,
                    private val badTrace : List<String>) {
        override fun toString() : String {
            val jsonGoodTrace = goodTrace.joinToString(",") { "\"$it\"" }
            val jsonBadTrace = badTrace.joinToString(",") { "\"$it\"" }
            return "{\"goodTrace\":[$jsonGoodTrace],\"badTrace\":[$jsonBadTrace]}"
        }
    }

    fun computeSTPARob(
        sys: LTS<Int,String>,
        env: LTS<Int,String>,
        prop: DetLTS<Int,String>,
        options: RobustnessOptions,
        bound: Int
    ): List<EquivClassRep> {
        val cal = BaseCalculator(
            sys,
            env,
            prop,
            options
        )
        val errTraces = cal.computeBoundedUnsafeBeh(bound)
        val tracePairs = mutableListOf<TracePair>()
        for (errTrace in errTraces) {
            val safeTracePrefix = maxTraceAccpeted(env, errTrace)
            val safeTrace = envExtendTrace(env, safeTracePrefix)
            tracePairs.add(TracePair(safeTrace.asList(), errTrace.asList()))
        }

        val jsonContents = tracePairs.joinToString { it.toString() }
        println("[$jsonContents]")

        return listOf()
    }

    /**
     * Attempts to extend the trace <trace> with one extra action in <env>'s alphabet that <env> accepts. Assumes
     * that <env> accepts <trace>.
     */
    private fun envExtendTrace(env : LTS<Int,String>, trace : Word<String>) : Word<String> {
        for (a in env.alphabet()) {
            var extendedTrace = Word.fromWords(trace, Word.fromLetter(a))
            if (env.accepts(extendedTrace)) {
                return extendedTrace
            }
        }
        return trace
    }

    private fun maxTraceAccpeted(lts : LTS<Int,String>, trace: Word<String>) : Word<String> {
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
}

fun parseSpec(spec: Spec, deterministic: Boolean = false): LTS<Int, String> {
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

fun parseSpecs(specs: List<Spec>, deterministic: Boolean = false): LTS<Int, String> {
    if (specs.isEmpty()) error("Specs cannot be empty")
    if (specs.size == 1) return parseSpec(specs.first(), deterministic)
    return parallel(*specs.map { parseSpec(it, deterministic) }.toTypedArray())
}
