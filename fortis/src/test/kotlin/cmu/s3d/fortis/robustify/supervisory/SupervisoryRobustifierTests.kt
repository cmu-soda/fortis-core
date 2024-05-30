package cmu.s3d.fortis.robustify.supervisory

import cmu.s3d.fortis.common.Algorithms
import cmu.s3d.fortis.common.Priority
import cmu.s3d.fortis.robustify.RobustifierTests
import cmu.s3d.fortis.supervisory.SupervisoryDFA
import cmu.s3d.fortis.supervisory.supremica.SupremicaRunner
import cmu.s3d.fortis.ts.acceptsSubWord
import cmu.s3d.fortis.ts.lts.ltsa.LTSACall
import cmu.s3d.fortis.ts.lts.ltsa.LTSACall.asDetLTS
import cmu.s3d.fortis.ts.lts.ltsa.LTSACall.compose
import cmu.s3d.fortis.ts.parallel
import net.automatalib.word.Word
import org.junit.jupiter.api.Disabled
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.condition.EnabledIfEnvironmentVariable
import kotlin.test.assertContentEquals
import kotlin.test.assertEquals

class SupervisoryRobustifierTests : RobustifierTests() {

    private fun loadVoting(): SupervisoryRobustifier {
        val sysSpec =
            ClassLoader.getSystemResource("specs/voting/sys.lts")?.readText() ?: error("Cannot find voting/sys.lts")
        val envSpec =
            ClassLoader.getSystemResource("specs/voting/env2.lts")?.readText() ?: error("Cannot find voting/env2.lts")
        val pSpec = ClassLoader.getSystemResource("specs/voting/p.lts")?.readText() ?: error("Cannot find voting/p.lts")

        val sys = LTSACall.compile(sysSpec).compose().asDetLTS()
        val env = LTSACall.compile(envSpec).compose().asDetLTS()
        val safety = LTSACall.compile(pSpec).compose().asDetLTS()
        val back = Word.fromSymbols("select", "back")
//    val back = Word.fromSymbols("select", "back", "select")
//    val back2 = Word.fromSymbols("select", "vote", "back", "back", "select")

        return SupervisoryRobustifier(
            sys,
            env,
            safety,
            progress = listOf("confirm"),
            preferredMap = mapOf(Priority.P3 to listOf(back)),
            controllableMap = mapOf(
                Priority.P0 to listOf("back", "confirm", "password", "select", "vote"),
                Priority.P3 to listOf("eo.enter", "eo.exit", "v.enter", "v.exit")
            ),
            observableMap = mapOf(
                Priority.P0 to listOf("back", "confirm", "password", "select", "vote"),
                Priority.P2 to listOf("eo.enter", "eo.exit", "v.enter", "v.exit")
            ),
            synthesizer = SupremicaRunner(),
            maxIter = 1
        )
    }

    private fun loadVoting2(): SupervisoryRobustifier {
        val sysSpec =
            ClassLoader.getSystemResource("specs/voting-2/sys.lts")?.readText() ?: error("Cannot find voting-2/sys.lts")
        val envSpec =
            ClassLoader.getSystemResource("specs/voting-2/env2.lts")?.readText()
                ?: error("Cannot find voting-2/env2.lts")
        val pSpec =
            ClassLoader.getSystemResource("specs/voting-2/p2.lts")?.readText() ?: error("Cannot find voting-2/p2.lts")

        val sys = LTSACall.compile(sysSpec).compose().asDetLTS()
        val env = LTSACall.compile(envSpec).compose().asDetLTS()
        val safety = LTSACall.compileSafetyLTL(pSpec, "SELECT_VOTE_BY_VOTER").asDetLTS()
        val back1 = Word.fromSymbols("select", "back", "select", "vote", "confirm")
        val back2 = Word.fromSymbols("select", "vote", "back", "back", "select", "vote", "confirm")

        return SupervisoryRobustifier(
            sys,
            env,
            safety,
            progress = listOf("confirm"),
            preferredMap = mapOf(Priority.P3 to listOf(back1, back2)),
            controllableMap = mapOf(
                Priority.P0 to listOf("back", "confirm", "password", "select", "vote"),
                Priority.P3 to listOf("eo.enter", "eo.exit", "v.enter", "v.exit")
            ),
            observableMap = mapOf(
                Priority.P0 to listOf("back", "confirm", "password", "select", "vote"),
                Priority.P2 to listOf("eo.enter", "eo.exit", "v.enter", "v.exit")
            ),
            synthesizer = SupremicaRunner(),
            maxIter = 1
        )
    }

