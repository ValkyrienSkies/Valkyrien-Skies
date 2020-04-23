package org.valkyrienskies.mod.common.tileentity.behaviour

import com.google.common.collect.MutableClassToInstanceMap
import net.minecraft.nbt.NBTTagCompound
import net.minecraft.network.NetworkManager
import net.minecraft.network.play.server.SPacketUpdateTileEntity
import net.minecraft.tileentity.TileEntity
import kotlin.reflect.KClass

open class BehaviourControlledTileEntity(
        vararg behaviourFactories: TileEntityBehaviourFactory<*>
) : TileEntity() {

    private val behavioursMap = MutableClassToInstanceMap.create<TileEntityBehaviour>()
    private val behaviours = behavioursMap.values

    init {
        behaviourFactories.forEach { this.behavioursMap[it.clazz] = it.createBehaviour(this) }
    }

    fun <B : TileEntityBehaviour> getBehaviour(behaviour: Class<B>): B? =
            behavioursMap.getInstance(behaviour)

    fun <B : TileEntityBehaviour> getBehaviour(behaviour: KClass<B>): B? =
            getBehaviour(behaviour.java)

    override fun readFromNBT(compound: NBTTagCompound) {
        behaviours.forEach { it.readFromNBT(compound.getCompoundTag(it.serialUID)) }
        super.readFromNBT(compound)
    }

    override fun writeToNBT(compound: NBTTagCompound): NBTTagCompound {
        behaviours.forEach { compound.setTag(it.serialUID, it.writeToNBT() ?: return@forEach) }
        return super.writeToNBT(compound)
    }

    override fun getUpdateTag(): NBTTagCompound {
        val compound = NBTTagCompound()
        behaviours.forEach { compound.setTag(it.serialUID, it.getUpdateTag() ?: return@forEach) }
        return super.getUpdateTag()
    }

    override fun handleUpdateTag(tag: NBTTagCompound) {
        behaviours.forEach { it.handleUpdateTag(tag.getCompoundTag(it.serialUID)) }
        super.handleUpdateTag(tag)
    }

    override fun onDataPacket(net: NetworkManager, pkt: SPacketUpdateTileEntity) {
        behaviours.forEach { it.onDataPacket(pkt.nbtCompound.getCompoundTag(it.serialUID)) }
        super.onDataPacket(net, pkt)
    }

    override fun getUpdatePacket(): SPacketUpdateTileEntity? {
        val compound = NBTTagCompound()
        behaviours.forEach { compound.setTag(it.serialUID, it.getUpdatePacket() ?: return@forEach) }

        return if (compound.isEmpty) null
        else SPacketUpdateTileEntity(getPos(), 0, compound)
    }

    override fun validate() {
        behaviours.forEach { it.onValidate() }
        super.validate()
    }

    override fun invalidate() {
        behaviours.forEach { it.onInvalidate() }
        super.invalidate()
    }


}