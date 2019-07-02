package valkyrienwarfare.mod.common.util;

import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import valkyrienwarfare.fixes.IPhysicsChunk;
import valkyrienwarfare.mod.common.physics.management.PhysicsObject;

import java.util.Optional;

public class ValkyrienUtils {

    /**
     * The liver of this mod. Returns the PhysicsObject that managed the given pos in the given world.
     *
     * @param world
     * @param pos
     * @return
     */
    public static Optional<PhysicsObject> getPhysicsObject(World world, BlockPos pos) {
        // No physics object manages a null world or a null pos.
        if (world != null && pos != null && world.isBlockLoaded(pos)) {
            IPhysicsChunk physicsChunk = (IPhysicsChunk) world.getChunk(pos);
            Optional<PhysicsObject> physicsObject = physicsChunk.getPhysicsObjectOptional();
            if (physicsObject.isPresent()) {
                if (physicsObject.get()
                        .getShipTransformationManager() != null) {
                    return physicsObject;
                } else {
                    new IllegalStateException("Tried accessing ship at " + pos + " but it wasn't fully initialized").printStackTrace();
                }
            }
        }
        return Optional.empty();
    }

}