    private fun loadTherac(): SupervisoryRobustifier {
        val sysSpec =
            ClassLoader.getSystemResource("specs/therac25/sys.lts")?.readText() ?: error("Cannot find therac25/sys.lts")
        val envSpec =
            ClassLoader.getSystemResource("specs/therac25/env.lts")?.readText() ?: error("Cannot find therac25/env.lts")
        val pSpec =
            ClassLoader.getSystemResource("specs/therac25/p.lts")?.readText() ?: error("Cannot find therac25/p.lts")
        val back1 = Word.fromSymbols("x", "up", "e", "enter", "b")
        val back2 = Word.fromSymbols("e", "up", "x", "enter", "b")
        val back3 = Word.fromSymbols("x", "enter", "up", "up", "e", "enter", "b")
        val back4 = Word.fromSymbols("e", "enter", "up", "up", "x", "enter", "b")

        val sys = LTSACall.compile(sysSpec).compose().asDetLTS()
        val env = LTSACall.compile(envSpec).compose().asDetLTS()
        val safety = LTSACall.compile(pSpec).compose().asDetLTS()

        return SupervisoryRobustifier(
            sys,
            env,
            safety,
            progress = listOf("fire_xray", "fire_ebeam"),
            preferredMap = mapOf(Priority.P3 to listOf(back1, back2), Priority.P2 to listOf(back3, back4)),
            controllableMap = mapOf(
                Priority.P0 to listOf("fire_xray", "fire_ebeam", "setMode"),
                Priority.P1 to listOf("x", "e", "enter", "up", "b")
            ),
            observableMap = mapOf(
                Priority.P0 to listOf("x", "e", "enter", "up", "b", "fire_xray", "fire_ebeam", "setMode"),
            ),
            synthesizer = SupremicaRunner(),
            maxIter = 1
        )
    }

    private fun loadTherac2(): SupervisoryRobustifier {
        val sysSpec =
            ClassLoader.getSystemResource("specs/therac25-2/sys.lts")?.readText()
                ?: error("Cannot find therac25-2/sys.lts")
        val envSpec =
            ClassLoader.getSystemResource("specs/therac25-2/env.lts")?.readText()
                ?: error("Cannot find therac25-2/env.lts")
        val pSpec = ClassLoader.getSystemResource("specs/therac25-2/p1.lts")?.readText()
            ?: error("Cannot find therac25-2/p1.lts")
        val back1 = Word.fromSymbols("x", "up", "e", "enter", "b")
        val back2 = Word.fromSymbols("e", "up", "x", "enter", "b")
//    val back3 = Word.fromSymbols("x", "enter", "up", "up", "e", "enter", "b")
//    val back4 = Word.fromSymbols("e", "enter", "up", "up", "x", "enter", "b")

        val sys = LTSACall.compile(sysSpec).compose().asDetLTS()
        val env = LTSACall.compile(envSpec).compose().asDetLTS()
        val safety = LTSACall.compileSafetyLTL(pSpec, "OVER_DOSE").asDetLTS()

        return SupervisoryRobustifier(
            sys,
            env,
            safety,
            progress = listOf("fire_xray", "fire_ebeam"),
            preferredMap = mapOf(Priority.P3 to listOf(back1, back2)),
            controllableMap = mapOf(
                Priority.P1 to listOf(
                    "x",
                    "e",
                    "enter",
                    "up",
                    "b",
                    "fire_xray",
                    "fire_ebeam",
                    "set_xray",
                    "set_ebeam",
                    "reset"
                )
            ),
            observableMap = mapOf(
                Priority.P0 to listOf(
                    "x",
                    "e",
                    "enter",
                    "up",
                    "b",
                    "fire_xray",
                    "fire_ebeam",
                    "set_xray",
                    "set_ebeam",
                    "reset"
                )
            ),
            synthesizer = SupremicaRunner(),
            maxIter = 1
        )
    }

