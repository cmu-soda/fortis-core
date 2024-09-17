package cmu.s3d.fortis.weakening

import cmu.s3d.fortis.ts.alphabet
import cmu.s3d.fortis.ts.lts.traceToLTS
import cmu.s3d.fortis.ts.parallel
import net.automatalib.alphabet.Alphabet
import net.automatalib.automaton.fsa.NFA
import net.automatalib.util.automaton.builder.AutomatonBuilders
import net.automatalib.word.Word
import kotlin.math.min

open class ExampleGenerator<S, I>(
    private val model: NFA<S, I>,
    private val numOfAdditionalExamples: Int
) : Iterator<Word<I>>, Iterable<Word<I>> {
    private val dfsStack: ArrayDeque<DFSRecord<S, I>> = ArrayDeque()
    // visited states s.t. visited[s] is the set of traces from this state s leading to an accepting example
    private val visited: MutableMap<S, MutableList<Word<I>>> = mutableMapOf()
    private val examples1: MutableList<Word<I>> = mutableListOf()
    private val examples2: MutableList<Word<I>> = mutableListOf()
    private var examplesCount = 0
    private var exampleFilter: ExampleFilter<I>? = null

    protected class DFSRecord<S, I>(val state: S, val trace: Word<I>, val visited: List<S>)

    override fun iterator(): Iterator<Word<I>> {
        dfsStack.clear()
        model.initialStates.forEach { dfsStack.add(DFSRecord(it, Word.epsilon(), emptyList())) }
        visited.clear()
        examples1.clear()
        examples2.clear()
        examplesCount = 0
        exampleFilter?.reset()

        searchNext()
        return this
    }

    private fun searchNext() {
        // a DFS search
        while (dfsStack.isNotEmpty()) {
            val record = dfsStack.removeLast()
            var isDeadlock = true

            visited[record.state] = mutableListOf()
            for (a in model.alphabet()) {
                val successors = model.getSuccessors(record.state, a)
                isDeadlock = isDeadlock && successors.isEmpty()
                for (s in successors) {
                    when (s) {
                        // lasso
                        in record.visited + record.state -> {
                            offerExample(record.trace, examples1, record)
                        }
                        // exist a trace from s to an accepting example
                        in visited -> {
                            for ((i, t) in visited[s]!!.withIndex()) {
                                if (i == 0) {
                                    offerExample(Word.fromList(record.trace + a + t), examples1, record)
                                } else {
                                    offerExample(Word.fromList(record.trace + a + t), examples2, record)
                                }
                            }
                        }
                        else -> {
                            dfsStack.add(
                                DFSRecord(
                                    state = s,
                                    trace = Word.fromList(record.trace + a),
                                    visited = record.visited + record.state
                                )
                            )
                        }
                    }
                }
            }
            if (isDeadlock) {
                offerExample(record.trace, examples1, record)
            }
        }
    }

    private fun updateVisited(record: DFSRecord<S, I>, trace: Word<I>) {
        for ((i, s) in (record.visited + record.state).withIndex()) {
            visited[s]!!.add(trace.subWord(i))
        }
    }

    protected open fun offerExample(trace: Word<I>, examples: MutableList<Word<I>>, record: DFSRecord<S, I>): Boolean {
        if (exampleFilter == null || exampleFilter!!.filter(trace)) {
            examples.add(trace)
            updateVisited(record, trace)
            return true
        }
        return false
    }

    override fun hasNext(): Boolean {
        return if (numOfAdditionalExamples >= 0)
            examplesCount < min(examples1.size + examples2.size, examples1.size + numOfAdditionalExamples)
        else
            examplesCount < examples1.size + examples2.size
    }

    override fun next(): Word<I> {
        return if (examplesCount < examples1.size) {
            examples1[examplesCount++]
        } else {
            examples2[examplesCount++ - examples1.size]
        }
    }

    fun withFilter(filter: ExampleFilter<I>): ExampleGenerator<S, I> {
        exampleFilter = filter
        return this
    }
}

@Deprecated("Use TraceExampleGenerator instead")
class ProgressExampleGenerator<S, I>(model: NFA<S, I>, progress: I) : ExampleGenerator<Int, I>(
    parallel(model, progress.let {
        val inputs = model.alphabet()
        val builder = AutomatonBuilders.newDFA(inputs)
            .withInitial(0)
            .from(0).on(it).to(1)
        for (a in inputs) {
            if (a != it) {
                builder.from(0).on(a).loop()
            }
        }
        builder.create()
    }),
    0
)

class TraceExampleGenerator<S, I>(
    model: NFA<S, I>,
    private val observedTrace: Word<I>,
    private val observedInputs: Alphabet<I>,
    numOfAdditionalExamples: Int = 0
) : ExampleGenerator<Int, I>(
    parallel(model, traceToLTS(observedTrace, observedInputs, makeError = false)),
    numOfAdditionalExamples
) {
    override fun offerExample(trace: Word<I>, examples: MutableList<Word<I>>, record: DFSRecord<Int, I>): Boolean {
        val projected = trace.filter { it in observedInputs }
        if (observedTrace.asList() == projected) {
            return super.offerExample(trace, examples, record)
        }
        return false
    }
}