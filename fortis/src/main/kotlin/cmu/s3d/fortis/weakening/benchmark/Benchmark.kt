package cmu.s3d.fortis.weakening.benchmark

import cmu.s3d.fortis.weakening.GR1InvariantWeakener
import cmu.s3d.fortis.weakening.SimpleGR1Invariant
import cmu.s3d.fortis.weakening.SimpleInvariantWeakener
import cmu.s3d.fortis.weakening.getGR1Invariant
import cmu.s3d.ltl.State
import cmu.s3d.ltl.learning.AlloyMaxBase
import com.github.ajalt.clikt.core.CliktCommand
import com.github.ajalt.clikt.parameters.options.default
import com.github.ajalt.clikt.parameters.options.flag
import com.github.ajalt.clikt.parameters.options.option
import com.github.ajalt.clikt.parameters.types.int
import edu.mit.csail.sdg.translator.A4Options
import org.apache.logging.log4j.Level
import org.apache.logging.log4j.core.config.Configurator
import java.io.File
import java.lang.management.ManagementFactory
import java.util.*

class ProblemGenerator(
    private val literals: List<String>,
    private val oldInvariant: SimpleGR1Invariant,
    private val expected: SimpleGR1Invariant,
    private val sizeOfTrace: Int,
    private val numOfPositives: Int,
    private val numOfNegatives: Int,
    private val maxNumOfNode: Int?,
    private val randomTraceSize: Boolean = false
) {
    private val trueStates: List<State>
    private val falseStates: List<State>

    init {
        val allStates = (0 until (1 shl literals.size)).map { idx ->
            State(literals.indices.associate { i -> literals[i] to (((idx shr i) and 1) == 1) })
        }
        trueStates = allStates.filter { satisfy(it) }
        falseStates = allStates.filter { !satisfy(it) }
    }

    private fun satisfy(state: State): Boolean {
        return !expected.antecedent.clauses.all { disjunction ->
            disjunction.props.any {
                state.values[it.first]!! == it.second
            }
        } || expected.consequent.clauses.any { conjunction ->
            conjunction.props.all {
                state.values[it.first]!! == it.second
            }
        }
    }

    fun generate(): Problem {
        return Problem(
            literals,
            oldInvariant,
            expected,
            (0 until numOfPositives).map {
                val size = if (randomTraceSize) (1..sizeOfTrace).random() else sizeOfTrace
                (0 until size).map { trueStates.random() }
            },
            (0 until numOfNegatives).map {
                val size = if (randomTraceSize) (1..sizeOfTrace).random() else sizeOfTrace
                val numOfFalse = (1 .. size).random()
                val numOfTrue = size - numOfFalse
                val t = (0 until numOfTrue).map { trueStates.random() } + (0 until numOfFalse).map { falseStates.random() }
                t.shuffled()
            },
            maxNumOfNode
        )
    }
}

