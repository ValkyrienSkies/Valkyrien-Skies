package org.valkyrienskies.addon.control.tileentity

import net.minecraft.util.math.AxisAlignedBB
import org.valkyrienskies.addon.control.tileentity.behaviour.NodeTEBehaviour
import org.valkyrienskies.mod.common.tileentity.behaviour.BehaviourControlledTileEntity

class TileEntityNetworkRelay :
        BehaviourControlledTileEntity(
                NodeTEBehaviour.Factory
        ) {

    // TODO: Not the best solution, but it works for now.
    override fun getRenderBoundingBox(): AxisAlignedBB {
        return INFINITE_EXTENT_AABB
    }

}