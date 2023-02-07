package cmu.isr.supervisory

import cmu.isr.ts.alphabet
import java.io.Closeable

interface SupervisorySynthesizer<S, I> : Closeable {
  fun synthesize(plant: SupervisoryDFA<*, I>, prop: SupervisoryDFA<*, I>): SupervisoryDFA<S, I>?

  fun checkAlphabets(plant: SupervisoryDFA<*, I>, prop: SupervisoryDFA<*, I>) {
    val common = plant.alphabet() intersect prop.alphabet()
    for (input in common) {
      if (!((input !in plant.controllable || input in prop.controllable) && // plant => prop
          (input !in prop.controllable || input in plant.controllable))) // prop => plant
        error("The plant and the property should have the same controllable")
      if (!((input !in plant.observable || input in prop.observable) && // plant => prop
        (input !in prop.observable || input in plant.observable))) // prop => plant
        error("The plant and the property should have the same observable")
    }
  }
}