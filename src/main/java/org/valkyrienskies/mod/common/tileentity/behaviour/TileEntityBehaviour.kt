package org.valkyrienskies.mod.common.tileentity.behaviour

import net.minecraft.nbt.NBTTagCompound
import net.minecraft.tileentity.TileEntity

/**
 * @see org.valkyrienskies.mod.common.tileentity.behaviour
 */
abstract class TileEntityBehaviour(
        protected open val owner: TileEntity,
        val serialUID: String = owner::class.qualifiedName!!
) {
    open fun writeToNBT(): NBTTagCompound? = null
    open fun readFromNBT(compound: NBTTagCompound) {}
    open fun getUpdateTag(): NBTTagCompound? = null
    open fun handleUpdateTag(tag: NBTTagCompound) {}
    open fun getUpdatePacket(): NBTTagCompound? = null
    open fun onDataPacket(compound: NBTTagCompound) {}
    open fun onInvalidate() {}
    open fun onValidate() {}

}