package cmu.s3d.fortis.utils

class OrderedPowerSetIterator<I>(val inputs: List<I>) : Iterator<Collection<I>> {
    private var k = 0
    private val queue = ArrayDeque<Collection<I>>()

    override fun hasNext(): Boolean {
        while (queue.isEmpty() && k <= inputs.size) {
            queue.addAll(inputs.combinations(k))
            k++
        }
        return queue.isNotEmpty()
    }

    override fun next(): Collection<I> {
        return queue.removeFirst()
    }

}