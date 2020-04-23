package org.valkyrienskies.mod.common.tileentity.behaviour

import net.minecraft.tileentity.TileEntity
import kotlin.contracts.ExperimentalContracts
import kotlin.contracts.contract
import kotlin.reflect.KClass

object TEBehaviours {

    fun <T: TileEntityBehaviour> getBehaviour(te: TileEntity?, behaviour: KClass<T>): T? =
            getBehaviour(te, behaviour.java)

    fun <T: TileEntityBehaviour> getBehaviour(te: TileEntity?, behaviour: Class<T>): T? {
        return if (te is BehaviourControlledTileEntity) te.getBehaviour(behaviour) else null
    }
}

/**
 * Check whether or not a tile entity has a particular behaviour
 */
@ExperimentalContracts
fun <T: TileEntityBehaviour> TEBehaviours.hasBehaviour(te: TileEntity?, behaviour: Class<T>): Boolean {
    contract {
        returns(true) implies (te is BehaviourControlledTileEntity)
    }
    return getBehaviour(te, behaviour) != null
}