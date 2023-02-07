package cmu.isr.robustify

import kotlin.test.assertEquals

typealias Events = Pair<Collection<String>, Collection<String>>

abstract class RobustifierTests {

  protected fun assertSynthesisResults(expected: List<Events>, actual: List<Events>) {
    assertEquals(expected.size, actual.size)
    for (i in expected.indices) {
      assertEquals(expected[i].first.toSet(), actual[i].first.toSet())
      assertEquals(expected[i].second.toSet(), actual[i].second.toSet())
    }
  }

}

