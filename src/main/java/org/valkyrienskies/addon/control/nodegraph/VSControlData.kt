package org.valkyrienskies.addon.control.nodegraph

import com.fasterxml.jackson.annotation.JsonCreator
import com.fasterxml.jackson.annotation.JsonIgnore
import com.fasterxml.jackson.annotation.JsonProperty
import com.fasterxml.jackson.databind.annotation.JsonDeserialize
import org.jgrapht.Graph
import org.jgrapht.graph.DefaultEdge
import org.jgrapht.graph.DefaultUndirectedGraph

class VSControlData @JsonCreator constructor (
        @JsonProperty("nodeGraph")
        @JsonDeserialize(
                `as` = DefaultUndirectedGraph::class,
                keyAs = NetworkNode::class,
                contentAs = DefaultEdge::class)
        val nodeGraph: Graph<NetworkNode, DefaultEdge> = DefaultUndirectedGraph(DefaultEdge::class.java)
) {

    @JsonIgnore
    val nodeNetwork: WorldNodeNetwork = WorldNodeNetwork(this)

}