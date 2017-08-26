package ValkyrienWarfareBase.Mixin.entity;

import ValkyrienWarfareBase.API.Vector;
import ValkyrienWarfareBase.Collision.EntityPolygon;
import ValkyrienWarfareBase.PhysicsManagement.PhysicsWrapperEntity;
import ValkyrienWarfareBase.ValkyrienWarfareMod;
import net.minecraft.block.Block;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.List;

@Mixin(EntityLivingBase.class)
public abstract class MixinEntityLivingBase extends Entity {

	/**
	 * This constructor is needed to make javac happy but doesn't actually affect anything
	 */
	public MixinEntityLivingBase(World world) {
		super(world);
	}

	@Inject(method = "dismountEntity", at = @At("HEAD"), cancellable = true)
	public void dismountEntity(Entity entityIn, CallbackInfo info) {
		if (entityIn instanceof PhysicsWrapperEntity) {
			this.ridingEntity = null;
			this.posY += 1.45D;
			info.cancel(); // effectively a premature method return
		}
	}

	@Overwrite
	public boolean isOnLadder() {
		boolean vanilla = this.isOnLadderOriginalButSlightlyOptimized();
		if (vanilla) {
			return true;
		}
		if (EntityPlayer.class.isInstance(this) && EntityPlayer.class.cast(this).isSpectator()) {
			return false;
		}
		List<PhysicsWrapperEntity> nearbyPhys = ValkyrienWarfareMod.physicsManager.getManagerForWorld(this.world).getNearbyPhysObjects(this.getEntityBoundingBox());
		for (PhysicsWrapperEntity physWrapper : nearbyPhys) {
			Vector playerPos = new Vector(EntityLivingBase.class.cast(this));
			physWrapper.wrapping.coordTransform.fromGlobalToLocal(playerPos);
			int i = MathHelper.floor(playerPos.X);
			int j = MathHelper.floor(playerPos.Y);
			int k = MathHelper.floor(playerPos.Z);

			BlockPos blockpos = new BlockPos(i, j, k);
			IBlockState iblockstate = this.world.getBlockState(blockpos);
			Block block = iblockstate.getBlock();

            /*boolean isSpectator = (EntityPlayer.class.isInstance(this) && EntityPlayer.class.cast(this).isSpectator());
            if (isSpectator)
                return false;*/
			//not needed, we already do this check

			EntityPolygon playerPoly = new EntityPolygon(this.getEntityBoundingBox(), physWrapper.wrapping.coordTransform.wToLTransform, this);
			AxisAlignedBB bb = playerPoly.getEnclosedAABB();
			for (int x = MathHelper.floor(bb.minX); x < bb.maxX; x++) {
				for (int y = MathHelper.floor(bb.minY); y < bb.maxY; y++) {
					for (int z = MathHelper.floor(bb.minZ); z < bb.maxZ; z++) {
						BlockPos pos = new BlockPos(x, y, z);
						IBlockState checkState = this.world.getBlockState(pos);
						if (checkState.getBlock().isLadder(checkState, this.world, pos, EntityLivingBase.class.cast(this))) {
							return true;
							// AxisAlignedBB ladderBB = checkState.getBlock().getBoundingBox(checkState, base.worldObj, pos).offset(pos).expandXyz(.1D);
							// Polygon checkBlock = new Polygon(ladderBB);
							// EntityPolygonCollider collider = new EntityPolygonCollider(playerPoly, checkBlock, physWrapper.wrapping.coordTransform.normals, new Vector(base.motionX,base.motionY,base.motionZ));
							//// System.out.println(!collider.seperated);
							// if(!collider.seperated){
							// return true;
							// }
						}
					}
				}
			}

			// return net.minecraftforge.common.ForgeHooks.isLivingOnLadder(iblockstate, base.worldObj, new BlockPos(i, j, k), base);
		}
		return false;
	}

	public boolean isOnLadderOriginalButSlightlyOptimized() {
		int i = MathHelper.floor(this.posX);
		int j = MathHelper.floor(this.getEntityBoundingBox().minY);
		int k = MathHelper.floor(this.posZ);
		BlockPos blockpos = new BlockPos(i, j, k);
		IBlockState iblockstate = this.world.getBlockState(blockpos);
		Block block = iblockstate.getBlock();
		return net.minecraftforge.common.ForgeHooks.isLivingOnLadder(iblockstate, world, new BlockPos(i, j, k), EntityLivingBase.class.cast(this));
	}
}
