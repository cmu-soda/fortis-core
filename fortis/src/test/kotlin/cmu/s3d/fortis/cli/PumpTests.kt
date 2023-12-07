package cmu.s3d.fortis.cli

import cmu.s3d.fortis.common.Algorithms
import cmu.s3d.fortis.robustify.RobustifierTests
import cmu.s3d.fortis.supervisory.SupervisoryDFA
import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import org.junit.jupiter.api.Test
import java.io.File
import java.nio.file.Files
import java.nio.file.Paths

class PumpTests : RobustifierTests() {

    private val tempPath = Files.createDirectories(Paths.get("fortis-test-temp/pump"))

    @Test
    fun testSmartPareto() {
        try {
            copyResourceToDirectory("specs/pump/lines.lts", tempPath)
            copyResourceToDirectory("specs/pump/deviation.lts", tempPath)
            copyResourceToDirectory("specs/pump/power.lts", tempPath)
            copyResourceToDirectory("specs/pump/alarm.lts", tempPath)
            copyResourceToDirectory("specs/pump/p.lts", tempPath)
            copyResourceToDirectory("specs/pump/config-pareto.json", tempPath)

            val robustify = Robustify()
            val config = jacksonObjectMapper().readValue(
                File("fortis-test-temp/pump/config-pareto.json"),
                RobustifyConfigJSON::class.java
            )
            val robustifier = robustify.buildSupervisory(config)

            robustifier.use {
                val expected = listOf(
                    Pair(
                        listOf(
                            "line.1.change_settings",
                            "line.1.clear_rate",
                            "line.1.confirm_settings",
                            "line.1.dispense_main_med_flow",
                            "line.1.flow_complete",
                            "line.1.set_rate",
                            "line.1.start_dispense"
                        ),
                        listOf(
                            "line.1.change_settings",
                            "line.1.clear_rate",
                            "line.1.confirm_settings",
                            "line.1.dispense_main_med_flow",
                            "line.1.erase_and_unlock_line",
                            "line.1.flow_complete",
                            "line.1.lock_line",
                            "line.1.lock_unit",
                            "line.1.set_rate",
                            "line.1.start_dispense",
                            "line.1.unlock_unit",
                            "turn_off",
                            "turn_on",
                            "unplug"
                        )
                    )
                )
                assertSynthesisResults(
                    expected,
                    it.synthesize(Algorithms.Pareto).map { r -> Pair((r as SupervisoryDFA).controllable, r.observable) }
                )
            }
        } finally {
            File("fortis-test-temp").deleteRecursively()
        }
    }

}