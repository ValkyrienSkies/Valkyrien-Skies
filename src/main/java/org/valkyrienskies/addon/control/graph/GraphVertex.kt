package org.valkyrienskies.addon.control.graph


/**
 * This represents a vertex in an undirected graph
 *
 * Type parameters
 *
 * - **VERTEX** - The type of the class that is implementing this vertex
 * - **EDGE** - The type of the edge. Use 'EmptyEdge' if you don't store data in the edge
 *
 * The ID MUST have a proper equals() and hashCode() method
 *
 * ```
 * class MyClass : GraphVertex<MyClass, EmptyEdge> {
 *      override val direct = VertexEdgeCollection(this)
 * }
 *
 * interface EmptyEdge {}
 * ```
 */
interface GraphVertex<VERTEX : GraphVertex<VERTEX, EDGE>, EDGE> {
    /**
     * The edges connecting directly to this vertex
     */
    val direct: VertexEdgeCollection<VERTEX, EDGE>

    /**
     * Returns an iterator over every connected point (inclusive of this one)
     */
    fun dfsInclusive() = direct.dfsInclusive()
    /**
     * Returns an iterable over every connected point (inclusive of this one)
     */
    fun dfsInclusiveIterable(): Iterable<VERTEX> = Iterable(direct::dfsInclusive)
    /**
     * Returns an iterator over every other connected point (exclusive)
     */
    fun dfs() = direct.dfsExclusive()
    /**
     * Returns an iterable over every other connected point (exclusive)
     */
    fun dfsIterable(): Iterable<VERTEX> = Iterable(direct::dfsExclusive)
    /**
     * Get the edge to a specified vertex
     */
    fun edgeTo(other: VERTEX): EDGE? = direct.vertexEdgeMap[other]
    fun isConnected(other: GraphVertex<*, *>): Boolean = direct.vertices.contains(other)
    fun disconnect(other: VERTEX): EDGE? = direct.disconnect(other)
    fun connect(other: VERTEX, edge: EDGE) = direct.connect(other, edge)
    fun connect(other: VERTEX) = direct.connect(other)
    fun disconnectAll() = direct.disconnectAll()
}
