package org.valkyrienskies.addon.control.nodegraph

import net.minecraft.util.math.BlockPos
import org.jgrapht.Graph
import org.jgrapht.graph.DefaultEdge

class WorldNodeNetwork(data: VSControlData) {

    private val _map: MutableMap<BlockPos, NetworkNode> = HashMap()

    // Expose a non-mutable version of the map to the world
    val map: Map<BlockPos, NetworkNode> = _map

    val graph: Graph<NetworkNode, DefaultEdge> = CustomUndirectedGraph(data.nodeGraph, _map)

    val nodes: Set<NetworkNode> = graph.vertexSet()

    // A Graph<NetworkNode, DefaultEdge> that maintains a map BlockPos -> NetworkNode
    private class CustomUndirectedGraph(
            val backing: Graph<NetworkNode, DefaultEdge>,
            val map: MutableMap<BlockPos, NetworkNode>
    ) : Graph<NetworkNode, DefaultEdge> by backing {

        override fun addVertex(v: NetworkNode): Boolean {
            map[v.pos] = v

            return backing.addVertex(v)
        }

        override fun removeVertex(v: NetworkNode): Boolean {
            map.remove(v.pos)

            return backing.removeVertex(v)
        }
    }
}