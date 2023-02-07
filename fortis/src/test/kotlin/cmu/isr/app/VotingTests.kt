package cmu.isr.app

import cmu.isr.robustify.RobustifierTests
import cmu.isr.robustify.supervisory.Algorithms
import cmu.isr.supervisory.SupervisoryDFA
import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import org.junit.jupiter.api.Disabled
import org.junit.jupiter.api.Test
import java.io.File

@Disabled
class VotingTests : RobustifierTests() {

  @Test
  fun testSmartPareto() {
    val robustify = Robustify()
    val config = jacksonObjectMapper().readValue(File("config-pareto.json"), RobustifyConfigJSON::class.java)
    val robustifier = robustify.buildSupervisory(config)

    robustifier.use {
      val expected = listOf(
        Pair(
          listOf("vote"),
          listOf("back", "confirm", "password", "select", "v.enter", "v.exit", "vote")
        ),
        Pair(
          listOf("select"),
          listOf("back", "confirm", "password", "select", "v.enter", "v.exit", "vote")
        ),
        Pair(
          listOf("confirm"),
          listOf("back", "confirm", "password", "select", "v.enter", "v.exit", "vote")
        ),
        Pair(
          listOf("vote"),
          listOf("back", "confirm", "eo.exit", "password", "select", "v.exit", "vote")
        ),
        Pair(
          listOf("confirm"),
          listOf("back", "confirm", "eo.exit", "password", "select", "v.exit", "vote")
        ),
        Pair(
          listOf("vote"),
          listOf("back", "confirm", "eo.exit", "password", "select", "v.enter", "vote")
        ),
        Pair(
          listOf("confirm"),
          listOf("back", "confirm", "eo.exit", "password", "select", "v.enter", "vote")
        ),
        Pair(
          listOf("vote"),
          listOf("back", "confirm", "eo.enter", "password", "select", "v.exit", "vote")
        ),
        Pair(
          listOf("select"),
          listOf("back", "confirm", "eo.enter", "password", "select", "v.exit", "vote")
        ),
        Pair(
          listOf("confirm"),
          listOf("back", "confirm", "eo.enter", "password", "select", "v.exit", "vote")
        ),
        Pair(
          listOf("vote"),
          listOf("back", "confirm", "eo.enter", "password", "select", "v.enter", "vote")
        ),
        Pair(
          listOf("select"),
          listOf("back", "confirm", "eo.enter", "password", "select", "v.enter", "vote")
        ),
        Pair(
          listOf("confirm"),
          listOf("back", "confirm", "eo.enter", "password", "select", "v.enter", "vote")
        ),
        Pair(
          listOf("vote"),
          listOf("back", "confirm", "eo.enter", "eo.exit", "password", "select", "vote")
        ),
        Pair(
          listOf("select"),
          listOf("back", "confirm", "eo.enter", "eo.exit", "password", "select", "vote")
        ),
        Pair(
          listOf("confirm"),
          listOf("back", "confirm", "eo.enter", "eo.exit", "password", "select", "vote")
        )
      )
      assertSynthesisResults(
        expected,
        it.synthesize(Algorithms.Pareto).map { r -> Pair((r as SupervisoryDFA).controllable, r.observable) }
      )
    }
  }
}