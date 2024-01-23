package cmu.s3d.fortis.weakening

import cmu.s3d.fortis.ts.lts.Fluent
import cmu.s3d.fortis.ts.lts.evaluateFluent
import cmu.s3d.fortis.ts.lts.getFluentValuationString
import edu.mit.csail.sdg.alloy4.A4Reporter
import edu.mit.csail.sdg.parser.CompUtil
import edu.mit.csail.sdg.translator.A4Options
import edu.mit.csail.sdg.translator.TranslateAlloyToKodkod
import net.automatalib.word.Word
import org.slf4j.LoggerFactory

class SimpleInvariantWeakener(
    private val invariant: List<SimpleInvariant>,
    private val fluents: List<Fluent>,
    private val positiveExamples: List<Word<String>>,
    private val negativeExamples: List<Word<String>>,
) {
    private val logger = LoggerFactory.getLogger(javaClass)

    fun generateAlloyModel(): String {
        val literals = fluents.map { it.name }
        val invariantPairs = invariant.map { i -> Pair(
            i.antecedent.map { it.first + "->" + if (it.second) "True" else "False" },
            i.consequent.map { it.first + "->" + if (it.second) "True" else "False" }
        ) }
        val statesMap = mutableMapOf<String, String>()
        val statesAlloyScript = mutableMapOf<String, String>()
        val positiveTraces = positiveExamples.map { generateTrace(it, statesMap, statesAlloyScript) }
        val negativeTraces = negativeExamples.map { generateTrace(it, statesMap, statesAlloyScript) }

        val invariantScript = invariantPairs.indices.map { i -> """
            one sig Invariant$i extends Invariant {} {
                ${invariantPairs[i].first.joinToString(" + ")} in antecedent
                false->True in antecedent implies antecedent in (false->True + ${invariantPairs[i].first.joinToString(" + ")})
                consequent in (${invariantPairs[i].second.joinToString(" + ")})
            }
            """
        }

        val alloyScript = """
            abstract sig Bool {}
            one sig True, False extends Bool {}
            abstract sig Literal {}
            one sig false, ${literals.joinToString(", ")} extends Literal {}
            abstract sig Invariant {
                antecedent: Literal -> lone Bool,
                consequent: Literal -> lone Bool
            } {
                consequent not in antecedent
                all l: Literal | (l -> True in antecedent implies l -> False not in consequent) and
		            (l -> False in antecedent implies l -> True not in consequent)
                false->False not in antecedent
            }
            ${invariantScript.joinToString("")}
            abstract sig State {
            	trueValues: set Literal
            }
            abstract sig Trace {
            	states: set State
            }
            abstract sig PositiveTrace, NegativeTrace extends Trace {}
            
            ${
                statesMap.entries.joinToString("\n            ") {
                    "one sig ${it.value} extends State {} { trueValues = ${statesAlloyScript[it.key]} }"
                }
            }
            
            ${
                positiveTraces.indices.joinToString("\n            ") { i ->
                    "one sig PT$i extends PositiveTrace {} { states = ${positiveTraces[i]} }"
                }
            }
            
            ${
                negativeTraces.indices.joinToString("\n            ") { i ->
                    "one sig NT$i extends NegativeTrace {} { states = ${negativeTraces[i]} }"
                }
            }
            
            pred satisfy[s: State, inv: Invariant] {
            	let trues = s.trueValues, falses = Literal - s.trueValues |
            		inv.antecedent.True in trues and inv.antecedent.False in falses implies
            			inv.consequent.True in trues and inv.consequent.False in falses
            }

            pred G[t: Trace, inv: Invariant] {
            	all s: t.states | satisfy[s, inv]
            }

            run {
                all t: PositiveTrace | all inv: Invariant | G[t, inv]
                all t: NegativeTrace | some inv: Invariant | not G[t, inv]
                all inv: Invariant | minsome[2] inv.antecedent and maxsome[1] inv.consequent
            }
        """.trimIndent()

        return alloyScript
    }

    private fun generateTrace(
        example: Word<String>, statesMap: MutableMap<String, String>,
        statesAlloyScript: MutableMap<String, String>
    ): String {
        return evaluateFluent(example, fluents).map { state ->
            val stateValues = getFluentValuationString(fluents, state)
            statesMap.getOrPut(stateValues) {
                statesAlloyScript[stateValues] = state.entries.filter { it.value }.let {
                    if (it.isEmpty()) "none" else it.joinToString(" + ") { entry -> entry.key.name }
                }
                "S${statesMap.size}"
            }
        }.toSet().joinToString(" + ")
    }

    fun learn(): WeakeningSolution? {
        logger.info("Generating Alloy model for weakening...")
        val alloyScript = generateAlloyModel()
        logger.debug("Generated Alloy model:\n{}", alloyScript)

        val reporter = A4Reporter.NOP
        val world = CompUtil.parseEverything_fromString(reporter, alloyScript)
        val options = alloyOptions()
        val command = world.allCommands.first()
        val solution = TranslateAlloyToKodkod.execute_command(reporter, world.allReachableSigs, command, options)

        return if (solution.satisfiable()) WeakeningSolution(world, solution) else null
    }

    private fun alloyOptions(): A4Options {
        val options = A4Options()
        options.solver = A4Options.SatSolver.SAT4JMax
        options.skolemDepth = 1
        options.noOverflow = false
        options.inferPartialInstance = false
        return options
    }
}