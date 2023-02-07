package cmu.isr.utils

import java.time.Duration
import java.util.*

fun Duration.pretty(): String {
  return "%02d:%02d:%02d:%03d".format(toHours(), toMinutes() % 60, seconds % 60, toMillis() % 1000)
}


fun <E> List<E>.combinations(k: Int): Collection<Collection<E>> {
  if (k > this.size)
    error("k should not be bigger than the size of this list")

  val l = mutableListOf<Collection<E>>()
  val c = (0 until k).toMutableList()

  while (true) {
    l.add(c.map { this[it] })

    var i = k - 1
    while (i >= 0 && c[i] == this.size - k + i)
      i--
    if (i < 0)
      break
    c[i]++
    for (j in i+1 until k)
      c[j] = c[j-1] + 1
  }

  return l
}

fun BitSet.forEachSetBit(f: (Int) -> Unit) {
  var i = this.nextSetBit(0)
  while (i >= 0) {
    if (i == Int.MAX_VALUE)
      break
    f(i)
    i = this.nextSetBit(i+1)
  }
}