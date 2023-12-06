package cmu.s3d.fortis.robustness

import cmu.s3d.fortis.common.EquivClass
import cmu.s3d.fortis.common.RepTrace
import cmu.s3d.fortis.common.RobustnessOptions
import cmu.s3d.fortis.ts.DetLTS

interface RobustnessCalculator {

    val options: RobustnessOptions

    /**
     * The weakest assumption of the given system and safety property.
     */
    val weakestAssumption: DetLTS<Int, String>

    /**
     * Compute the set of traces allowed by the system but would violate the safety property.
     */
    fun computeUnsafeBeh(): Map<EquivClass, Collection<RepTrace>>

    /**
     * The entrance function to compute the robustness. It first generates the weakest assumption, and then build the
     * representation model and compute the representative traces.
     */
    fun computeRobustness(): Map<EquivClass, Collection<RepTrace>>

    /**
     * The entrance function to compare the robustness of this model to another model, i.e., X = \Delta_This - \Delta_2.
     */
    fun compare(cal: RobustnessCalculator): Map<EquivClass, Collection<RepTrace>>
}
