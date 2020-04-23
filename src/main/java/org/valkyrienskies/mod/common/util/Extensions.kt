package org.valkyrienskies.mod.common.util

import net.minecraft.nbt.NBTTagCompound
import net.minecraft.util.math.BlockPos
import net.minecraft.world.World

fun NBTTagCompound.setBlockPos(key: String, pos: BlockPos) {
    this.setIntArray(key, intArrayOf(pos.x, pos.y, pos.z))
}

fun NBTTagCompound.getBlockPos(key: String): BlockPos {
    val (x, y, z) = this.getIntArray(key)
    return BlockPos(x, y, z)
}

/**
 * @see [World.setBlockState]
 */
object WorldFlags {
    val BLOCK_UPDATE = 1
    val SEND_TO_CLIENT = 2
    val NO_RERENDER = 4
    val FORCE_MAIN_THREAD_RERENDER = 8
    val NO_OBSERVER = 16
}