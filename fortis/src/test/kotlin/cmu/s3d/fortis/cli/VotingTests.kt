package cmu.s3d.fortis.cli

import cmu.s3d.fortis.common.Algorithms
import cmu.s3d.fortis.robustify.RobustifierTests
import cmu.s3d.fortis.supervisory.SupervisoryDFA
import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import org.junit.jupiter.api.Test
import java.io.File
import java.nio.file.Files
import java.nio.file.Paths

class VotingTests : RobustifierTests() {
    private val tempPath = Files.createDirectories(Paths.get("fortis-test-temp/voting"))

    @Test
    fun testSmartPareto() {
        try {
            copyResourceToDirectory("specs/voting/sys.lts", tempPath)
            copyResourceToDirectory("specs/voting/env2.lts", tempPath)
            copyResourceToDirectory("specs/voting/p.lts", tempPath)
            copyResourceToDirectory("specs/voting/config-pareto.json", tempPath)

            val robustify = Robustify()
            val config = jacksonObjectMapper().readValue(
                File("fortis-test-temp/voting/config-pareto.json"),
                RobustifyConfigJSON::class.java
            )
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
        } finally {
            File("fortis-test-temp").deleteRecursively()
        }
    }
}