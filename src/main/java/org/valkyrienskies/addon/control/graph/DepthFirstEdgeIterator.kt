package org.valkyrienskies.addon.control.graph

import java.util.*
import kotlin.collections.HashMap
import kotlin.collections.Iterator
import kotlin.collections.component1
import kotlin.collections.component2
import kotlin.collections.iterator
import kotlin.collections.plusAssign
import kotlin.collections.set

class DepthFirstEdgeIterator<V : GraphVertex<V, E>, E>(
        start: V
) : Iterator<GraphIteration<V, E>> {

    private val visited = HashMap<V, Boolean>()
    private val toVisit = ArrayDeque<GraphIteration<V, E>>()

    init {
        for ((vertex, edge) in start.direct.vertexEdgeMap)
            toVisit += GraphIteration(start, vertex, edge)
    }

    override fun hasNext(): Boolean = !toVisit.isEmpty()

    override fun next(): GraphIteration<V, E> {
        val iteration = toVisit.pop()
        val (v1, v2) = iteration

        visited[v1] = true

        for ((vertex, edge) in v2.direct.vertexEdgeMap) {
            if (visited[vertex] == false)
                toVisit += GraphIteration(v2, vertex, edge)
        }

        return iteration
    }
}

data class GraphIteration<V : GraphVertex<V, E>, E>(
        val v1: V,
        val v2: V,
        val edge: E
)