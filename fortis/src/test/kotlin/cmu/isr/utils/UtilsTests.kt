package cmu.isr.utils

import org.junit.jupiter.api.Test
import kotlin.test.assertContentEquals
import kotlin.test.assertEquals

class UtilsTests {

  @Test
  fun testCombinations() {
    val l = listOf('a', 'b', 'c', 'd')
    val cs = l.combinations(2)
    assertContentEquals(
      listOf(
        listOf('a', 'b'),
        listOf('a', 'c'),
        listOf('a', 'd'),
        listOf('b', 'c'),
        listOf('b', 'd'),
        listOf('c', 'd')
      ),
      cs
    )
  }

  @Test
  fun testCombinations2() {
    val l = listOf('a')
    val cs = l.combinations(1)
    assertContentEquals(
      listOf(listOf('a')),
      cs
    )
  }

  @Test
  fun testCombinations3() {
    val l = listOf('a')
    val cs = l.combinations(0)
    assertContentEquals(
      listOf(emptyList()),
      cs
    )
  }

  @Test
  fun testLRUCache() {
    val cache = LRUCache<String, Int>(3)
    assertEquals(0, cache.size)
    assertEquals(emptyList(), cache.toList())

    cache.put("a", 1)
    cache.put("b", 2)
    cache.put("c", 3)
    assertEquals(3, cache.size)
    assertEquals(listOf(Pair("c", 3), Pair("b", 2), Pair("a", 1)), cache.toList())

    cache.put("a", 4)
    assertEquals(3, cache.size)
    assertEquals(listOf(Pair("a", 4), Pair("c", 3), Pair("b", 2)), cache.toList())

    cache.put("d", 5)
    assertEquals(3, cache.size)
    assertEquals(listOf(Pair("d", 5), Pair("a", 4), Pair("c", 3)), cache.toList())

    assertEquals(3, cache.get("c"))
    assertEquals(3, cache.size)
    assertEquals(listOf(Pair("c", 3), Pair("d", 5), Pair("a", 4)), cache.toList())

    assertEquals(5, cache.get("d"))
    assertEquals(3, cache.size)
    assertEquals(listOf(Pair("d", 5), Pair("c", 3), Pair("a", 4)), cache.toList())
  }

}