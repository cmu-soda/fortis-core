package cmu.isr.weakening

import cmu.isr.ts.lts.Fluent
import cmu.isr.ts.lts.evaluateFluent
import net.automatalib.words.Word

data class Invariant(
    val antecedent: List<Pair<String, Boolean>>,
    val consequent: List<Pair<String, Boolean>>,
)

class InvariantWeakener(
    private val invariant: Invariant,
    private val fluents: List<Fluent>,
    private val positiveExamples: List<Word<String>>,
    private val negativeExamples: List<Word<String>>,
) {
    fun generateAlloyModel(): String {
        val literals = fluents.map { it.name }
        val antecedent = invariant.antecedent.map { it.first + "->" + if (it.second) "True" else "False" }
        val consequent = invariant.consequent.map { it.first }
        val statesMap = mutableMapOf<String, String>()
        val statesAlloyScript = mutableMapOf<String, String>()
        val positiveTraces = positiveExamples.map { generateTrace(it, statesMap, statesAlloyScript) }
        val negativeTraces = negativeExamples.map { generateTrace(it, statesMap, statesAlloyScript) }

        val alloyScript = """
            abstract sig Bool {}
            one sig True, False extends Bool {}
            abstract sig Literal {}
            one sig ${literals.joinToString(", ")} extends Literal {}
            one sig Invariant {
                antecedent: Literal -> lone Bool,
                consequent: Literal -> lone Bool
            } {
                consequent not in antecedent
                ${antecedent.joinToString(" + ")} in antecedent
                consequent.Bool in (${consequent.joinToString(" + ")})
            }

            abstract sig State {
            	trueValues: set Literal
            }
            abstract sig Trace {
            	states: set State
            }
            abstract sig PositiveTrace, NegativeTrace extends Trace {}
            
            ${statesMap.entries.joinToString("\n            ") {
                "one sig ${it.value} extends State {} { trueValues = ${statesAlloyScript[it.key]} }"
            }}
            
            ${positiveTraces.indices.joinToString("\n            ") { i ->
                "one sig PT$i extends PositiveTrace {} { states = ${positiveTraces[i]} }"
            }}
            
            ${negativeTraces.indices.joinToString("\n            ") { i ->
                "one sig NT$i extends NegativeTrace {} { states = ${negativeTraces[i]} }"
            }}
            
            pred satisfy[s: State, inv: Invariant] {
            	let trues = s.trueValues, falses = Literal - s.trueValues |
            		inv.antecedent.True in trues and inv.antecedent.False in falses implies
            			inv.consequent.True in trues and inv.consequent.False in falses
            }

            pred G[t: Trace, inv: Invariant] {
            	all s: t.states | satisfy[s, inv]
            }

            run {
            	all inv: Invariant, t: PositiveTrace | G[t, inv]
            	all inv: Invariant, t: NegativeTrace | not G[t, inv]
            	minsome[2] Invariant.antecedent
            	maxsome[1] Invariant.consequent
            }
        """.trimIndent()

        return alloyScript
    }

    private fun generateTrace(example: Word<String>, statesMap: MutableMap<String, String>,
                              statesAlloyScript: MutableMap<String, String>): String {
        return evaluateFluent(example, fluents).map { state ->
            val stateValues = fluents.joinToString { if (state[it] == true) "1" else "0" }
            statesMap.getOrPut(stateValues) {
                statesAlloyScript[stateValues] = state.entries.filter { it.value }.let {
                    if (it.isEmpty()) "none" else it.joinToString(" + ") { entry -> entry.key.name }
                }
                "S${statesMap.size}"
            }
        }.toSet().joinToString(" + ")
    }
}