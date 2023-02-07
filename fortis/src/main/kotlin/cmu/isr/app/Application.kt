package cmu.isr.app

import com.github.ajalt.clikt.core.CliktCommand
import com.github.ajalt.clikt.core.subcommands

class Application : CliktCommand(
  name = "LTS-Robustness",
  help = "A tool to compute and robustify a system design modeled in labelled transition systems (LTS)."
) {
  override fun run() {}
}

fun main(args: Array<String>) {
  Application().subcommands(Robustness(), Robustify()).main(args)
}