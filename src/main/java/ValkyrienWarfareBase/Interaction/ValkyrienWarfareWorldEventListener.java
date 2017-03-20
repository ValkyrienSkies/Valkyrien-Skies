package ValkyrienWarfareBase.Interaction;

import ValkyrienWarfareBase.ValkyrienWarfareMod;
import ValkyrienWarfareBase.API.RotationMatrices;
import ValkyrienWarfareBase.API.Vector;
import ValkyrienWarfareBase.CoreMod.CallRunner;
import ValkyrienWarfareBase.PhysicsManagement.PhysicsWrapperEntity;
import ValkyrienWarfareCombat.Entity.EntityMountingWeaponBase;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.Entity;
import net.minecraft.entity.item.EntityArmorStand;
import net.minecraft.entity.item.EntityBoat;
import net.minecraft.entity.item.EntityFallingBlock;
import net.minecraft.entity.passive.EntityPig;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.network.play.server.SPacketBlockBreakAnim;
import net.minecraft.util.SoundCategory;
import net.minecraft.util.SoundEvent;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.world.IWorldEventListener;
import net.minecraft.world.World;

public class ValkyrienWarfareWorldEventListener implements IWorldEventListener{

	private World worldObj;

	public ValkyrienWarfareWorldEventListener(World world){
		worldObj = world;
	}

	//TODO: Maybe replace the ASM setBlockState with this instead
	@Override
	public void notifyBlockUpdate(World worldIn, BlockPos pos, IBlockState oldState, IBlockState newState, int flags) {
		PhysicsWrapperEntity wrapper = ValkyrienWarfareMod.physicsManager.getObjectManagingPos(worldObj, pos);
		if(worldObj.isRemote){
			CallRunner.markBlockRangeForRenderUpdate(worldIn,pos.getX(),pos.getY(),pos.getZ(),pos.getX(),pos.getY(),pos.getZ());
			//Strange bounding box error on CLIENT SIDE Fix, possibly broken and terrible, but probably ok
			if(wrapper != null){
				wrapper.wrapping.onSetBlockState(oldState, newState, pos);
			}
		}else{
			if(wrapper != null){
				wrapper.wrapping.pilotingController.onSetBlockInShip(pos, newState);
			}
		}
	}

	@Override
	public void notifyLightSet(BlockPos pos) {
		// TODO Auto-generated method stub

	}

	@Override
	public void markBlockRangeForRenderUpdate(int x1, int y1, int z1, int x2, int y2, int z2) {
//		if(worldObj.isRemote){
//			int midX = (x1 + x2) / 2;
//			int midY = (y1 + y2) / 2;
//			int midZ = (z1 + z2) / 2;
//			BlockPos newPos = new BlockPos(midX, midY, midZ);
//			PhysicsWrapperEntity wrapper = ValkyrienWarfareMod.physicsManager.getObjectManagingPos(worldObj, newPos);
//			if (wrapper != null && wrapper.wrapping.renderer != null) {
//				wrapper.wrapping.renderer.updateRange(x1-1, y1-1, z1-1, x2+1, y2+1, z2+1);
//			}
//		}
	}

	@Override
	public void playSoundToAllNearExcept(EntityPlayer player, SoundEvent soundIn, SoundCategory category, double x,
			double y, double z, float volume, float pitch) {
		// TODO Auto-generated method stub

	}

	@Override
	public void playRecord(SoundEvent soundIn, BlockPos pos) {
		// TODO Auto-generated method stub

	}

	@Override
	public void spawnParticle(int particleID, boolean ignoreRange, double xCoord, double yCoord, double zCoord,
			double xSpeed, double ySpeed, double zSpeed, int... parameters) {
		// TODO Auto-generated method stub

	}

	@Override
	public void onEntityAdded(Entity entityIn) {
		int oldChunkX = MathHelper.floor_double(entityIn.posX / 16.0D);
        int oldChunkZ = MathHelper.floor_double(entityIn.posZ / 16.0D);

		BlockPos posAt = new BlockPos(entityIn);
		PhysicsWrapperEntity wrapper = ValkyrienWarfareMod.physicsManager.getObjectManagingPos(worldObj, posAt);
		if (!(entityIn instanceof EntityFallingBlock) && wrapper != null && wrapper.wrapping.coordTransform != null) {
			if (entityIn instanceof EntityMountingWeaponBase || entityIn instanceof EntityArmorStand || entityIn instanceof EntityPig || entityIn instanceof EntityBoat) {
//				entity.startRiding(wrapper);
				wrapper.wrapping.fixEntity(entityIn, new Vector(entityIn));
				wrapper.wrapping.queueEntityForMounting(entityIn);
			}
			RotationMatrices.applyTransform(wrapper.wrapping.coordTransform.lToWTransform, wrapper.wrapping.coordTransform.lToWRotation, entityIn);

			int newChunkX = MathHelper.floor_double(entityIn.posX / 16.0D);
	        int newChunkZ = MathHelper.floor_double(entityIn.posZ / 16.0D);

			worldObj.getChunkFromChunkCoords(oldChunkX, oldChunkZ).removeEntity(entityIn);
			worldObj.getChunkFromChunkCoords(newChunkX, newChunkZ).addEntity(entityIn);

		}
		if(entityIn instanceof PhysicsWrapperEntity){
			ValkyrienWarfareMod.physicsManager.onShipLoad((PhysicsWrapperEntity) entityIn);
		}
	}

	@Override
	public void onEntityRemoved(Entity entityIn) {
		if (entityIn instanceof PhysicsWrapperEntity) {
			ValkyrienWarfareMod.physicsManager.onShipUnload((PhysicsWrapperEntity) entityIn);
		}
	}

	@Override
	public void broadcastSound(int soundID, BlockPos pos, int data) {
		// TODO Auto-generated method stub

	}

	@Override
	public void playEvent(EntityPlayer player, int type, BlockPos blockPosIn, int data) {
		// TODO Auto-generated method stub

	}

	@Override
	public void sendBlockBreakProgress(int breakerId, BlockPos pos, int progress) {
		if(!worldObj.isRemote){
		 for (EntityPlayer entityplayermp : worldObj.playerEntities){
	            if (entityplayermp != null && entityplayermp.getEntityId() != breakerId){
	            	Vector posVector = new Vector(pos.getX(),pos.getY(),pos.getZ());

	            	PhysicsWrapperEntity wrapper = ValkyrienWarfareMod.physicsManager.getObjectManagingPos(worldObj, pos);

	            	if(wrapper != null){
	            		RotationMatrices.applyTransform(wrapper.wrapping.coordTransform.lToWTransform, posVector);
	            	}

	                double d0 = posVector.X - entityplayermp.posX;
	                double d1 = posVector.Y - entityplayermp.posY;
	                double d2 = posVector.Z - entityplayermp.posZ;

	                if (d0 * d0 + d1 * d1 + d2 * d2 < 1024.0D){
	                    ((EntityPlayerMP)entityplayermp).connection.sendPacket(new SPacketBlockBreakAnim(breakerId, pos, progress));
	                }
	            }
	        }
		}
	}


}
