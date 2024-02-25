package com.pokeskies.skiesclear.utils

import java.util.*

class RandomCollection<E> @JvmOverloads constructor(private val random: Random = Random()) {
    private val map: NavigableMap<Double, E> = TreeMap()
    private var total = 0.0
    fun add(weight: Double, result: E): RandomCollection<E> {
        if (weight <= 0) return this
        total += weight
        map[total] = result
        return this
    }

    fun next(remove: Boolean = false): E {
        val value = random.nextDouble() * total
        val entry = map.higherEntry(value)
        if (remove) {
            total -= entry.key
            map.remove(entry.key)
        }
        return entry.value
    }

    fun size(): Int {
        return map.size
    }

    override fun toString(): String {
        return "RandomCollection(random=$random, map=$map, total=$total)"
    }
}