package org.valkyrienskies.addon.control.graph

import com.google.common.collect.Iterators

fun <V : GraphVertex<V, E>, E> DepthFirstIteratorInclusive(start: V) : Iterator<V> =
        Iterators.concat(DepthFirstIteratorExclusive(start), Iterators.singletonIterator(start))