    private fun loadTherac3(): SupervisoryRobustifier {
        val sysSpec =
            ClassLoader.getSystemResource("specs/therac25/sys.lts")?.readText() ?: error("Cannot find therac25/sys.lts")
        val envSpec =
            ClassLoader.getSystemResource("specs/therac25/env.lts")?.readText() ?: error("Cannot find therac25/env.lts")
        val pSpec =
            ClassLoader.getSystemResource("specs/therac25/p.lts")?.readText() ?: error("Cannot find therac25/p.lts")
        val back1 = Word.fromSymbols("x", "up", "e", "enter", "b")
        val back2 = Word.fromSymbols("e", "up", "x", "enter", "b")
        val back3 = Word.fromSymbols("x", "enter", "up", "up", "e", "enter", "b")
        val back4 = Word.fromSymbols("e", "enter", "up", "up", "x", "enter", "b")

        val sys = LTSACall.compile(sysSpec).compose().asDetLTS()
        val env = LTSACall.compile(envSpec).compose().asDetLTS()
        val safety = LTSACall.compile(pSpec).compose().asDetLTS()

        return SupervisoryRobustifier(
            sys,
            env,
            safety,
            progress = emptyList(),
            preferredMap = mapOf(
                Priority.P3 to listOf(back1, back2),
                Priority.P2 to listOf(back3, back4),
                Priority.P0 to listOf(
                    Word.fromSymbols("x", "enter", "b", "fire_xray"),
                    Word.fromSymbols("e", "enter", "b", "fire_ebeam")
                )
            ),
            controllableMap = mapOf(
                Priority.P0 to listOf("fire_xray", "fire_ebeam", "setMode"),
                Priority.P1 to listOf("x", "e", "enter", "up", "b")
            ),
            observableMap = mapOf(
                Priority.P0 to listOf("x", "e", "enter", "up", "b", "fire_xray", "fire_ebeam", "setMode"),
            ),
            synthesizer = SupremicaRunner(),
            maxIter = 1
        )
    }

    private fun loadPump(): SupervisoryRobustifier {
        val powerSpec =
            ClassLoader.getSystemResource("specs/pump/power.lts")?.readText() ?: error("Cannot find pump/power.lts")
        val linesSpec =
            ClassLoader.getSystemResource("specs/pump/lines.lts")?.readText() ?: error("Cannot find pump/lines.lts")
        val alarmSpec =
            ClassLoader.getSystemResource("specs/pump/alarm.lts")?.readText() ?: error("Cannot find pump/alarm.lts")
        val envSepc =
            ClassLoader.getSystemResource("specs/pump/deviation.lts")?.readText()
                ?: error("Cannot find pump/deviation.lts")
        val pSpec =
            ClassLoader.getSystemResource("specs/pump/p.lts")?.readText() ?: error("Cannot find pump/p.lts")

        val power = LTSACall.compile(powerSpec).compose().asDetLTS()
        val lines = LTSACall.compile(linesSpec).compose().asDetLTS()
        val alarm = LTSACall.compile(alarmSpec).compose().asDetLTS()
        val sys = parallel(power, lines, alarm)
        val env = LTSACall.compile(envSepc).compose().asDetLTS()
        val safety = LTSACall.compile(pSpec).compose().asDetLTS()

        val ideal = Word.fromSymbols(
            "plug_in",
            "battery_charge",
            "battery_charge",
            "turn_on",
            "line.1.dispense_main_med_flow",
            "line.1.flow_complete"
        )
        val recover = Word.fromSymbols(
            "plug_in", "line.1.start_dispense", "line.1.dispense_main_med_flow", "line.1.dispense_main_med_flow",
            "power_failure", "plug_in", "line.1.start_dispense", "line.1.dispense_main_med_flow"
        )

        return SupervisoryRobustifier(
            sys,
            env,
            safety,
            progress = listOf("line.1.flow_complete"),
            preferredMap = mapOf(Priority.P3 to listOf(ideal, recover)),
            controllableMap = mapOf(
                Priority.P0 to listOf(
                    "line.1.start_dispense",
                    "line.1.dispense_main_med_flow",
                    "line.1.flow_complete",
                ),
                Priority.P1 to listOf(
                    "line.1.change_settings",
                    "line.1.clear_rate",
                    "line.1.confirm_settings",
                    "line.1.set_rate",
                ),
                Priority.P3 to listOf(
                    "line.1.erase_and_unlock_line",
                    "line.1.lock_line",
                    "line.1.lock_unit",
                    "line.1.unlock_unit",
                )
            ),
            observableMap = mapOf(
                Priority.P0 to listOf(
                    "line.1.start_dispense",
                    "line.1.dispense_main_med_flow",
                    "line.1.flow_complete",
                    "line.1.change_settings",
                    "line.1.clear_rate",
                    "line.1.confirm_settings",
                    "line.1.set_rate",
                ),
                Priority.P2 to listOf(
                    // Events of the line that are related to the physical world have moderate cost to observe
                    "line.1.erase_and_unlock_line",
                    "line.1.lock_line",
                    "line.1.lock_unit",
                    "line.1.unlock_unit",
                    // Has moderate cost to observe some system events in other modules
                    "alarm_silence",
                    "enable_alarm",
                ),
                Priority.P3 to listOf(
                    // Has high cost to observe some events of other modules related to the physical world.
                    "battery_charge",
                    "battery_spent",
                    "plug_in",
                    "unplug",
                    "turn_off",
                    "turn_on",
                )
            ),
            synthesizer = SupremicaRunner(),
            maxIter = 1
        )
    }

