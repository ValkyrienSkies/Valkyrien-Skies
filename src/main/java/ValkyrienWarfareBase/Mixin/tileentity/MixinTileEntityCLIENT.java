package ValkyrienWarfareBase.Mixin.tileentity;

import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Implements;
import org.spongepowered.asm.mixin.Interface;
import org.spongepowered.asm.mixin.Intrinsic;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;

import ValkyrienWarfareBase.ValkyrienWarfareMod;
import ValkyrienWarfareBase.Collision.Polygon;
import ValkyrienWarfareBase.PhysicsManagement.PhysicsWrapperEntity;
import ValkyrienWarfareBase.Render.IntrinsicTileEntityInterface;
import net.minecraft.block.Block;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

@Mixin(TileEntity.class)
@Implements(@Interface(iface = IntrinsicTileEntityInterface.class, prefix = "vw$"))
public abstract class MixinTileEntityCLIENT {

	@Shadow
	@Final
	public AxisAlignedBB INFINITE_EXTENT_AABB;

	@Shadow
	public World world;

	@Intrinsic(displace = true)
    public AxisAlignedBB vw$getRenderBoundingBox(){
        AxisAlignedBB toReturn = getRenderBoundingBox();
        BlockPos pos = new BlockPos(toReturn.minX, toReturn.minY, toReturn.minZ);
        PhysicsWrapperEntity wrapper = ValkyrienWarfareMod.physicsManager.getObjectManagingPos(world, pos);
        if(wrapper != null) {
        	Polygon poly = new Polygon(toReturn, wrapper.wrapping.coordTransform.lToWTransform);
        	return poly.getEnclosedAABB();
        }
        return toReturn;
    }

	@Shadow
	public abstract AxisAlignedBB getRenderBoundingBox();

	@Shadow
	public abstract BlockPos getPos();

	@Shadow
	public abstract Block getBlockType();
}
