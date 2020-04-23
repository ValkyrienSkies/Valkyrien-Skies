package org.valkyrienskies.mod.common.tileentity.behaviour

import net.minecraft.tileentity.TileEntity

interface TileEntityBehaviourFactory<T : TileEntityBehaviour> {

    fun createBehaviour(owner: TileEntity): T
    val clazz: Class<T>

}