class Benchmark : CliktCommand(
    name = "Weakening-Benchmark",
    help = "A tool to benchmark the performance of the weakening algorithm."
) {
    private val _run by option("--_run", help = "Run the learning process. YOU SHOULD NOT USE THIS. INTERNAL USE ONLY.").flag()
    private val solver by option("--solver", "-s", help = "The AlloyMax solver to use. Default: SAT4JMax").default("SAT4JMax")
    private val timeout by option("--timeout", "-T", help = "The timeout in seconds for solving each task. Default: 5").default("5")
    private val literals by option("--literals", "-l", help = "The atomic propositions (literals) of the invariant.")
    private val oldInvariant by option("--oldInvariant", "-o", help = "The old invariant.")
    private val expected by option("--expected", "-e", help = "The expected invariant.")
    private val sizeOfTrace by option("--sizeOfTrace", "-S", help = "The size of the traces.").int()
    private val numOfPositives by option("--numOfPositives", "-P", help = "The number of positive traces.").int()
    private val numOfNegatives by option("--numOfNegatives", "-N", help = "The number of negative traces.").int()
    private val randomTraceSize by option("--randomTraceSize", "-R", help = "Randomize the size of the traces.").flag()
    private val encoding by option("--encoding", "-E", help = "The Alloy encoding to use.")
    private val maxNumOfNode by option("--maxNumOfNode", "-M", help = "The maximum number of nodes in the learning process for GR1 weakener.").int()
    private val dryRun by option("--dryRun", "-D", help = "Dry run the benchmark.").flag()
    private val taskFile by option("--taskFile", "-f", help = "The file task to run.")
    private val taskFolder by option("--taskFolder", "-F", help = "The folder of tasks to run. This finds tasks in the folder recursively.")

    override fun run() {
        if (!dryRun && !_run)
            println("filename,oldInvariant,expected,sizeOfTrace,numOfPositives,numOfNegatives,solvingTime,formula")

        if (taskFolder != null) {
            val folder = File(taskFolder!!)
            if (folder.isDirectory) {
                folder.walk()
                    .filter { it.isFile && it.name.endsWith(".trace") }
                    .forEach { runSingleTask(it) }
            }
        } else {
            runSingleTask(taskFile?.let { File(it) })
        }
    }

    private fun runSingleTask(file: File?) {
        val problem = if (file != null) {
            if (file.isFile && file.name.endsWith(".trace")) {
                ProblemParser.parseTask(file.readText())
            } else {
                error("The file $file is not a trace file.")
            }
        } else {
            ProblemGenerator(
                literals?.split(",")?.map { it.trim() } ?: error("Literals are required."),
                SimpleGR1Invariant.oneFromString(oldInvariant ?: error("Old invariant is required.")),
                SimpleGR1Invariant.oneFromString(expected ?: error("Expected invariant is required.")),
                sizeOfTrace ?: error("Size of trace is required."),
                numOfPositives ?: error("Number of positives is required."),
                numOfNegatives ?: error("Number of negatives is required."),
                maxNumOfNode,
                randomTraceSize
            ).generate()
        }

        if (_run || dryRun) {
            actualRun(problem, file?.path)
        } else {
            rubInSubProcess(problem, file?.path)
        }
    }

    private fun actualRun(problem: Problem, taskPath: String?) {
        if (dryRun) {
            println(problem)
            return
        }

        try {
            val options = AlloyMaxBase.defaultAlloyOptions()
            options.solver = when (solver) {
                "SAT4JMax" -> A4Options.SatSolver.SAT4JMax
                "OpenWBO" -> A4Options.SatSolver.OpenWBO
                "OpenWBOWeighted" -> A4Options.SatSolver.OpenWBOWeighted
                "POpenWBO" -> A4Options.SatSolver.POpenWBO
                "POpenWBOAuto" -> A4Options.SatSolver.POpenWBOAuto
                else -> error("Unknown solver: $solver")
            }
            val (formula, solvingTime) = when (encoding) {
                "simple" -> solveBySimpleWeakener(problem, options)
                "gr1" -> solveByGR1Weakener(problem, options)
                null -> error("Encoding is required.")
                else -> error("Unknown encoding: $encoding")
            }
            println("$taskPath,${problem.toCSVString()},$solvingTime,\"$formula\"")
        } catch (e: Exception) {
            val message = e.message?.replace("\\v".toRegex(), " ") ?: "Unknown error"
            println("$taskPath,${problem.toCSVString()},\"ERR:$message\",-")
        }
    }

    private fun solveBySimpleWeakener(problem: Problem, options: A4Options): Pair<String, Double> {
        val weakener = SimpleInvariantWeakener(
            listOf(problem.oldInvariant.toSimpleInvariant()),
            problem.literals,
            problem.positiveTraces,
            problem.negativeTraces,
            options
        )
        val startTime = System.currentTimeMillis()
        val solution = weakener.learn()
        val solvingTime = (System.currentTimeMillis() - startTime).toDouble() / 1000
        return Pair(solution?.getInvariant()?.joinToString(" && ") ?: "UNSAT", solvingTime)
    }

    private fun solveByGR1Weakener(problem: Problem, options: A4Options): Pair<String, Double> {
        val weakener = GR1InvariantWeakener(
            listOf(problem.oldInvariant),
            problem.literals,
            problem.positiveTraces,
            problem.negativeTraces,
            problem.maxNumOfNode?.plus(problem.literals.size) ?: error("Max number of nodes is required for GR(1) mode."),
            options
        )
        val startTime = System.currentTimeMillis()
        val solution = weakener.learn()
        val solvingTime = (System.currentTimeMillis() - startTime).toDouble() / 1000
        return Pair(solution?.getGR1Invariant() ?: "UNSAT", solvingTime)
    }

    private fun rubInSubProcess(problem: Problem, taskPath: String?) {
        val jvmArgs = ManagementFactory.getRuntimeMXBean().inputArguments
        val jvmXms = jvmArgs.find { it.startsWith("-Xms") }
        val jvmXmx = jvmArgs.find { it.startsWith("-Xmx") }

        val cmd = mutableListOf(
            "java",
            jvmXms ?: "-Xms512m",
            jvmXmx ?: "-Xmx4g",
            "-Djava.library.path=${System.getProperty("java.library.path")}",
            "-cp",
            System.getProperty("java.class.path"),
            "cmu.s3d.fortis.weakening.benchmark.BenchmarkKt",
            "--_run",
            "-s", solver
        )
        if (literals != null) cmd.addAll(listOf("-l", literals))
        if (oldInvariant != null) cmd.addAll(listOf("-o", oldInvariant))
        if (expected != null) cmd.addAll(listOf("-e", expected))
        if (sizeOfTrace != null) cmd.addAll(listOf("-S", sizeOfTrace.toString()))
        if (numOfPositives != null) cmd.addAll(listOf("-P", numOfPositives.toString()))
        if (numOfNegatives != null) cmd.addAll(listOf("-N", numOfNegatives.toString()))
        if (randomTraceSize) cmd.add("-R")
        if (encoding != null) cmd.addAll(listOf("-E", encoding))
        if (maxNumOfNode != null) cmd.addAll(listOf("-M", maxNumOfNode.toString()))
        if (taskPath != null) cmd.addAll(listOf("-f", taskPath))

        val processBuilder = ProcessBuilder(cmd)
        processBuilder.redirectErrorStream(true)
        val process = processBuilder.start()

        try {
            val timer = Timer(true)
            timer.schedule(object : TimerTask() {
                override fun run() {
                    if (process.isAlive) {
                        process.destroy()
                        println("$taskPath,${problem.toCSVString()},TO,-")
                    }
                }
            }, timeout.toLong() * 1000)
            val output = process.inputStream.bufferedReader().readText()
            print(output)

            process.waitFor()
            timer.cancel()
        } catch (e: Exception) {
            process.destroyForcibly()
        }
    }
}

fun main(args: Array<String>) {
    Configurator.setRootLevel(Level.OFF)
    Benchmark().main(args)
}