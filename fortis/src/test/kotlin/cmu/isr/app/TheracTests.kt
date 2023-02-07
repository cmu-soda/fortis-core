package cmu.isr.app

import cmu.isr.robustify.RobustifierTests
import cmu.isr.robustify.supervisory.Algorithms
import cmu.isr.supervisory.SupervisoryDFA
import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import org.junit.jupiter.api.Disabled
import org.junit.jupiter.api.Test
import java.io.File

@Disabled
class TheracTests : RobustifierTests() {

  @Test
  fun testSmartPareto() {
    val robustify = Robustify()
    val config = jacksonObjectMapper().readValue(File("config-pareto.json"), RobustifyConfigJSON::class.java)
    val robustifier = robustify.buildSupervisory(config)

    robustifier.use {
      val expected = listOf(
        Pair(
          listOf("b", "fire_ebeam", "fire_xray", "setMode"),
          listOf("b", "e", "enter", "fire_ebeam", "fire_xray", "setMode", "up", "x")
        ),
        Pair(
          listOf("enter", "fire_ebeam", "fire_xray", "setMode"),
          listOf("b", "e", "enter", "fire_ebeam", "fire_xray", "setMode", "up", "x")
        )
      )
      assertSynthesisResults(
        expected,
        it.synthesize(Algorithms.Pareto).map { r -> Pair((r as SupervisoryDFA).controllable, r.observable) }
      )
    }
  }

}