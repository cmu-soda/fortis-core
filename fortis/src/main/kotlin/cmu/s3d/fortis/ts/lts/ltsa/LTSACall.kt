package cmu.s3d.fortis.ts.lts.ltsa

import cmu.s3d.fortis.ts.DetLTS
import cmu.s3d.fortis.ts.LTS
import cmu.s3d.fortis.ts.lts.asLTS
import lts.*
import lts.ltl.AssertDefinition
import net.automatalib.util.automata.builders.AutomatonBuilders
import net.automatalib.words.impl.Alphabets
import org.slf4j.LoggerFactory
import java.util.*


object LTSACall {

    private val logger = LoggerFactory.getLogger(javaClass)

    init {
        SymbolTable.init()
    }

    /**
     * Compile a given FSP spec. This should behave the same as the compile button in the LTSA tool.
     * @param compositeName The name of the targeting composition process. This will affect which process
     * would be composed when calling the doCompose() function. By default, the name is "DEFAULT" which will
     * create an implicit process named DEFAULT which is the composition of all the processes in the spec.
     */
    fun compile(fsp: String, compositeName: String = "DEFAULT"): CompositeState {
        val ltsInput = StringLTSInput(fsp)
        val ltsOutput = StringLTSOutput()
        val compiler = LTSCompiler(ltsInput, ltsOutput, System.getProperty("user.dir"))
        try {
            return compiler.compile(compositeName)
        } catch (e: LTSException) {
            logger.debug(e.stackTraceToString())
            throw Exception("Failed to compile the fsp source string of machine '${compositeName}'.")
        }
    }

    /**
     * Compile a given FSP spec with fluents and LTL assertions. Return the safety LTS of the LTL with the given name.
     * The LTL should be a safety property, i.e., the compiled LTS should have an error state.
     */
    fun compileSafetyLTL(fsp: String, name: String): CompactState {
        val out = StringLTSOutput()
        compile(fsp)
        val ltl = AssertDefinition.compile(out, name)
        return if (ltl.composition.hasERROR()) ltl.composition else error("The given LTL is not a safety property")
    }

    /**
     * Get the actions in a given menu from the last compilation
     */
    fun menuActions(name: String): Collection<String> {
        val def = (MenuDefinition.definitions[name] ?: error("No such menu named '$name'")) as MenuDefinition
        val actionField = MenuDefinition::class.java.getDeclaredField("actions")
        actionField.isAccessible = true
        val actions = actionField.get(def)
        val actionVectorField = actions.javaClass.getDeclaredField("actions")
        actionVectorField.isAccessible = true
        return actionVectorField.get(actions) as Vector<String>
    }

    /**
     * This behaves the same as the Compose option in the LTSA tool.
     */
    fun CompositeState.compose(): CompositeState {
        val ltsOutput = StringLTSOutput()
        try {
            this.compose(ltsOutput)
            return this
        } catch (e: LTSException) {
            logger.debug(e.stackTraceToString())
            throw Exception("Failed to compose machine '${this.name}'.")
        }
    }

    fun CompositeState.minimize(): CompositeState {
        val ltsOutput = StringLTSOutput()
        try {
            this.minimise(ltsOutput)
            return this
        } catch (e: LTSException) {
            logger.debug(e.stackTraceToString())
            throw Exception("Failed to minimize machine '${this.name}'")
        }
    }

    /**
     * @return The alphabet list of the composed state machine excluding tau.
     */
    private fun CompactState.alphabetNoTau(escape: Boolean = false): List<String> {
        val alphabet = this.alphabet.toMutableList()
        alphabet.remove("tau")
        return if (escape) alphabet.map(LTSACall::escapeEvent) else alphabet
    }

    private fun CompactState.alphabet(escape: Boolean = false): List<String> {
        val alphabet = this.alphabet.toList()
        return if (escape) alphabet.map(LTSACall::escapeEvent) else alphabet
    }

    /**
     * Rename the events:
     * if e == "tau" then e' = "_tau_"
     * if e match abc.123 then e' = abc[123]
     */
    fun escapeEvent(e: String): String {
//    if (e == "tau")
//      return "_tau_"
        var escaped = e
        var lastIdx = e.length
        while (true) {
            val idx = escaped.substring(0, lastIdx).lastIndexOf('.')
            if (idx == -1)
                return escaped
            val suffix = escaped.substring(idx + 1, lastIdx)
            if (suffix.toIntOrNull() != null)
                escaped = "${escaped.substring(0, idx)}[$suffix]${escaped.substring(lastIdx)}"
            lastIdx = idx
        }
    }

    fun CompositeState.asDetLTS(escape: Boolean = false, removeError: Boolean = false): DetLTS<Int, String> {
        return this.composition.asDetLTS(escape, removeError)
    }

    fun CompactState.asDetLTS(escape: Boolean = false, removeError: Boolean = false): DetLTS<Int, String> {
        // check there's no tau transition
        if (this.hasTau() || this.isNonDeterministic)
            error("The given LTS is non-deterministic")

        val inputs = alphabet(escape)
        val builder = AutomatonBuilders.newDFA(Alphabets.fromCollection(inputs - "tau")).withInitial(0)
        for (s in this.states.indices) { // if s is an error state, then it will not be in this iteration
            val state = this.states[s]
            for (a in this.alphabet.indices) {
                val input = inputs[a]
                val succ = EventState.nextState(state, a)
                if (succ != null && (!removeError || succ[0] != -1)) {
                    builder.from(s).on(input).to(succ[0])
                }
            }
            builder.withAccepting(s)
        }
        return builder.create().asLTS()
    }

    fun CompositeState.asLTS(escape: Boolean = false, removeError: Boolean = false): LTS<Int, String> {
        return this.composition.asLTS(escape, removeError)
    }

    fun CompactState.asLTS(escape: Boolean = false, removeError: Boolean = false): LTS<Int, String> {
        val inputs = alphabet(escape)
        val builder = if (this.hasTau())
            AutomatonBuilders.newNFA(Alphabets.fromCollection(inputs)).withInitial(0)
        else
            AutomatonBuilders.newNFA(Alphabets.fromCollection(inputs - "tau")).withInitial(0)
        for (s in this.states.indices) { // if s is an error state, then it will not be in this iteration
            val state = this.states[s]
            for (a in this.alphabet.indices) {
                val input = inputs[a]
                val succs = EventState.nextState(state, a)
                if (succs != null) {
                    for (succ in succs) {
                        if (!removeError || succ != -1)
                            builder.from(s).on(input).to(succ)
                    }
                }
            }
            builder.withAccepting(s)
        }
        return builder.create().asLTS()
    }
}
