package org.valkyrienskies.mod.fixes;

import net.minecraft.client.Minecraft;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.RayTraceResult;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;
import org.joml.Vector3d;
import org.valkyrienskies.mod.common.config.VSConfig;
import org.valkyrienskies.mod.common.ships.ship_world.IWorldVS;
import org.valkyrienskies.mod.common.ships.ship_world.PhysicsObject;
import org.valkyrienskies.mod.common.util.JOML;
import org.valkyrienskies.mod.common.util.ValkyrienUtils;

import java.util.List;

/**
 * Why not just inline this code into MixinClientWorld? There's a strange mixin bug that causes MixinClientWorld to get
 * loaded on servers. However by using World.isRemote we can prevent this code from ever being loaded on server side
 * therefore preventing an incorrect side crash.
 */
public class FixAccurateRain {

    public static BlockPos getRainPosFromShips(final World world, final BlockPos originalHeight) {
        if (VSConfig.accurateRain && Minecraft.getMinecraft().player != null) {
            final AxisAlignedBB boundingBox = new AxisAlignedBB(originalHeight.getX() - .5, 0, originalHeight.getZ() - .5, originalHeight.getX() + .5, 255, originalHeight.getZ() + .5);
            final List<PhysicsObject> physicsObjectList = ValkyrienUtils.getPhysObjWorld(world).getPhysObjectsInAABB(boundingBox);

            final Vec3d traceStart = new Vec3d(originalHeight.getX() + .5, Minecraft.getMinecraft().player.posY + 50, originalHeight.getZ() + .5);
            final Vec3d traceEnd = new Vec3d(originalHeight.getX() + .5, originalHeight.getY() + .5, originalHeight.getZ() + .5);

            if (traceStart.y < traceEnd.y) {
                return originalHeight;
            }

            for (final PhysicsObject physicsObject : physicsObjectList) {
                final RayTraceResult result = ((IWorldVS) world).rayTraceBlocksInShip(traceStart, traceEnd, true, true, false, physicsObject);

                //noinspection ConstantConditions
                if (result != null && result.getBlockPos() != null && result.typeOfHit != RayTraceResult.Type.MISS) {
                    Vector3d blockPosVector = JOML.convertDouble(result.getBlockPos())
                            .add(.5, .5, .5);

                    physicsObject
                            .getShipTransformationManager()
                            .getCurrentTickTransform()
                            .getSubspaceToGlobal()
                            .transformPosition(blockPosVector);

                    return new BlockPos(originalHeight.getX(), blockPosVector.y(), originalHeight.getZ());
                }
            }
        }
        return originalHeight;
    }
}
