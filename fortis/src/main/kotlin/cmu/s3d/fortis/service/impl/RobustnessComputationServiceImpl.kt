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
import cmu.s3d.fortis.ts.lts.ltsa.LTSACall
import cmu.s3d.fortis.ts.lts.ltsa.LTSACall.asDetLTS
import cmu.s3d.fortis.ts.lts.ltsa.LTSACall.asLTS
import cmu.s3d.fortis.ts.lts.ltsa.LTSACall.compose
import cmu.s3d.fortis.ts.lts.ltsa.writeFSP
import cmu.s3d.fortis.ts.parallel
import net.automatalib.serialization.aut.AUTWriter
import java.io.ByteArrayOutputStream

class RobustnessComputationServiceImpl : RobustnessComputationService {
    override fun compareRobustnessOfTwoProps(
        sysSpecs: List<Spec>,
        envSpecs: List<Spec>,
        prop1Specs: List<Spec>,
        prop2Specs: List<Spec>,
        devSpecs: List<Spec>,
        options: RobustnessOptions
    ): List<EquivClassRep> {
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
        return equivClassMap.map { (_, reps) ->
            reps.map {
                RepWithExplain(
                    it,
                    explainer?.generate(it.word, cal1.weakestAssumption.alphabet())
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
        return equivClassMap.map { (_, reps) ->
            reps.map {
                RepWithExplain(
                    it,
                    explainer?.generate(it.word, cal1.weakestAssumption.alphabet())
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
        return equivClassMap.map { (_, reps) ->
            reps.map {
                RepWithExplain(
                    it,
                    explainer?.generate(it.word, cal.weakestAssumption.alphabet())
                )
            }
        }
    }

    override fun computeRobustness(
        sysSpecs: List<Spec>,
        envSpecs: List<Spec>,
        propSpecs: List<Spec>,
        devSpecs: List<Spec>,
        options: RobustnessOptions
    ): List<EquivClassRep> {
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
        return equivClassMap.map { (_, reps) ->
            reps.map {
                RepWithExplain(
                    it,
                    explainer?.generate(it.word, cal.weakestAssumption.alphabet())
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
        val out = ByteArrayOutputStream().use {
            when (outputFormat) {
                SpecType.FSP -> writeFSP(it, wa, wa.alphabet())
                SpecType.AUT -> AUTWriter.writeAutomaton(wa, wa.alphabet(), it)
                else -> error("Unsupported output format")
            }
        }
        return out.toString()
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