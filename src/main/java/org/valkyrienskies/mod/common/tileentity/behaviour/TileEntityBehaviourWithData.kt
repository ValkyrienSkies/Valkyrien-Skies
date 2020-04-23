package org.valkyrienskies.mod.common.tileentity.behaviour

import net.minecraft.nbt.NBTTagCompound
import net.minecraft.tileentity.TileEntity

open class TileEntityBehaviourWithData<DATA>(
        override val owner: TileEntity,
        /**
         * Starting/empty data
         */
        data: DATA,
        dataClass: Class<DATA>
) : TileEntityBehaviour(owner) {

    private val container = NBTSerializableDataContainer(data, dataClass)
    val data: DATA
        get() = container.data

    override fun readFromNBT(compound: NBTTagCompound) {
        container.readFromNBT(compound)
    }
    override fun writeToNBT(): NBTTagCompound? {
        return container.writeToNBT(NBTTagCompound())
    }

}