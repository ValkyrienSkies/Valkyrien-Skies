package org.valkyrienskies.mod.common.util.jackson

import com.fasterxml.jackson.annotation.JsonAutoDetect
import com.fasterxml.jackson.annotation.JsonCreator
import com.fasterxml.jackson.annotation.JsonProperty
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.databind.annotation.JsonDeserialize
import org.jgrapht.Graph
import org.jgrapht.graph.DefaultEdge
import org.jgrapht.graph.DefaultUndirectedGraph
import org.jgrapht.nio.dot.DOTExporter
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.BeforeAll
import org.junit.jupiter.api.Test
import java.io.StringWriter

class GraphSerializationTest {

    data class Person @JsonCreator constructor (
            @JsonProperty("name") var name: String
    )

    class SerializeMe {
        @JsonDeserialize(
                `as` = DefaultUndirectedGraph::class,
                keyAs = Person::class,
                contentAs = DefaultEdge::class
        )
        val graph = DefaultUndirectedGraph<Person, DefaultEdge>(DefaultEdge::class.java)
    }

    @Test
    fun testSerializeGraph() {
        val data = SerializeMe()
        val g1 = data.graph;

        val bob = Person("Bob")
        val doe = Person("Doe")
        val jane = Person("Jane")
        val bill = Person("Bill")

        g1.addVertex(bob)
        g1.addVertex(doe)
        g1.addVertex(jane)
        g1.addVertex(bill)

        g1.addEdge(bob, doe)
        g1.addEdge(jane, bob)
        g1.addEdge(jane, bill)
        g1.addEdge(bob, bill)

        val bytes = mapper.writeValueAsBytes(data)
        val data2 = mapper.readValue(bytes, SerializeMe::class.java)

        val g2 = data2.graph

        // DefaultEdge only has referential equality, so we can't actually use g1.equals(g2)
        assertEquals(export(g1), export(g2))
        assertEquals(g1.vertexSet(), g2.vertexSet())
    }

    fun <V, E> export(g: Graph<V, E>): String {
        val exporter = DOTExporter<V, E>()
        val w = StringWriter()
        exporter.exportGraph(g, w)
        return w.buffer.toString()
    }

    companion object {
        lateinit var mapper: ObjectMapper

        @BeforeAll
        @JvmStatic
        fun setupMapper() {
            mapper = ObjectMapper()
            mapper.registerModule(JGraphTSerializationModule())
            mapper.setVisibility(mapper.visibilityChecker
                    .withFieldVisibility(JsonAutoDetect.Visibility.ANY)
                    .withGetterVisibility(JsonAutoDetect.Visibility.NONE)
                    .withIsGetterVisibility(JsonAutoDetect.Visibility.NONE)
                    .withSetterVisibility(JsonAutoDetect.Visibility.NONE))
        }
    }
}
