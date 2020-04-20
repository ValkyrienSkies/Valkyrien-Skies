package org.valkyrienskies.addon.control.nodegraph

import net.minecraft.util.math.BlockPos
import org.jgrapht.Graph
import org.jgrapht.graph.DefaultEdge
import java.lang.IllegalStateException

class WorldNodeNetwork(data: VSControlData) {

    private val _map: MutableMap<BlockPos, NetworkNode> = HashMap()

    // Expose a non-mutable version of the map to the world
    val map: Map<BlockPos, NetworkNode> = _map

    val graph: NodeNetworkGraph = NodeNetworkGraph(data.nodeGraph, _map)

    val nodes: Set<NetworkNode> = graph.vertexSet()

    // A Graph<NetworkNode, DefaultEdge> that maintains a map BlockPos -> NetworkNode
    class NodeNetworkGraph(
            val backing: Graph<NetworkNode, DefaultEdge>,
            val map: MutableMap<BlockPos, NetworkNode>
    ) : Graph<NetworkNode, DefaultEdge> by backing {

        fun canConnect(v1: NetworkNode, v2: NetworkNode): Boolean {
            return outgoingEdgesOf(v1).size < v1.maxConnections &&
                    outgoingEdgesOf(v2).size < v2.maxConnections
        }

        override fun addEdge(v1: NetworkNode, v2: NetworkNode): DefaultEdge {
           if (!canConnect(v1, v2))
               throw IllegalStateException("That node is at its maximum connections already!")

            return backing.addEdge(v1, v2)
        }

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