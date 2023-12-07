package cmu.s3d.fortis.cli

import cmu.s3d.fortis.common.Algorithms
import cmu.s3d.fortis.robustify.RobustifierTests
import cmu.s3d.fortis.supervisory.SupervisoryDFA
import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.TestInstance
import java.io.File
import java.nio.file.Files
import java.nio.file.Paths

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class TheracTests : RobustifierTests() {

    private val tempPath = Files.createDirectories(Paths.get("fortis-test-temp/therac25"))

    @Test
    fun testSmartPareto() {
        try {
            copyResourceToDirectory("specs/therac25/sys.lts", tempPath)
            copyResourceToDirectory("specs/therac25/env.lts", tempPath)
            copyResourceToDirectory("specs/therac25/p.lts", tempPath)
            copyResourceToDirectory("specs/therac25/config-pareto.json", tempPath)

            val robustify = Robustify()
            val config = jacksonObjectMapper().readValue(
                File("fortis-test-temp/therac25/config-pareto.json"),
                RobustifyConfigJSON::class.java
            )
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
        } finally {
            File("fortis-test-temp").deleteRecursively()
        }
    }
}