package ValkyrienWarfareBase.Mixin.tileentity;

import ValkyrienWarfareBase.API.Vector;
import ValkyrienWarfareBase.PhysicsManagement.PhysicsWrapperEntity;
import ValkyrienWarfareBase.ValkyrienWarfareMod;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;
import org.spongepowered.asm.mixin.Shadow;

@Mixin(TileEntity.class)
public abstract class MixinTileEntity implements net.minecraftforge.common.capabilities.ICapabilitySerializable<NBTTagCompound> {
    @Shadow
    protected BlockPos pos;

    @Shadow
    protected World world;

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
                PhysicsWrapperEntity wrapper = ValkyrienWarfareMod.physicsManager.getObjectManagingPos(this.world, pos);

                if (wrapper != null) {
                    Vector tilePos = new Vector(pos.getX() + .5D, pos.getY() + .5D, pos.getZ() + .5D);
                    wrapper.wrapping.coordTransform.fromLocalToGlobal(tilePos);

                    tilePos.X -= x;
                    tilePos.Y -= y;
                    tilePos.Z -= z;

                    return tilePos.lengthSq();
                }
            }

        }
        return toReturn;
    }
}
