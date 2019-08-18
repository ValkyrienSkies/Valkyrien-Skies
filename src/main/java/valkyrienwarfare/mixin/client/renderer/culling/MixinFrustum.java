package valkyrienwarfare.mixin.client.renderer.culling;

import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.culling.Frustum;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;
import org.spongepowered.asm.mixin.Shadow;
import valkyrienwarfare.mod.common.util.ValkyrienUtils;

@Mixin(Frustum.class)
public abstract class MixinFrustum {

    @Shadow
    public abstract boolean isBoxInFrustum(double p_78548_1_, double p_78548_3_, double p_78548_5_, double p_78548_7_, double p_78548_9_, double p_78548_11_);

    /**
     * Temp code until I can manage to properly mixin displace TileEntity.getRenderBoundingBox() for every tile entity.
     *
     * @author thebest108
     */
    @Overwrite
    public boolean isBoundingBoxInFrustum(AxisAlignedBB axisAlignedBB) {
        BlockPos pos = new BlockPos((axisAlignedBB.minX + axisAlignedBB.maxX) / 2, (axisAlignedBB.minY + axisAlignedBB.maxY) / 2, (axisAlignedBB.minZ + axisAlignedBB.maxZ) / 2);
        axisAlignedBB = ValkyrienUtils.getAABBInGlobal(axisAlignedBB, Minecraft.getMinecraft().world, pos);
        return isBoxInFrustum(axisAlignedBB.minX, axisAlignedBB.minY, axisAlignedBB.minZ, axisAlignedBB.maxX, axisAlignedBB.maxY, axisAlignedBB.maxZ);
    }
}