    @Test
    fun testComputeWeights() {
        val robustifier = loadVoting()

        robustifier.use {
            val weights = it.computeWeights()

            assertEquals(0, weights.controllable["back"])
            assertEquals(0, weights.controllable["confirm"])
            assertEquals(0, weights.controllable["password"])
            assertEquals(0, weights.controllable["select"])
            assertEquals(0, weights.controllable["vote"])

            assertEquals(0, weights.observable["back"])
            assertEquals(0, weights.observable["confirm"])
            assertEquals(0, weights.observable["password"])
            assertEquals(0, weights.observable["select"])
            assertEquals(0, weights.observable["vote"])

            assertEquals(-1, weights.observable["eo.enter"])
            assertEquals(-1, weights.observable["eo.exit"])
            assertEquals(-1, weights.observable["v.enter"])
            assertEquals(-1, weights.observable["v.exit"])

            assertEquals(-5, weights.controllable["eo.enter"])
            assertEquals(-5, weights.controllable["eo.exit"])
            assertEquals(-5, weights.controllable["v.enter"])
            assertEquals(-5, weights.controllable["v.exit"])

            assertEquals(5, weights.preferred[Word.fromSymbols("select", "back")])
        }
    }

    @Test
    fun testPreferredBehIterator() {
        val a = Word.fromSymbols('a', 'b')
        val preferred = mapOf(Priority.P1 to listOf(a))
        val iter = PreferredBehIterator(preferred).asSequence().toList()
        assertContentEquals(
            listOf(listOf(emptyList()), listOf(listOf(a))),
            iter
        )
    }

    @Test
    fun testPreferredBehIterator2() {
        val a = Word.fromSymbols('a', 'b')
        val b = Word.fromSymbols('c', 'd')
        val c = Word.fromSymbols('e', 'f')
        val preferred = mapOf(Priority.P1 to listOf(a), Priority.P2 to listOf(b), Priority.P3 to listOf(c))
        val iter = PreferredBehIterator(preferred).asSequence().toList()
        assertContentEquals(
            listOf(
                listOf(emptyList()),
                listOf(listOf(a)),
                listOf(listOf(b)),
                listOf(listOf(a, b)),
                listOf(listOf(c)),
                listOf(listOf(a, c)),
                listOf(listOf(b, c)),
                listOf(listOf(a, b, c))
            ),
            iter
        )
    }

    @Test
    fun testPreferredBehIterator3() {
        val a = Word.fromSymbols('a', 'b')
        val b = Word.fromSymbols('c', 'd')
        val c = Word.fromSymbols('e', 'f')
        val preferred = mapOf(Priority.P1 to listOf(a, b), Priority.P3 to listOf(c))
        val iter = PreferredBehIterator(preferred).asSequence().toList()
        assertContentEquals(
            listOf(
                listOf(emptyList()),
                listOf(listOf(a), listOf(b)),
                listOf(listOf(a, b)),
                listOf(listOf(c)),
                listOf(listOf(a, c), listOf(b, c)),
                listOf(listOf(a, b, c)),
            ),
            iter
        )
    }

    @Test
    fun testVoting() {
        val robustifier = loadVoting()

        robustifier.use {
            val paretoExpected = listOf(
                Pair(
                    listOf("confirm", "select", "back", "password", "vote"),
                    listOf("back", "confirm", "password", "select", "vote", "v.enter", "v.exit")
                ),
                Pair(
                    listOf("confirm", "select", "back", "password", "vote"),
                    listOf("back", "confirm", "password", "select", "vote", "eo.exit", "v.exit")
                ),
                Pair(
                    listOf("confirm", "select", "back", "password", "vote"),
                    listOf("back", "confirm", "password", "select", "vote", "eo.exit", "v.enter")
                ),
                Pair(
                    listOf("confirm", "select", "back", "password", "vote"),
                    listOf("back", "confirm", "password", "select", "vote", "eo.enter", "v.exit")
                ),
                Pair(
                    listOf("confirm", "select", "back", "password", "vote"),
                    listOf("back", "confirm", "password", "select", "vote", "eo.enter", "v.enter")
                ),
                Pair(
                    listOf("confirm", "select", "back", "password", "vote"),
                    listOf("back", "confirm", "password", "select", "vote", "eo.enter", "eo.exit")
                )
            )
            assertSynthesisResults(
                paretoExpected,
                it.synthesize(Algorithms.Pareto).map { r -> Pair((r as SupervisoryDFA).controllable, r.observable) }
            )

            val fastExpected = listOf(
                Pair(
                    listOf("confirm", "select", "back", "password", "vote"),
                    listOf("back", "confirm", "password", "select", "vote", "v.enter", "v.exit")
                )
            )
            assertSynthesisResults(
                fastExpected,
                it.synthesize(Algorithms.Fast).map { r -> Pair((r as SupervisoryDFA).controllable, r.observable) }
            )
        }
    }

