package cmu.isr.ts.lts.ltsa

import cmu.isr.ts.DetLTS
import cmu.isr.ts.LTS
import cmu.isr.ts.lts.asLTS
import lts.*
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
  private fun CompositeState.alphabetNoTau(escape: Boolean = false): List<String> {
    val alphabet = this.composition.alphabet.toMutableList()
    alphabet.remove("tau")
    return if (escape) alphabet.map(LTSACall::escapeEvent) else alphabet
  }

  private fun CompositeState.alphabet(escape: Boolean = false): List<String> {
    val alphabet = this.composition.alphabet.toList()
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

  fun CompositeState.asDetLTS(escape: Boolean = false): DetLTS<Int, String> {
    // check there's no tau transition
    if (this.composition.hasTau() || this.composition.isNonDeterministic)
      error("The given LTS is non-deterministic")

    val alphabet = alphabet(escape)
    val builder = AutomatonBuilders.newDFA(Alphabets.fromCollection(alphabet - "tau")).withInitial(0)
    for (s in this.composition.states.indices) {
      val state = this.composition.states[s]
      for (a in this.composition.alphabet.indices) {
        val input = alphabet[a]
        val succ = EventState.nextState(state, a)
        if (succ != null) {
          builder.from(s).on(input).to(succ[0])
        }
      }
      builder.withAccepting(s)
    }
    return builder.create().asLTS()
  }

  fun CompositeState.asLTS(escape: Boolean = false): LTS<Int, String> {
    val alphabet = alphabet(escape)
    val builder = if (this.composition.hasTau())
      AutomatonBuilders.newNFA(Alphabets.fromCollection(alphabet)).withInitial(0)
    else
      AutomatonBuilders.newNFA(Alphabets.fromCollection(alphabet - "tau")).withInitial(0)
    for (s in this.composition.states.indices) {
      val state = this.composition.states[s]
      for (a in this.composition.alphabet.indices) {
        val input = alphabet[a]
        val succs = EventState.nextState(state, a)
        if (succs != null) {
          for (succ in succs)
            builder.from(s).on(input).to(succ)
        }
      }
      builder.withAccepting(s)
    }
    return builder.create().asLTS()
  }
}
