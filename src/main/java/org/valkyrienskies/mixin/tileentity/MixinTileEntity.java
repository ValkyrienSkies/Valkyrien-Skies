package org.valkyrienskies.mixin.tileentity;

import java.util.Optional;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;
import org.spongepowered.asm.mixin.Shadow;
import org.valkyrienskies.mod.common.math.Vector;
import org.valkyrienskies.mod.common.physics.management.physo.PhysicsObject;
import org.valkyrienskies.mod.common.util.ValkyrienUtils;

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
                    Vector tilePos = new Vector(pos.getX() + .5D, pos.getY() + .5D,
                        pos.getZ() + .5D);
                    physicsObject.get()
                        .getShipTransformationManager()
                        .fromLocalToGlobal(tilePos);

                    tilePos.x -= x;
                    tilePos.y -= y;
                    tilePos.z -= z;

                    return tilePos.lengthSq();
                }
            }
        }
        return toReturn;
    }
}
