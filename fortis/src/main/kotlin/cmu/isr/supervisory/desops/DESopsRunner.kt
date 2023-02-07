package cmu.isr.supervisory.desops


import cmu.isr.robustify.supervisory.extendAlphabet
import cmu.isr.robustify.supervisory.observer
import cmu.isr.supervisory.SupervisoryDFA
import cmu.isr.supervisory.SupervisorySynthesizer
import cmu.isr.supervisory.asSupDFA
import cmu.isr.ts.alphabet
import cmu.isr.utils.pretty
import org.slf4j.LoggerFactory
import java.io.BufferedReader
import java.io.FileOutputStream
import java.net.Socket
import java.time.Duration


class DESopsRunner : SupervisorySynthesizer<Int, String> {

  private val logger = LoggerFactory.getLogger(javaClass)
  private val desops = ClassLoader.getSystemResource("scripts/desops.py")?.readBytes() ?: error("Cannot find desops.py")
  private val process: Process
  private val processStdErr: BufferedReader
  private val port = 5000

  init {
    val out = FileOutputStream("./desops.py")
    out.write(desops)
    out.close()

    val processBuilder = ProcessBuilder("python", "desops.py")
    process = processBuilder.start()
    processStdErr = process.errorStream.bufferedReader()

    Thread.sleep(500) // Wait the DESops process to start
  }

  private fun synthesizeRaw(plant: SupervisoryDFA<*, String>, prop: SupervisoryDFA<*, String>): SupervisoryDFA<Int, String>? {
    val inputs1 = plant.alphabet()
    val inputs2 = prop.alphabet()
    // check alphabets
    if (inputs1 != inputs2)
      error("The plant and the property should have the same alphabet")
    checkAlphabets(plant, prop)

    if (!process.isAlive) {
      error("The DESops process has terminated. Caused by: ${processStdErr.readText()}")
    }

    val startTime = System.currentTimeMillis()
    val socket = Socket("127.0.0.1", port)
    var sup: SupervisoryDFA<Int, String>? = null

    socket.use {
      val outStream = it.getOutputStream()
      write(outStream, plant)
      write(outStream, prop)

      val inStream = it.getInputStream()
      val reader = inStream.bufferedReader()
      sup = when (reader.readLine()) {
        "0" -> {
          val dfa = parse(reader, inputs1, plant.controllable, plant.observable)
          if (dfa is SupervisoryDFA<*, *>) {
            logger.debug("Synthesis spent ${Duration.ofMillis(System.currentTimeMillis() - startTime).pretty()}")
            observer(dfa as SupervisoryDFA<Int, String>, dfa.alphabet())
          } else {
            error("Does not support NFA synthesis!")
          }
        }
        "1" -> null
        else -> error(processStdErr.readLine())
      }
    }

    return sup
  }

  override fun synthesize(plant: SupervisoryDFA<*, String>, prop: SupervisoryDFA<*, String>): SupervisoryDFA<Int, String>? {
    val inputs1 = plant.alphabet()
    val inputs2 = prop.alphabet()
    // DESops requires the prop to have the same alphabet as the plant
    if (inputs1 != inputs2) {
      val extendedProp = extendAlphabet(prop, inputs2, inputs1).asSupDFA(
        prop.controllable union plant.controllable, prop.observable union plant.observable)
      return synthesizeRaw(plant, extendedProp)
    }
    return synthesizeRaw(plant, prop)
  }

  override fun close() {
    process.destroy()
  }
}