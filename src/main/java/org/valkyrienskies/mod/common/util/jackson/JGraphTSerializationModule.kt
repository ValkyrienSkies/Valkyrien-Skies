package org.valkyrienskies.mod.common.util.jackson

import com.fasterxml.jackson.databind.module.SimpleModule
import org.jgrapht.Graph
import org.jgrapht.graph.DefaultUndirectedGraph
import org.valkyrienskies.mod.common.util.jackson.jgrapht.GraphSerialization

class JGraphTSerializationModule : SimpleModule() {
    init {
        super.addSerializer(Graph::class.java, GraphSerialization.Serializer())
        super.addDeserializer(DefaultUndirectedGraph::class.java, GraphSerialization.DefaultUndirectedGraphDeserializer())
    }
}