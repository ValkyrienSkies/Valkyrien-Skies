package org.valkyrienskies.mod.common.physmanagement;

import javax.annotation.Nullable;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import org.valkyrienskies.mod.common.util.ValkyrienUtils;
import valkyrienwarfare.api.IPhysicsEntity;
import valkyrienwarfare.api.IPhysicsEntityManager;

/**
 * @deprecated This class is basically useless
 */
@Deprecated
public class VS_APIPhysicsEntityManager implements IPhysicsEntityManager {

    @Nullable
    @Override
    public IPhysicsEntity getPhysicsEntityFromShipSpace(World world, BlockPos pos) {
        return ValkyrienUtils.getPhysoManagingBlock(world, pos).orElse(null);
    }

}
