package cmu.isr.app

import cmu.isr.robustify.oasis.OASISRobustifier
import cmu.isr.robustify.simple.SimpleRobustifier
import cmu.isr.robustify.supervisory.Algorithms
import cmu.isr.robustify.supervisory.Priority
import cmu.isr.robustify.supervisory.SupervisoryRobustifier
import cmu.isr.supervisory.SupervisoryDFA
import cmu.isr.supervisory.desops.DESopsRunner
import cmu.isr.supervisory.supremica.SupremicaRunner
import cmu.isr.ts.alphabet
import cmu.isr.ts.lts.ltsa.LTSACall
import cmu.isr.ts.lts.ltsa.LTSACall.asDetLTS
import cmu.isr.ts.lts.ltsa.LTSACall.compose
import cmu.isr.ts.lts.ltsa.write
import cmu.isr.ts.parallel
import cmu.isr.utils.pretty
import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import com.github.ajalt.clikt.core.CliktCommand
import com.github.ajalt.clikt.parameters.arguments.argument
import com.github.ajalt.clikt.parameters.options.default
import com.github.ajalt.clikt.parameters.options.flag
import com.github.ajalt.clikt.parameters.options.option
import net.automatalib.automata.fsa.DFA
import net.automatalib.serialization.aut.AUTWriter
import net.automatalib.words.Word
import org.apache.logging.log4j.Level
import org.apache.logging.log4j.LogManager
import org.apache.logging.log4j.core.config.Configurator
import org.slf4j.LoggerFactory
import java.io.File
import java.time.Duration

class Robustify : CliktCommand(help = "Robustify a system design using supervisory control.") {
  private val configFile by argument(name = "<config.json>")
  private val verbose by option("--verbose", "-v", help = "Enable verbose mode.").flag()
  private val output by option("--output", "-o", help = "Output file format.").default("aut")

  private val logger = LoggerFactory.getLogger(javaClass)

  override fun run() {
    if (verbose) {
//      System.setProperty(org.slf4j.impl.SimpleLogger.DEFAULT_LOG_LEVEL_KEY, "Debug")
      Configurator.setAllLevels(LogManager.getRootLogger().name, Level.DEBUG)
    }

    val config = jacksonObjectMapper().readValue(File(configFile), RobustifyConfigJSON::class.java)
    val startTime = System.currentTimeMillis()

    val (robustifier, sols) = when (config.method) {
      "supervisory-non-opt" -> {
        val robustifer = buildSupervisory(config)
        robustifer.optimization = false
        val sols = robustifer.synthesize(Algorithms.valueOf(config.options.algorithm)).toList()
        robustifer.close()
        Pair(robustifer, sols)
      }
      "supervisory" -> {
        val robustifer = buildSupervisory(config)
        val sols = robustifer.synthesize(Algorithms.valueOf(config.options.algorithm)).toList()
        robustifer.close()
        Pair(robustifer, sols)
      }
      "oasis" -> {
        val robustifier = buildOASIS(config)
        val sol = if (config.options.controllable.isEmpty() || config.options.observable.isEmpty()) {
          robustifier.synthesize()
        } else {
          robustifier.synthesize(config.options.controllable, config.options.observable)
        }
        Pair(robustifier, if (sol != null) listOf(sol) else emptyList())
      }
      "simple" -> {
        val robustifier = buildSimple(config)
        val sol = if (config.options.controllable.isEmpty() || config.options.observable.isEmpty()) {
          robustifier.synthesize()
        } else {
          robustifier.synthesize(config.options.controllable, config.options.observable)
        }
        Pair(robustifier, if (sol != null) listOf(sol) else emptyList())
      }
      else -> error("Unsupported method, should be either 'supervisory', 'oasis', or 'simple'.")
    }

    logger.info("Total number of controller synthesis invoked: ${robustifier.numberOfSynthesis}")
    if (sols.isNotEmpty()) {
      logger.info("Total number of solutions: ${sols.size}")
      saveSolutions(sols)
    } else {
      logger.warn("Failed to find a solution.")
    }
    logger.info("Robustification completes, total time: ${Duration.ofMillis(System.currentTimeMillis() - startTime).pretty()}")
  }

