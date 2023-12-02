package cmu.isr.weakening

import cmu.isr.ts.lts.Fluent
import cmu.isr.ts.lts.evaluateFluent
import cmu.isr.ts.lts.getFluentValuationString
import net.automatalib.words.Word

class ExampleFilterByFluents(
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
        return current?: error("No more examples")
    }
}