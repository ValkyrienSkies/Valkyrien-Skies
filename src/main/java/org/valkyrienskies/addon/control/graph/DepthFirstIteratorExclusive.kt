package org.valkyrienskies.addon.control.graph

import java.util.*

class DepthFirstIteratorExclusive<V : GraphVertex<V, E>, E>(
        start: V
) : Iterator<V> {

    private val visited = HashSet<V>()
    private val toVisit = ArrayDeque<V>()

    init {
        visited += start
        for (vertex in start.direct.vertices) {
            if (!visited.contains(vertex)) {
                toVisit += vertex
                visited += vertex
            }
        }
    }

    override fun hasNext(): Boolean {
        return !toVisit.isEmpty()
    }

    override fun next(): V {
        val currentVertex = toVisit.pop()
        for (vertex in currentVertex.direct.vertices) {
            if (!visited.contains(vertex)) {
                toVisit += vertex
                visited += vertex
            }
        }
        return currentVertex
    }

}