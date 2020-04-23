package org.valkyrienskies.addon.control.graph

import java.util.function.Supplier

/**
 * Supplies null for everything.
 * Useful for edges that store no information
 */
class NullEdgeSupplier : Supplier<Any?> {
    override fun get(): Any? = null
}