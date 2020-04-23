package org.valkyrienskies.addon.control.tileentity

import org.valkyrienskies.addon.control.tileentity.behaviour.NodeTEBehaviour
import org.valkyrienskies.mod.common.tileentity.behaviour.BehaviourControlledTileEntity

class TileEntityNetworkDisplay : BehaviourControlledTileEntity(NodeTEBehaviour.Factory)