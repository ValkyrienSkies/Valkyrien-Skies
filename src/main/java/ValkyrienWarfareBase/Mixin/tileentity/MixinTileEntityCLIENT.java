package ValkyrienWarfareBase.Mixin.tileentity;

import ValkyrienWarfareBase.Collision.Polygon;
import ValkyrienWarfareBase.PhysicsManagement.PhysicsWrapperEntity;
import ValkyrienWarfareBase.Render.IntrinsicTileEntityInterface;
import ValkyrienWarfareBase.ValkyrienWarfareMod;
import net.minecraft.block.Block;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.*;

@Mixin(TileEntity.class)
@Implements(@Interface(iface = IntrinsicTileEntityInterface.class, prefix = "vw$"))
public abstract class MixinTileEntityCLIENT {

	@Shadow
	@Final
	public AxisAlignedBB INFINITE_EXTENT_AABB;

	@Shadow
	public World world;

	@Intrinsic(displace = true)
	public AxisAlignedBB vw$getRenderBoundingBox() {
		AxisAlignedBB toReturn = getRenderBoundingBox();
		BlockPos pos = new BlockPos(toReturn.minX, toReturn.minY, toReturn.minZ);
		PhysicsWrapperEntity wrapper = ValkyrienWarfareMod.physicsManager.getObjectManagingPos(world, pos);
		if (wrapper != null) {
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
