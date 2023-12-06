package cmu.s3d.fortis.assumption

import cmu.s3d.fortis.robustness.RobustnessOptions
import cmu.s3d.fortis.ts.DetLTS

interface WeakestAssumptionGenerator<I> {
    /**
     * Generate the weakest assumption of a machine and a safety property
     * (given an environment). Note that the environment is only needed for
     * deciding the interface alphabets.
     */
    fun generate(options: RobustnessOptions): DetLTS<Int, I>

    fun generateUnsafe(): DetLTS<Int, I>
}