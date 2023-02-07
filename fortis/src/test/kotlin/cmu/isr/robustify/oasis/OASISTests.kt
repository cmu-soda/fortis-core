package cmu.isr.robustify.oasis

import cmu.isr.supervisory.supremica.SupremicaRunner
import cmu.isr.ts.lts.ltsa.LTSACall
import cmu.isr.ts.lts.ltsa.LTSACall.asDetLTS
import cmu.isr.ts.lts.ltsa.LTSACall.compose
import cmu.isr.ts.parallel
import net.automatalib.words.Word
import org.junit.jupiter.api.Test
import kotlin.test.assertContentEquals
import kotlin.test.assertNotNull

class OASISTests {

  private fun loadVoting(): OASISRobustifier<Int, String> {
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

    return OASISRobustifier(
      sys,
      env,
      safety,
      progress = listOf("confirm"),
      preferred = listOf(back),
      synthesizer = SupremicaRunner()
    )
  }

  private fun loadTherac(): OASISRobustifier<Int, String> {
    val sysSpec =
      ClassLoader.getSystemResource("specs/therac25/sys.lts")?.readText() ?: error("Cannot find therac25/sys.lts")
    val envSpec =
      ClassLoader.getSystemResource("specs/therac25/env.lts")?.readText() ?: error("Cannot find therac25/env.lts")
    val pSpec = ClassLoader.getSystemResource("specs/therac25/p.lts")?.readText() ?: error("Cannot find therac25/p.lts")
    val back1 = Word.fromSymbols("x", "up")
    val back2 = Word.fromSymbols("e", "up")
    val back3 = Word.fromSymbols("enter", "up")

    val sys = LTSACall.compile(sysSpec).compose().asDetLTS()
    val env = LTSACall.compile(envSpec).compose().asDetLTS()
    val safety = LTSACall.compile(pSpec).compose().asDetLTS()

    return OASISRobustifier(
      sys,
      env,
      safety,
      progress = listOf("fire_xray", "fire_ebeam"),
      preferred = listOf(back1, back2, back3),
      synthesizer = SupremicaRunner()
    )
  }

  private fun loadPump(): OASISRobustifier<Int, String> {
    val powerSpec =
      ClassLoader.getSystemResource("specs/pump/power.lts")?.readText() ?: error("Cannot find pump/power.lts")
    val linesSpec =
      ClassLoader.getSystemResource("specs/pump/lines.lts")?.readText() ?: error("Cannot find pump/lines.lts")
    val alarmSpec =
      ClassLoader.getSystemResource("specs/pump/alarm.lts")?.readText() ?: error("Cannot find pump/alarm.lts")
    val envSepc =
      ClassLoader.getSystemResource("specs/pump/deviation.lts")?.readText() ?: error("Cannot find pump/deviation.lts")
    val pSpec =
      ClassLoader.getSystemResource("specs/pump/p.lts")?.readText() ?: error("Cannot find pump/p.lts")

    val power = LTSACall.compile(powerSpec).compose().asDetLTS()
    val lines = LTSACall.compile(linesSpec).compose().asDetLTS()
    val alarm = LTSACall.compile(alarmSpec).compose().asDetLTS()
    val sys = parallel(power, lines, alarm)
    val env = LTSACall.compile(envSepc).compose().asDetLTS()
    val safety = LTSACall.compile(pSpec).compose().asDetLTS()

    val ideal = Word.fromSymbols("plug_in", "battery_charge", "battery_charge", "turn_on", "line.1.dispense_main_med_flow", "line.1.flow_complete")
    val recover = Word.fromSymbols("plug_in", "line.1.start_dispense", "line.1.dispense_main_med_flow", "line.1.dispense_main_med_flow",
      "power_failure", "plug_in", "line.1.start_dispense", "line.1.dispense_main_med_flow")

    return OASISRobustifier(
      sys,
      env,
      safety,
      progress = listOf("line.1.flow_complete"),
      preferred = listOf(ideal, recover),
      synthesizer = SupremicaRunner()
    )
  }

  @Test
  fun testPowerSetIterator() {
    val iter = OrderedPowerSetIterator(listOf("a", "b", "c"))
    assertContentEquals(
      listOf(
        emptyList(),
        listOf("a"), listOf("b"), listOf("c"),
        listOf("a", "b"), listOf("a", "c"), listOf("b", "c"),
        listOf("a", "b", "c")
      ),
      iter.asSequence().toList()
    )
  }

  @Test
  fun testVoting() {
    val robustifier = loadVoting()
    val sup = robustifier.synthesize()

    assertNotNull(sup)
  }

  @Test
  fun testTherac() {
    val robustifier = loadTherac()
    val sup = robustifier.synthesize()

    assertNotNull(sup)
  }

  @Test
  fun testPump() {
    val robustifier = loadPump()
    val sup = robustifier.synthesize()

    assertNotNull(sup)
  }
}