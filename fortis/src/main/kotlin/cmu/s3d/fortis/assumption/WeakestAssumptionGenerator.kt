package cmu.s3d.fortis.assumption

import cmu.s3d.fortis.common.RobustnessOptions
import cmu.s3d.fortis.ts.DetLTS

interface WeakestAssumptionGenerator {
    /**
     * Generate the weakest assumption of a machine and a safety property
     * (given an environment). Note that the environment is only needed for
     * deciding the interface alphabets.
     */
    fun generate(options: RobustnessOptions): DetLTS<Int, String>

    fun generateUnsafe(): DetLTS<Int, String>
}