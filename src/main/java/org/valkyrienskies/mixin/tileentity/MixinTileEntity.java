package org.valkyrienskies.mixin.tileentity;

import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import org.joml.Vector3d;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;
import org.spongepowered.asm.mixin.Shadow;
import org.valkyrienskies.mod.common.ships.ship_world.PhysicsObject;
import org.valkyrienskies.mod.common.util.ValkyrienUtils;
import valkyrienwarfare.api.TransformType;

import java.util.Optional;

/**
 * Necessary to allow for rendering and for players to interact with tiles (ex. chests).
 */
@Mixin(TileEntity.class)
public abstract class MixinTileEntity {

    @Shadow
    protected BlockPos pos;

    @Shadow
    protected World world;

    /**
     * This is easier to have as an overwrite because there's less laggy hackery to be done then :P
     *
     * @author DaPorkchop_
     */
    @Overwrite
    public double getDistanceSq(double x, double y, double z) {
        World tileWorld = this.world;
        double d0 = (double) this.pos.getX() + 0.5D - x;
        double d1 = (double) this.pos.getY() + 0.5D - y;
        double d2 = (double) this.pos.getZ() + 0.5D - z;
        double toReturn = d0 * d0 + d1 * d1 + d2 * d2;

        if (tileWorld != null) {
            //Assume on Ship
            if (tileWorld.isRemote && toReturn > 9999999D) {
                BlockPos pos = this.pos;
                Optional<PhysicsObject> physicsObject = ValkyrienUtils.getPhysoManagingBlock(world, pos);

                if (physicsObject.isPresent()) {
                    Vector3d tilePos = new Vector3d(pos.getX() + .5, pos.getY() + .5, pos.getZ() + .5);
                    physicsObject.get()
                        .getShipTransformationManager().getCurrentTickTransform().transformPosition(tilePos, TransformType.SUBSPACE_TO_GLOBAL);

                    tilePos.x -= x;
                    tilePos.y -= y;
                    tilePos.z -= z;

                    return tilePos.lengthSquared();
                }
            }
        }
        return toReturn;
    }
}
