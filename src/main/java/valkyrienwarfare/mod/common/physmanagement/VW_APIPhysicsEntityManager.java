package valkyrienwarfare.mod.common.physmanagement;

import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraft.world.chunk.Chunk;
import valkyrienwarfare.api.IPhysicsEntity;
import valkyrienwarfare.api.IPhysicsEntityManager;
import valkyrienwarfare.fixes.IPhysicsChunk;

import javax.annotation.Nullable;

public class VW_APIPhysicsEntityManager implements IPhysicsEntityManager {

    @Nullable
    @Override
    public IPhysicsEntity getPhysicsEntityFromShipSpace(World world, BlockPos pos) {
        Chunk chunk = world.getChunk(pos);
        IPhysicsChunk physicsChunk = (IPhysicsChunk) chunk;
        if (physicsChunk.getPhysicsObjectOptional()
                .isPresent()) {
            return physicsChunk.getPhysicsObjectOptional()
                    .get();
        } else {
            return null;
        }
    }

}