    @Test
    fun testVoting2() {
        val robustifier = loadVoting2()

        robustifier.use {
            val fastExpected = listOf(
                Pair(
                    listOf("back", "confirm", "password", "select", "v.exit", "vote"),
                    listOf("back", "confirm", "password", "select", "v.exit", "vote")
                )
            )
            assertSynthesisResults(
                fastExpected,
                it.synthesize(Algorithms.Fast).map { r -> Pair((r as SupervisoryDFA).controllable, r.observable) }
            )
        }
    }

    @Test
    fun testTherac() {
        val robustifier = loadTherac()

        robustifier.use {
            val paretoExpected = listOf(
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
                paretoExpected,
                it.synthesize(Algorithms.Pareto).map { r -> Pair((r as SupervisoryDFA).controllable, r.observable) }
            )

            val fastExpected = listOf(
                Pair(
                    listOf("enter", "fire_ebeam", "fire_xray", "setMode"),
                    listOf("b", "e", "enter", "fire_ebeam", "fire_xray", "setMode", "up", "x")
                )
            )
            assertSynthesisResults(
                fastExpected,
                it.synthesize(Algorithms.Fast).map { r -> Pair((r as SupervisoryDFA).controllable, r.observable) }
            )
        }
    }

    @Test
    fun testTherac2() {
        val robustifier = loadTherac2()

        robustifier.use {
            val solutions = it.synthesize(Algorithms.Fast).toList()
            solutions.forEach { r ->
                assert(!acceptsSubWord(r, Word.fromSymbols("x", "up", "e", "enter", "b")).first)
            }
            val fastExpected = listOf(
                Pair(
                    listOf("up"),
                    "b, e, enter, fire_ebeam, fire_xray, reset, set_ebeam, set_xray, up, x".split(", ")
                )
            )
            assertSynthesisResults(
                fastExpected,
                solutions.map { r -> Pair((r as SupervisoryDFA).controllable, r.observable) }
            )
        }
    }

    @Test
    @Disabled("Must have (P0) preferred behavior is problematic.")
    fun testTherac3() {
        val robustifier = loadTherac3()

        robustifier.use {
            val paretoExpected = listOf(
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
                paretoExpected,
                it.synthesize(Algorithms.Pareto).map { r -> Pair((r as SupervisoryDFA).controllable, r.observable) }
            )

            val fastExpected = listOf(
                Pair(
                    listOf("enter", "fire_ebeam", "fire_xray", "setMode"),
                    listOf("b", "e", "enter", "fire_ebeam", "fire_xray", "setMode", "up", "x")
                )
            )
            assertSynthesisResults(
                fastExpected,
                it.synthesize(Algorithms.Fast).map { r -> Pair((r as SupervisoryDFA).controllable, r.observable) }
            )
        }
    }

    @Test
    @EnabledIfEnvironmentVariable(named = "tests", matches = "all")
    fun testPump() {
        val robustifier = loadPump()

        robustifier.use {
            val paretoExpected = listOf(
                Pair(
                    listOf(
                        "line.1.dispense_main_med_flow",
                        "line.1.flow_complete",
                        "line.1.set_rate",
                        "line.1.start_dispense"
                    ),
                    listOf(
                        "battery_charge",
                        "line.1.change_settings",
                        "line.1.clear_rate",
                        "line.1.confirm_settings",
                        "line.1.dispense_main_med_flow",
                        "line.1.flow_complete",
                        "line.1.set_rate",
                        "line.1.start_dispense",
                        "unplug"
                    )
                )
            )
            assertSynthesisResults(
                paretoExpected,
                it.synthesize(Algorithms.Pareto).map { r -> Pair((r as SupervisoryDFA).controllable, r.observable) }
            )

            val fastExpected = listOf(
                Pair(
                    listOf(
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
                        "line.1.flow_complete",
                        "line.1.set_rate",
                        "line.1.start_dispense",
                        "turn_off",
                        "turn_on",
                        "unplug"
                    )
                )
            )
            assertSynthesisResults(
                fastExpected,
                it.synthesize(Algorithms.Fast).map { r -> Pair((r as SupervisoryDFA).controllable, r.observable) }
            )
        }
    }
}