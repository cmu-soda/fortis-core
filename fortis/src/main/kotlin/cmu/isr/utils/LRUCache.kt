package cmu.isr.utils

class LRUCache<K, V>(private val maxSize: Int) {

  private val map = mutableMapOf<K, DoubleLinkedList.Node<Pair<K, V>>>()
  private val list = DoubleLinkedList<Pair<K, V>>()

  val size: Int get() = list.size

  fun put(key: K, value: V) {
    if (key !in map) {
      if (size >= maxSize)
        evict()
      val node = list.addFirst(Pair(key, value))
      map[key] = node
    } else {
      val node = map[key]!!
      node.value = Pair(key, value)
      list.moveToFront(node)
    }
  }

  operator fun get(key: K): V {
    val n = map[key] ?: error("No such key $key")
    list.moveToFront(n)
    return n.value.second
  }

  fun toList(): List<Pair<K, V>> {
    return list.toList()
  }

  private fun evict() {
    val node = list.removeLast()
    map.remove(node.value.first)
  }

  operator fun contains(key: K): Boolean {
    return key in map
  }

  operator fun set(key: K, value: V) {
    put(key, value)
  }
}

private class DoubleLinkedList<E> {
  class Node<E>(var value: E, var prev: Node<E>?, var next: Node<E>?)

  var size: Int = 0
    private set
  private var first: Node<E>? = null
  private var last: Node<E>? = null

//  val size: Int get() = _size

  fun addFirst(value: E): Node<E> {
    if (first != null) {
      val n = Node(value, null, first)
      first!!.prev = n
      first = n
    } else {
      first = Node(value, null, null)
      last = first
    }
    size++
    return first!!
  }

  fun moveToFront(node: Node<E>) {
    if (node.prev != null) {
      node.prev!!.next = node.next
    } else {
      return
    }
    if (node.next != null) {
      node.next!!.prev = node.prev
    } else {
      last = node.prev
    }

    node.prev = null
    node.next = first
    first!!.prev = node
    first = node
  }

  fun removeLast(): Node<E> {
    if (size == 0)
      error("The list is empty")

    val n = if (size == 1) {
      val n = first!!
      first = null
      last = null
      n
    } else {
      val n = last!!
      last = n.prev
      last!!.next = null
      n.prev = null
      n
    }
    size--
    return n
  }

  fun toList(): List<E> {
    val list = mutableListOf<E>()
    var n = first
    while (n != null) {
      list.add(n.value)
      n = n.next
    }
    return list
  }
}