package org.valkyrienskies.mod.common.ships.ship_world;

import net.minecraft.util.math.RayTraceResult;
import net.minecraft.util.math.Vec3d;

public interface IWorldVS {

    /**
     * Makes a physics wrapper entity be ignored by the {@link net.minecraft.world.World#rayTraceBlocks(Vec3d,
     * Vec3d, boolean, boolean, boolean)} method (and overloads thereof).
     * <p>
     * This has no effect on the behavior of {@link #rayTraceBlocksIgnoreShip(Vec3d, Vec3d, boolean,
     * boolean, boolean, PhysicsObject)}.
     * <p>
     * Must be followed later by a call to {@link #unexcludeShipFromRayTracer(PhysicsObject)}
     * with the same {@link PhysicsObject} instance as a parameter.
     *
     * @param entity the {@link PhysicsObject} to exclude from ray tracing
     */
    void excludeShipFromRayTracer(PhysicsObject entity);

    void unexcludeShipFromRayTracer(PhysicsObject entity);

    RayTraceResult rayTraceBlocksIgnoreShip(Vec3d vec31, Vec3d vec32, boolean stopOnLiquid,
                                            boolean ignoreBlockWithoutBoundingBox, boolean returnLastUncollidableBlock,
                                            PhysicsObject toIgnore);

    RayTraceResult rayTraceBlocksInShip(Vec3d vec31, Vec3d vec32, boolean stopOnLiquid,
                                            boolean ignoreBlockWithoutBoundingBox, boolean returnLastUncollidableBlock,
                                            PhysicsObject toUse);
}
