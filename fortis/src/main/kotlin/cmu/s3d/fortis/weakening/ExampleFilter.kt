package cmu.s3d.fortis.weakening

import cmu.s3d.fortis.ts.lts.Fluent
import cmu.s3d.fortis.ts.lts.evaluateFluent
import cmu.s3d.fortis.ts.lts.getFluentValuationString
import net.automatalib.word.Word

interface ExampleFilter<I> {
    fun filter(trace: Word<I>): Boolean

    fun reset()
}

/**
 * This filter would only return example traces that would visit a new state that has not been visited by other traces.
 * This is proper for safety invariant with only the Globally operator because the evaluation of the invariant only
 * depends on the valuation of the fluents at the current state.
 */
class InvariantExampleFilter(private val fluents: List<Fluent>) : ExampleFilter<String> {
    private val literals = fluents.map { it.name }
    private val existingExamples = mutableSetOf<String>()

    override fun filter(trace: Word<String>): Boolean {
        val evaluation = evaluateFluent(trace, fluents)
        val sortedStatesString = evaluation
            .map { getFluentValuationString(literals, it) }
            .toSortedSet()
            .joinToString("")
        return existingExamples.add(sortedStatesString)
    }

    override fun reset() {
        existingExamples.clear()
    }

}