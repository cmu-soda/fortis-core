package cmu.s3d.fortis.service.impl

import cmu.s3d.fortis.common.*
import cmu.s3d.fortis.robustify.supervisory.SupervisoryRobustifier
import cmu.s3d.fortis.service.RobustificationService
import cmu.s3d.fortis.supervisory.SupervisoryDFA
import cmu.s3d.fortis.supervisory.desops.parseFSM
import cmu.s3d.fortis.supervisory.supremica.SupremicaRunner
import cmu.s3d.fortis.ts.alphabet
import cmu.s3d.fortis.ts.lts.ltsa.LTSACall
import cmu.s3d.fortis.ts.lts.ltsa.LTSACall.asDetLTS
import cmu.s3d.fortis.ts.lts.ltsa.LTSACall.compose
import cmu.s3d.fortis.ts.lts.ltsa.writeFSP
import cmu.s3d.fortis.ts.parallel
import net.automatalib.automaton.fsa.DFA
import net.automatalib.serialization.aut.AUTWriter
import java.io.ByteArrayOutputStream

class RobustificationServiceImpl : RobustificationService {
    override fun robustify(
        sysSpecs: List<Spec>,
        envSpecs: List<Spec>,
        propSpecs: List<Spec>,
        options: SupervisoryOptions,
        outputFormat: SpecType
    ): List<RobustificationResult> {
        val robustifier = SupervisoryRobustifier(
            parseSpecs(sysSpecs),
            parseSpecs(envSpecs),
            parseSpecs(propSpecs),
            progress = options.progress,
            preferredMap = options.preferredBeh,
            controllableMap = options.controllable,
            observableMap = options.observable,
            synthesizer = SupremicaRunner(),
            maxIter = options.maxIter
        )
        val sols = robustifier.use {
            robustifier.synthesize2(options.algorithm).toList()
        }
        return sols.map { sol ->
            val model = ByteArrayOutputStream().use {
                when (outputFormat) {
                    SpecType.FSP -> writeFSP(it, sol.model, sol.model.alphabet())
                    SpecType.AUT -> AUTWriter.writeAutomaton(sol.model, sol.model.alphabet(), it)
                    else -> error("Unsupported output format")
                }
                it.toString()
            }
            RobustificationResult(
                model,
                sol.preferred.map { it.asSerializableWord() },
                sol.controllable,
                sol.observable
            )
        }
    }

    private fun parseSpec(spec: Spec): DFA<Int, String> {
        return when (spec.type) {
            SpecType.FSP -> {
                LTSACall.compile(spec.content).compose().asDetLTS()
            }
            SpecType.FSM -> {
                parseFSM(spec.content) as SupervisoryDFA
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

    private fun parseSpecs(specs: List<Spec>): DFA<Int, String> {
        if (specs.isEmpty()) error("Specs cannot be empty")
        if (specs.size == 1) return parseSpec(specs.first())
        return parallel(*specs.map { parseSpec(it) }.toTypedArray())
    }
}