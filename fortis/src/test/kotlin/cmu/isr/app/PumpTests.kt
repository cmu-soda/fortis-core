package cmu.isr.app

import cmu.isr.robustify.RobustifierTests
import cmu.isr.robustify.supervisory.Algorithms
import cmu.isr.supervisory.SupervisoryDFA
import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import org.junit.jupiter.api.Disabled
import org.junit.jupiter.api.Test
import java.io.File

@Disabled
class PumpTests : RobustifierTests() {

  @Test
  fun testSmartPareto() {
    val robustify = Robustify()
    val config = jacksonObjectMapper().readValue(File("config-pareto.json"), RobustifyConfigJSON::class.java)
    val robustifier = robustify.buildSupervisory(config)

    robustifier.use {
      val expected = listOf(
        Pair(
          listOf("line.1.change_settings", "line.1.clear_rate", "line.1.confirm_settings",
            "line.1.dispense_main_med_flow", "line.1.flow_complete", "line.1.set_rate", "line.1.start_dispense"),
          listOf("line.1.change_settings", "line.1.clear_rate", "line.1.confirm_settings",
            "line.1.dispense_main_med_flow", "line.1.erase_and_unlock_line", "line.1.flow_complete",
            "line.1.lock_line", "line.1.lock_unit", "line.1.set_rate", "line.1.start_dispense", "line.1.unlock_unit",
            "turn_off", "turn_on", "unplug")
        )
      )
      assertSynthesisResults(
        expected,
        it.synthesize(Algorithms.Pareto).map { r -> Pair((r as SupervisoryDFA).controllable, r.observable) }
      )
    }
  }

}