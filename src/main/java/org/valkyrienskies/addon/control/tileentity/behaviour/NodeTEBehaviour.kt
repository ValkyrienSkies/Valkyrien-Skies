package org.valkyrienskies.addon.control.tileentity.behaviour

import net.minecraft.nbt.NBTTagCompound
import net.minecraft.nbt.NBTTagList
import net.minecraft.tileentity.TileEntity
import net.minecraftforge.common.util.Constants
import org.valkyrienskies.addon.control.nodenetwork.VSNode
import org.valkyrienskies.addon.control.nodenetwork.WireType
import org.valkyrienskies.mod.common.tileentity.behaviour.BehaviourFactoryFactory
import org.valkyrienskies.mod.common.tileentity.behaviour.TEBehaviours
import org.valkyrienskies.mod.common.tileentity.behaviour.TileEntityBehaviour
import org.valkyrienskies.mod.common.util.getBlockPos
import org.valkyrienskies.mod.common.util.setBlockPos

/**
 * A behaviour that enables a Tile Entity to become a node
 * that can be a part of the node networks in VS Control
 *
 * It essentially just stores the 'node'
 */
class NodeTEBehaviour(owner: TileEntity) : TileEntityBehaviour(owner) {

    val node: VSNode = VSNode(owner)

    override fun getUpdatePacket(): NBTTagCompound? =
            writeToNBT()
    override fun onDataPacket(compound: NBTTagCompound) =
            readFromNBT(compound)
    override fun getUpdateTag(): NBTTagCompound? =
            writeToNBT()
    override fun handleUpdateTag(tag: NBTTagCompound) = readFromNBT(tag)

    override fun onInvalidate() {
        this.node.disconnectAll()
    }

    override fun writeToNBT(): NBTTagCompound? {
        val compound = NBTTagCompound()
        val connected = NBTTagList()
        node.direct.vertexEdgeMap.forEach { (vertex, edge) ->
            val tag = NBTTagCompound()
            tag.setBlockPos("pos", vertex.pos)
            tag.setString("wireType", edge.name)
            connected.appendTag(tag)
        }
        compound.setTag("connected", connected)
        compound.setInteger("maxConnections", node.maxConnections)
        return compound
    }

    @Suppress("UNCHECKED_CAST")
    override fun readFromNBT(compound: NBTTagCompound) {
        // reset!
        this.node.disconnectAll()

        val connected = compound.getTagList("connected", Constants.NBT.TAG_COMPOUND) as Iterable<NBTTagCompound>
        connected.forEach { tag ->
            val targetPos = tag.getBlockPos("pos")
            val wireType = WireType.valueOf(tag.getString("wireType"))

            val target = owner.world.getTileEntity(targetPos)
            val otherNode = TEBehaviours.getBehaviour(target, NodeTEBehaviour::class)?.node ?: return@forEach
            node.connect(otherNode, wireType)
        }
        node.maxConnections = compound.getInteger("maxConnections")
    }

    companion object {
        @JvmStatic
        val Factory = BehaviourFactoryFactory.create(NodeTEBehaviour::class.java)
    }

}



