package cmu.s3d.fortis.cli

import cmu.s3d.fortis.common.EquivClassRep
import cmu.s3d.fortis.common.RobustnessOptions
import cmu.s3d.fortis.common.Spec
import cmu.s3d.fortis.common.SpecType
import cmu.s3d.fortis.service.impl.RobustnessComputationServiceImpl
import cmu.s3d.fortis.utils.pretty
import com.fasterxml.jackson.annotation.JsonProperty
import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import com.github.ajalt.clikt.core.CliktCommand
import com.github.ajalt.clikt.parameters.options.flag
import com.github.ajalt.clikt.parameters.options.option
import com.github.ajalt.clikt.parameters.options.split
import org.slf4j.LoggerFactory
import java.io.File
import java.time.Duration
import kotlin.system.exitProcess

class Robustness : CliktCommand(help = "Compute the robustness of a system design.") {

    // specs
    private val sys by option("--sys", "-s", help = "The model of the system.")
    private val env by option("--env", "-e", help = "The model of the environment.")
    private val prop by option("--prop", "-p", help = "The model of the safety property.")
    private val dev by option("--dev", "-d", help = "The model of the deviation model for explanation.")
    private val jsons by option("--jsons", help = "One or more model config files, separated by ','.").split(",")

    // function modes
    private val unsafe by option("--unsafe", help = "Generate unsafe behaviors.").flag()
    private val compareSys by option("--compare-sys", help = "Compare the robustness of two system models.").flag()
    private val compareProp by option("--compare-prop", help = "Compare the robustness of one system under two properties.").flag()
    private val generateWA by option("--wa", help = "Output the weakest assumption model.").flag()

    // options
    private val expand by option("--expand", help = "Expand the equivalence classes to all acyclic traces.").flag()
    private val disables by option("--disables", help = "Add a sink state to include disabled actions.").flag()
    private val minimized by option("--minimized", help = "Minimize the weakest assumption model.").flag()

    private val logger = LoggerFactory.getLogger(javaClass)
    private val robustnessComputationService = RobustnessComputationServiceImpl()

    private data class Problem(
        val sys: List<Spec>,
        val env: List<Spec>,
        val prop: List<Spec>,
        val dev: List<Spec>
    )

    override fun run() {
        val problems: List<Problem> = if (jsons != null) {
            jsons!!.map { parseJSONConfig(it) }
        } else if (sys != null && env != null && prop != null) {
            listOf(
                Problem(
                    listOf(readSpecFile(sys!!)),
                    listOf(readSpecFile(env!!)),
                    listOf(readSpecFile(prop!!)),
                    listOfNotNull(dev?.let { readSpecFile(it) })
                )
            )
        } else {
            println(getFormattedHelp())
            exitProcess(0)
        }
        val options = RobustnessOptions(expand, minimized, disables)
        val start = System.currentTimeMillis()

        if (compareSys) {
            if (problems.size < 2)
                error("Must provide two configs for robustness comparison.")
            compareSys(problems[0], problems[1], options)
        } else if (compareProp) {
            if (problems.size < 2)
                error("Must provide two configs for robustness comparison.")
            compareProp(problems[0], problems[1], options)
        } else if (generateWA) {
            val out = robustnessComputationService.computeWeakestAssumption(
                problems[0].sys,
                problems[0].env,
                problems[0].prop,
                options,
                SpecType.FSP
            )
            logger.info("Weakest assumption model:\n\n$out")
        } else if (unsafe) {
            val re = robustnessComputationService.computeIntolerableBeh(
                problems[0].sys,
                problems[0].env,
                problems[0].prop,
                problems[0].dev,
                options
            )
            logResult(re)
        } else {
            val re = robustnessComputationService.computeRobustness(
                problems[0].sys,
                problems[0].env,
                problems[0].prop,
                problems[0].dev,
                options
            )
            logResult(re)
        }

        logger.info("Total time: ${Duration.ofMillis(System.currentTimeMillis() - start).pretty()}")
    }

    private fun compareSys(a: Problem, b: Problem, options: RobustnessOptions) {
        logger.info("Comparing the robustness of a to b...")
        val re1 = robustnessComputationService.compareRobustnessOfTwoSystems(
            a.sys,
            b.sys,
            a.env,
            a.prop,
            a.dev,
            options
        )
        logger.info("Results: a is robust than b in that:")
        logResult(re1)

        logger.info("Comparing the robustness of b to a...")
        val re2 = robustnessComputationService.compareRobustnessOfTwoSystems(
            b.sys,
            a.sys,
            b.env,
            b.prop,
            b.dev,
            options
        )
        logger.info("Results: b is robust than a in that:")
        logResult(re2)
    }

    private fun compareProp(a: Problem, b: Problem, options: RobustnessOptions) {
        logger.info("Comparing the robustness of a to b...")
        val re1 = robustnessComputationService.compareRobustnessOfTwoProps(
            a.sys,
            a.env,
            a.prop,
            b.prop,
            a.dev,
            options
        )
        logger.info("Results: a is robust than b in that:")
        logResult(re1)

        logger.info("Comparing the robustness of b to a...")
        val re2 = robustnessComputationService.compareRobustnessOfTwoProps(
            b.sys,
            b.env,
            b.prop,
            a.prop,
            b.dev,
            options
        )
        logger.info("Results: b is robust than a in that:")
        logResult(re2)
    }

    private fun logResult(equivClasses: List<EquivClassRep>) {
        for (i in equivClasses.indices) {
            logger.info("Equivalence class '${i+1}':")
            for (rep in equivClasses[i]) {
                if (rep.explanation != null)
                    logger.info("\t${rep.rep} => ${rep.explanation}")
                else
                    logger.info("\t${rep.rep}")
            }
        }
    }

    private fun readSpecFile(path: String): Spec {
        val f = File(path)
        val content = f.readText()
        val fltlRegex = "assert\\s+\\w+\\s*=".toRegex()
        return when (f.extension) {
            "lts" -> if (fltlRegex.matches(content)) Spec(SpecType.FLTL, content) else Spec(SpecType.FSP, content)
            "fsm" -> Spec(SpecType.FSM, content)
            "aut" -> Spec(SpecType.AUT, content)
            else -> error("Unsupported file type '.${f.extension}'")
        }
    }

    private fun parseJSONConfig(path: String): Problem {
        val obj = jacksonObjectMapper().readValue(File(path), RobustnessConfigJSON::class.java)
        return Problem(
            obj.sys.map { readSpecFile(it) },
            obj.env.map { readSpecFile(it) },
            obj.prop.map { readSpecFile(it) },
            obj.dev?.map { readSpecFile(it) } ?: emptyList()
        )
    }
}

private data class RobustnessConfigJSON(
    @JsonProperty
    val sys: List<String>,
    @JsonProperty
    val env: List<String>,
    @JsonProperty
    val prop: List<String>,
    @JsonProperty
    val dev: List<String>?,
)