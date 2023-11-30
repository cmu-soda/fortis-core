package cmu.isr.weakening

import cmu.isr.ts.alphabet
import net.automatalib.automata.fsa.NFA
import net.automatalib.words.Word

class LearningExampleGenerator<S, I>(private val model: NFA<S, I>) : Iterator<Word<I>>, Iterable<Word<I>> {
    private var dfsStack: ArrayDeque<DFSRecord<S, I>>? = null
    private var visited: MutableSet<S>? = null
    private var currentExample: Word<I>? = null

    private class DFSRecord<S, I>(val trace: Word<I>, val state: S)

    override fun iterator(): Iterator<Word<I>> {
        dfsStack = ArrayDeque()
        model.initialStates.forEach { dfsStack!!.add(DFSRecord(Word.epsilon(), it)) }
        visited = mutableSetOf()
        currentExample = null
        return this
    }

    override fun hasNext(): Boolean {
        if (dfsStack == null || visited == null)
            return false
        if (visited!!.size == model.size())
            return false

        // a DFS search
        while (dfsStack!!.isNotEmpty()) {
            val record = dfsStack!!.removeLast()
            var isDeadlock = true
            visited!!.add(record.state)
            for (a in model.alphabet()) {
                val successors = model.getSuccessors(record.state, a)
                if (successors.isNotEmpty()) {
                    isDeadlock = false
                    for (s in successors) {
                        val trace = Word.fromList(record.trace + a)
                        dfsStack!!.add(DFSRecord(trace, s))
                    }
                }
            }
            if (isDeadlock) {
                currentExample = record.trace
                return true
            }
        }

        return false
    }

    override fun next(): Word<I> {
        return currentExample?: error("No more examples")
    }
}