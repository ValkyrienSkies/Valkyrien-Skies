package org.valkyrienskies.addon.control.nodenetwork

import net.minecraft.tileentity.TileEntity
import net.minecraft.util.math.BlockPos
import org.valkyrienskies.addon.control.graph.GraphVertex
import org.valkyrienskies.addon.control.graph.VertexEdgeCollection
import org.valkyrienskies.mod.common.util.WorldFlags
import java.util.function.Supplier

class VSNode(
        val owner: TileEntity,
        var maxConnections: Int = 2
) : GraphVertex<VSNode, WireType> {

    override val direct = VertexEdgeCollection(this, Supplier { WireType.RELAY })

    val pos: BlockPos get() = owner.pos

    override fun connect(other: VSNode, edge: WireType) {
        markDirtyAndUpdate()
        other.markDirtyAndUpdate()
        return super.connect(other, edge)
    }

    override fun connect(other: VSNode) {
        markDirtyAndUpdate()
        other.markDirtyAndUpdate()
        return super.connect(other)
    }

    override fun disconnect(other: VSNode): WireType? {
        markDirtyAndUpdate()
        other.markDirtyAndUpdate()
        return super.disconnect(other)
    }

    override fun disconnectAll() {
        markDirtyAndUpdate()
        direct.vertices.forEach { it.markDirtyAndUpdate() }
        super.disconnectAll()
    }

    fun canConnect() = direct.edges.size < maxConnections

    private fun markDirtyAndUpdate() {
        owner.markDirty()
        val state = owner.world.getBlockState(owner.pos)
        owner.world.notifyBlockUpdate(owner.pos, state, state, WorldFlags.SEND_TO_CLIENT)
    }
}