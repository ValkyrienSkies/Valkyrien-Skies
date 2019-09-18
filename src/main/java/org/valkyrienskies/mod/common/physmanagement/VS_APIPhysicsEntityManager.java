package org.valkyrienskies.mod.common.physmanagement;

import javax.annotation.Nullable;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraft.world.chunk.Chunk;
import org.valkyrienskies.fixes.IPhysicsChunk;
import valkyrienwarfare.api.IPhysicsEntity;
import valkyrienwarfare.api.IPhysicsEntityManager;

public class VS_APIPhysicsEntityManager implements IPhysicsEntityManager {

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
