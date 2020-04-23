package org.valkyrienskies.addon.control.graph

import io.kotest.core.spec.style.StringSpec
import io.kotest.matchers.collections.shouldContainExactlyInAnyOrder
import io.kotest.property.arbitrary.arb
import io.kotest.property.forAll
import kotlin.random.Random

class DepthFirstIteratorExclusiveSpec : StringSpec({

    class Vertex : GraphVertex<Vertex, Any?> {
        override val direct = VertexEdgeCollection(this, NullEdgeSupplier(), allowSelfEdges = true)
        val id = Random.nextInt()
        override fun toString(): String = id.toString()
    }

    // Generates a random graph with edges between every node
    // and then some
    val randomGraph = arb { rs ->
        generateSequence {
            val r = rs.random
            val numVertices = r.nextInt(1, 150)
            val numEdges = r.nextInt(1, 800)
            val vertices = List(numVertices) { Vertex() }
            for (x in 0 until numEdges) {
                val v1 = vertices[Random.nextInt(vertices.size)]
                val v2 = vertices[Random.nextInt(vertices.size)]
                v1.connect(v2)
            }
            var last = vertices[0]
            for (x in 1 until numVertices) {
                last.connect(vertices[x])
                last = vertices[x]
            }
            vertices
        }
    }

    "should only iterate each node once" {
        forAll(500, randomGraph) { g ->
            val touched = ArrayList<Vertex>()
            for (x in g[0].dfsIterable()) {
                touched += x
            }
            touched shouldContainExactlyInAnyOrder (g - g[0])
            true
        }
    }

})