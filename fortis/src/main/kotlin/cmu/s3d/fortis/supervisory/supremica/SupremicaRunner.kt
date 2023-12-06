package cmu.s3d.fortis.supervisory.supremica

import cmu.s3d.fortis.supervisory.SupervisoryDFA
import cmu.s3d.fortis.supervisory.SupervisorySynthesizer
import org.supremica.automata.Automata
import org.supremica.automata.AutomatonType
import org.supremica.automata.algorithms.*

class SupremicaRunner : SupervisorySynthesizer<Int, String> {

    private val synthOptions = SynthesizerOptions.getDefaultSynthesizerOptions()
    private val syncOptions = SynchronizationOptions.getDefaultSynthesisOptions()

    init {
        synthOptions.dialogOK = false
        synthOptions.oneEventAtATime = false
        synthOptions.addOnePlantAtATime = false
        synthOptions.synthesisType = SynthesisType.NONBLOCKING_CONTROLLABLE_NORMAL
        synthOptions.synthesisAlgorithm = SynthesisAlgorithm.MONOLITHIC_WATERS
        synthOptions.setPurge(true)
        synthOptions.setRename(true)
        synthOptions.removeUnecessarySupervisors = false
        synthOptions.maximallyPermissive = true
        synthOptions.maximallyPermissiveIncremental = true
        synthOptions.reduceSupervisors = false
        synthOptions.localizeSupervisors = false
    }

    override fun synthesize(
        plant: SupervisoryDFA<*, String>,
        prop: SupervisoryDFA<*, String>
    ): SupervisoryDFA<Int, String>? {
        assert(synthOptions.isValid)
        checkAlphabets(plant, prop)

        val theAutomata = Automata()
        val plantAutomaton = writeSupremica(plant, "plant")
        val specAutomaton = writeSupremica(prop, "spec")
        plantAutomaton.type = AutomatonType.PLANT
        specAutomaton.type = AutomatonType.SPECIFICATION
        theAutomata.addAutomaton(plantAutomaton)
        theAutomata.addAutomaton(specAutomaton)

        val synthesizer = AutomataSynthesizer(theAutomata, syncOptions, synthOptions)
        val sup = synthesizer.execute().firstAutomaton

        return if (sup.nbrOfStates() == 0) null else parseSupremica(sup)
    }

    override fun close() {}

}