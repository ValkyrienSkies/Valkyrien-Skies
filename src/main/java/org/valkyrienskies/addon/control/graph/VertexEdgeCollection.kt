package org.valkyrienskies.addon.control.graph

import java.util.*
import java.util.function.Supplier
import kotlin.collections.HashMap


class VertexEdgeCollection<V : GraphVertex<V, E>, E>(
        private val owner: V,
        /**
         * Use NullEdgeSupplier if you don't want edges
         */
        private val edgeSupplier: Supplier<E>? = null,
        initialVertexEdgeMap: Map<V, E> = Collections.emptyMap(),
        private val allowSelfEdges: Boolean = false
) {

    /**
     * Maps the ID of a connected vertex -> the edges that connect it
     */
    private val outgoing = HashMap(initialVertexEdgeMap)

    val vertexEdgeMap: Map<V, E> = outgoing
    val edges: Collection<E> = outgoing.values
    val vertices: Set<V> = outgoing.keys

    fun dfsExclusive(): Iterator<V> = DepthFirstIteratorExclusive(owner)
    fun dfsInclusive(): Iterator<V> = DepthFirstIteratorInclusive(owner)

    fun disconnectAll() {
        this.vertices.forEach { disconnect(it) }
    }

    fun connect(target: V) {
        if (edgeSupplier == null)
            throw UnsupportedOperationException(
                    "No edge supplier provided, use connect(target, edge) instead")

        connect(target, edgeSupplier.get())
    }

    fun connect(target: V, edge: E) {
        if (target == owner && !allowSelfEdges) {
            throw IllegalArgumentException("Edges to self are not permitted")
        }
        outgoing[target] = edge
        target.direct.outgoing[owner] = edge
    }

    fun disconnect(target: V): E? {
        val edge = outgoing[target]
        outgoing -= target
        target.direct.outgoing -= owner
        return edge
    }

}