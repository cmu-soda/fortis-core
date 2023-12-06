package cmu.s3d.fortis.robustness

import cmu.s3d.fortis.ts.DetLTS
import net.automatalib.words.Word

interface RobustnessCalculator<S, I> {

    val options: RobustnessOptions

    /**
     * The weakest assumption of the given system and safety property.
     */
    val weakestAssumption: DetLTS<S, I>

    /**
     * Compute the set of traces allowed by the system but would violate the safety property.
     */
    fun computeUnsafeBeh(): Map<EquivClass<I>, Collection<RepTrace<I>>>

    /**
     * The entrance function to compute the robustness. It first generates the weakest assumption, and then build the
     * representation model and compute the representative traces.
     */
    fun computeRobustness(): Map<EquivClass<I>, Collection<RepTrace<I>>>

    /**
     * The entrance function to compare the robustness of this model to another model, i.e., X = \Delta_This - \Delta_2.
     */
    fun compare(cal: RobustnessCalculator<*, I>): Map<EquivClass<I>, Collection<RepTrace<I>>>
}

data class RobustnessOptions(
    val expand: Boolean = false,
    val minimized: Boolean = false,
    val disables: Boolean = false
)

data class EquivClass<I>(val s: Int, val a: I)

data class RepTrace<I>(val word: Word<I>, val deadlock: Boolean)
