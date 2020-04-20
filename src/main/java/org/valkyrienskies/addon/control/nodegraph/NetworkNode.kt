package org.valkyrienskies.addon.control.nodegraph

import net.minecraft.util.math.BlockPos

/**
 * The NetworkNode is linked to a tile entity inside of any 'node network' created by the relay wires
 */
data class NetworkNode(
        val pos: BlockPos,
        var maxConnections: Int = 2
)