package cmu.s3d.fortis.weakening

import cmu.s3d.fortis.ts.lts.Fluent
import cmu.s3d.fortis.ts.lts.evaluateFluent
import cmu.s3d.fortis.ts.lts.getFluentValuationString
import net.automatalib.word.Word

/**
 * This filter would only return example traces that would visit a new state that has not been visited by other traces.
 * This is proper for safety invariant with only the Globally operator because the evaluation of the invariant only
 * depends on the valuation of the fluents at the current state.
 */
class ExampleFilterForInvariant(
    private val examples: Iterable<Word<String>>,
    private val fluents: List<Fluent>
) : Iterator<Word<String>>, Iterable<Word<String>> {
    private val visited = mutableSetOf<String>()
    private var exampleIterator = examples.iterator()
    private var current: Word<String>? = null

    override fun iterator(): Iterator<Word<String>> {
        visited.clear()
        exampleIterator = examples.iterator()
        current = null
        return this
    }

    override fun hasNext(): Boolean {
        while (exampleIterator.hasNext()) {
            val trace = exampleIterator.next()
            val evaluation = evaluateFluent(trace, fluents)
            val currentSize = visited.size
            for (state in evaluation) {
                visited.add(getFluentValuationString(fluents, state))
            }
            if (visited.size > currentSize) {
                current = trace
                return true
            }
        }
        return false
    }

    override fun next(): Word<String> {
        return current ?: error("No more examples")
    }
}