package cmu.isr.ts.lts.ltsa

/**
 * This class is a simple String implementation of the LTSInput interface in order to
 * use the ltsa.jar.
 */
class StringLTSInput(private val source: String) : lts.LTSInput {
  private var pos: Int = -1

  override fun nextChar(): Char {
    ++pos
    return if (pos < source.length) source[pos] else '\u0000'
  }

  override fun backChar(): Char {
    --pos
    return if (pos < 0) {
      pos = 0
      '\u0000'
    } else {
      source[pos]
    }
  }

  override fun getMarker(): Int {
    return pos
  }
}

/**
 * This class is a simple String implementation of the LTSOutput interface in order to
 * use the ltsa.jar.
 */
class StringLTSOutput : lts.LTSOutput {
  private val text: StringBuilder = StringBuilder()

  fun getText(): String {
    return text.toString()
  }

  override fun out(s: String) {
    text.append(s)
  }

  override fun outln(s: String) {
    text.appendLine(s)
  }

  override fun clearOutput() {
    text.setLength(0)
  }
}