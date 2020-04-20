package org.valkyrienskies.mod.common.util.jackson.jgrapht

import com.fasterxml.jackson.core.JsonGenerator
import com.fasterxml.jackson.core.JsonParser
import com.fasterxml.jackson.databind.*
import com.fasterxml.jackson.databind.annotation.JsonDeserialize
import com.fasterxml.jackson.databind.deser.ContextualDeserializer
import com.fasterxml.jackson.databind.deser.std.StdDeserializer
import com.fasterxml.jackson.databind.ser.std.StdSerializer
import org.jgrapht.Graph
import org.jgrapht.graph.DefaultEdge
import org.jgrapht.graph.DefaultUndirectedGraph
import org.jgrapht.nio.IntegerIdProvider
import java.io.IOException

/**
 * This class serializes and deserializes JGraphT objects
 */
class GraphSerialization {
    // Serializes any Graph
    class Serializer(vc: Class<Graph<*, *>>? = null) : StdSerializer<Graph<*, *>>(vc) {

        @Suppress("UNCHECKED_CAST")
        override fun serialize(_graph: Graph<*, *>, gen: JsonGenerator, provider: SerializerProvider) {
            val graph = _graph as Graph<Any, Any>
            val vertexIdProvider = IntegerIdProvider<Any>()
            gen.writeStartObject()
            gen.writeArrayFieldStart("vertices")
            for (v in graph.vertexSet()) {
                gen.writeStartObject()

                gen.writeStringField("id", vertexIdProvider.apply(v))
                gen.writeFieldName("data")
                gen.codec.writeValue(gen, v)

                gen.writeEndObject()
            }
            gen.writeEndArray()

            gen.writeArrayFieldStart("edges")
            for (e in graph.edgeSet()) {
                gen.writeStartObject()

                val source = vertexIdProvider.apply(graph.getEdgeSource(e))
                val target = vertexIdProvider.apply(graph.getEdgeTarget(e))

                gen.writeStringField("source", source)
                gen.writeStringField("target", target)

                if (e::class != DefaultEdge::class) {
                    gen.writeFieldName("data")
                    gen.codec.writeValue(gen, e)
                }

                gen.writeEndObject()
            }
            gen.writeEndArray()
            gen.writeEndObject()
        }

    }

    class DefaultUndirectedGraphDeserializer :
            StdDeserializer<DefaultUndirectedGraph<Any, Any>>(DefaultUndirectedGraph::class.java),
            ContextualDeserializer {

        lateinit var vertexAs: Class<*>
        lateinit var edgeAs: Class<*>

        override fun deserialize(p: JsonParser, ctxt: DeserializationContext): DefaultUndirectedGraph<Any, Any> {
            return Deserializer<Any, Any, DefaultUndirectedGraph<Any, Any>>(
                    DefaultUndirectedGraph(edgeAs), vertexAs, edgeAs).deserialize(p, ctxt);
        }

        override fun createContextual(ctxt: DeserializationContext, property: BeanProperty): JsonDeserializer<*> {
            val jsonDeserialize = property.getAnnotation(JsonDeserialize::class.java)
            this.vertexAs = jsonDeserialize.keyAs.java
            this.edgeAs = jsonDeserialize.contentAs.java
            return this
        }

    }

    // Deserializes to a provided graph
    private class Deserializer<V, E, G : Graph<V, E>>(
            val graph: G,
            val vertexAs: Class<*>,
            val edgeAs: Class<*>
    ) {

        fun deserialize(p: JsonParser, ctxt: DeserializationContext): G {
            val root: JsonNode = p.codec.readTree(p)
            val vertices = root.get("vertices")

            val idMap = HashMap<String, V>()

            assert(vertices.isArray)

            for (vertex in vertices) {
                val data = vertex["data"].traverse(p.codec).readValueAs(vertexAs) as V
                graph.addVertex(data)
                idMap[vertex["id"].asText()] = data
            }

            val edges = root.get("edges")

            assert(edges.isArray)

            for (edge in edges) {
                val source = idMap[edge["source"].asText()]
                val target = idMap[edge["target"].asText()]

                if (source == null || target == null) {
                    throw IOException("Invalid edge ID")
                }

                if (edgeAs == DefaultEdge::class.java) {
                    graph.addEdge(source, target)
                } else {
                    val data = edge["data"].traverse(p.codec).readValueAs(edgeAs) as E
                    graph.addEdge(source, target, data)
                }
            }

            return graph
        }

    }
}