  private fun parseSpecFile(path: String): DFA<*, String> {
    val f = File(path)
    return when (f.extension) {
      "lts" -> LTSACall.compile(f.readText()).compose().asDetLTS()
      "fsm" -> cmu.isr.supervisory.desops.parse(f.bufferedReader()) as SupervisoryDFA
      else -> error("Unsupported file type '.${f.extension}'")
    }
  }

  private fun parseSpecFiles(paths: List<String>): DFA<*, String> {
    if (paths.isEmpty())
      error("Should provide at least one model file")
    if (paths.size == 1)
      return parseSpecFile(paths[0])
    return parallel(*paths.map { parseSpecFile(it) }.toTypedArray())
  }

  private fun saveSolutions(dfas: List<DFA<*, String>>) {
    val dir = File("./solutions")
    if (dir.exists())
      dir.deleteRecursively()
    dir.mkdir()
    when (output) {
      "aut" -> saveSolutionsAUT(dfas)
      "fsp" -> saveSolutionsFSP(dfas)
    }
  }

  private fun saveSolutionsAUT(dfas: List<DFA<*, String>>) {
    for (i in dfas.indices) {
      val f = File("./solutions/sol${i+1}.aut")
      f.createNewFile()
      val out = f.outputStream()
      AUTWriter.writeAutomaton(dfas[i], dfas[i].alphabet(), out)
      out.close()
    }
  }

  private fun saveSolutionsFSP(dfas: List<DFA<*, String>>) {
    for (i in dfas.indices) {
      val f = File("./solutions/sol${i+1}.lts")
      f.createNewFile()
      val out = f.outputStream()
      write(out, dfas[i], dfas[i].alphabet())
      out.close()
    }
  }

  fun buildSupervisory(config: RobustifyConfigJSON): SupervisoryRobustifier<String> {
    val sys = parseSpecFiles(config.sys)
    val dev = parseSpecFiles(config.dev)
    val safety = parseSpecFiles(config.safety)
    return SupervisoryRobustifier(
      sys,
      dev,
      safety,
      progress = config.options.progress,
      preferredMap = config.options.preferredMap.map { entry ->
        when (entry.key) {
          "0" -> Priority.P0 to entry.value.map { Word.fromList(it) }
          "1" -> Priority.P1 to entry.value.map { Word.fromList(it) }
          "2" -> Priority.P2 to entry.value.map { Word.fromList(it) }
          "3" -> Priority.P3 to entry.value.map { Word.fromList(it) }
          else -> error("Unsupported priority $entry")
        }
      }.toMap(),
      controllableMap = config.options.controllableMap.map { entry ->
        when (entry.key) {
          "0" -> Priority.P0 to entry.value
          "1" -> Priority.P1 to entry.value
          "2" -> Priority.P2 to entry.value
          "3" -> Priority.P3 to entry.value
          else -> error("Unsupported priority $entry")
        }
      }.toMap(),
      observableMap = config.options.observableMap.map { entry ->
        when (entry.key) {
          "0" -> Priority.P0 to entry.value
          "1" -> Priority.P1 to entry.value
          "2" -> Priority.P2 to entry.value
          "3" -> Priority.P3 to entry.value
          else -> error("Unsupported priority $entry")
        }
      }.toMap(),
      synthesizer = when (SolverType.valueOf(config.options.solver)) {
        SolverType.Supremica -> SupremicaRunner()
        SolverType.DESops -> DESopsRunner()
      },
      maxIter = config.options.maxIter
    )
  }

  fun buildOASIS(config: RobustifyConfigJSON): OASISRobustifier<Int, String> {
    val sys = parseSpecFiles(config.sys)
    val dev = parseSpecFiles(config.dev)
    val safety = parseSpecFiles(config.safety)
    return OASISRobustifier(
      sys,
      dev,
      safety,
      progress = config.options.progress,
      preferred = config.options.preferred.map { Word.fromList(it) },
      synthesizer = SupremicaRunner()
    )
  }

  fun buildSimple(config: RobustifyConfigJSON): SimpleRobustifier<Int, String> {
    val sys = parseSpecFiles(config.sys)
    val dev = parseSpecFiles(config.dev)
    val safety = parseSpecFiles(config.safety)
    return SimpleRobustifier(
      sys,
      dev,
      safety,
      progress = config.options.progress,
      preferred = config.options.preferred.map { Word.fromList(it) },
      synthesizer = SupremicaRunner()
    )
  }
}