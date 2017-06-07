package ValkyrienWarfareBase.CoreMod;

import ValkyrienWarfareBase.API.RotationMatrices;
import ValkyrienWarfareBase.API.Vector;
import ValkyrienWarfareBase.Interaction.ShipUUIDToPosData.ShipPositionData;
import ValkyrienWarfareBase.PhysicsManagement.PhysicsWrapperEntity;
import ValkyrienWarfareBase.PhysicsManagement.WorldPhysObjectManager;
import ValkyrienWarfareBase.ValkyrienWarfareMod;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.network.Packet;
import net.minecraft.network.play.server.SPacketEffect;
import net.minecraft.network.play.server.SPacketSoundEffect;
import net.minecraft.server.management.PlayerList;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.SoundCategory;
import net.minecraft.util.SoundEvent;
import net.minecraft.util.math.*;
import net.minecraft.util.math.RayTraceResult.Type;
import net.minecraft.world.World;
import net.minecraft.world.chunk.Chunk;
import net.minecraftforge.common.DimensionManager;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.UUID;

public class CallRunner {

	public static Iterator<Chunk> rebuildChunkIterator(Iterator<Chunk> chunkIterator){
		ArrayList<Chunk> newBackingArray = new ArrayList<Chunk>();
		while(chunkIterator.hasNext()){
			newBackingArray.add(chunkIterator.next());
		}
		return newBackingArray.iterator();
	}

    public static BlockPos getBedSpawnLocation(BlockPos toReturn, World worldIn, BlockPos bedLocation, boolean forceSpawn){
//    	System.out.println("Keep their heads ringing");

		int chunkX = bedLocation.getX() >> 4;
		int chunkZ = bedLocation.getZ() >> 4;
		long chunkPos = ChunkPos.asLong(chunkX, chunkZ);

		UUID shipManagingID = ValkyrienWarfareMod.chunkManager.getShipIDManagingPos_Persistant(worldIn, chunkX, chunkZ);
		if(shipManagingID != null){
			ShipPositionData positionData = ValkyrienWarfareMod.chunkManager.getShipPosition_Persistant(worldIn, shipManagingID);

			if(positionData != null){
				double[] lToWTransform = RotationMatrices.convertToDouble(positionData.lToWTransform);

				Vector bedPositionInWorld = new Vector(bedLocation.getX() + .5D, bedLocation.getY() + .5D, bedLocation.getZ() + .5D);
				RotationMatrices.applyTransform(lToWTransform, bedPositionInWorld);

				bedPositionInWorld.Y += 1D;

				bedLocation = new BlockPos(bedPositionInWorld.X, bedPositionInWorld.Y, bedPositionInWorld.Z);

				return bedLocation;
			}else{
				System.err.println("A ship just had Chunks claimed persistant, but not any position data persistant");
			}
		}

    	return toReturn;
    }

//	public static Iterator<Chunk> onGetPersistentChunkIterable(World world, Iterator<Chunk> chunkIterator) {
//		Iterator<Chunk> vanillaResult = world.getPersistentChunkIterable(chunkIterator);
//		ArrayList<Chunk> newResultArray = new ArrayList<Chunk>();
//		while (vanillaResult.hasNext()) {
//			newResultArray.add(vanillaResult.next());
//		}
//		WorldPhysObjectManager manager = ValkyrienWarfareMod.physicsManager.getManagerForWorld(world);
//		ArrayList<PhysicsWrapperEntity> physEntities = (ArrayList<PhysicsWrapperEntity>) manager.physicsEntities.clone();
//		for (PhysicsWrapperEntity wrapper : physEntities) {
//			for (Chunk[] chunkArray : wrapper.wrapping.claimedChunks) {
//				for (Chunk chunk : chunkArray) {
//					newResultArray.add(chunk);
//				}
//			}
//		}
//		return newResultArray.iterator();
//	}

	public static double getDistanceSq(double vanilla, Entity entity, double x, double y, double z) {
		if (vanilla < 64.0D) {
			return vanilla;
		} else {
			BlockPos pos = new BlockPos(x, y, z);
			PhysicsWrapperEntity wrapper = ValkyrienWarfareMod.physicsManager.getObjectManagingPos(entity.world, pos);
			if (wrapper != null) {
				Vector posVec = new Vector(x, y, z);
				wrapper.wrapping.coordTransform.fromLocalToGlobal(posVec);
				posVec.X -= entity.posX;
				posVec.Y -= entity.posY;
				posVec.Z -= entity.posZ;
				if (vanilla > posVec.lengthSq()) {
					return posVec.lengthSq();
				}
			}
		}
		return vanilla;
	}

	public static void onPlaySound(World world, double x, double y, double z, SoundEvent soundIn, SoundCategory category, float volume, float pitch, boolean distanceDelay) {
		BlockPos pos = new BlockPos(x, y, z);
		PhysicsWrapperEntity wrapper = ValkyrienWarfareMod.physicsManager.getObjectManagingPos(world, pos);
		if (wrapper != null) {
			Vector posVec = new Vector(x, y, z);
			wrapper.wrapping.coordTransform.fromLocalToGlobal(posVec);
			x = posVec.X;
			y = posVec.Y;
			z = posVec.Z;
		}
		world.playSound(x, y, z, soundIn, category, volume, pitch, distanceDelay);
	}

	public static double getDistanceSq(double ret, TileEntity ent, double x, double y, double z) {
		PhysicsWrapperEntity wrapper = ValkyrienWarfareMod.physicsManager.getObjectManagingPos(ent.getWorld(), ent.getPos());
		if (wrapper != null) {
			Vector vec = new Vector(x, y, z);
			wrapper.wrapping.coordTransform.fromGlobalToLocal(vec);
			double d0 = ent.getPos().getX() - vec.X;
	        double d1 = ent.getPos().getY() - vec.Y;
	        double d2 = ent.getPos().getZ() - vec.Z;
	        return d0 * d0 + d1 * d1 + d2 * d2;
		}
		return ret;
	}

/*	public static boolean onSpawnEntityInWorld(World world, Entity entity) {
		BlockPos posAt = new BlockPos(entity);
		PhysicsWrapperEntity wrapper = ValkyrienWarfareMod.physicsManager.getObjectManagingPos(world, posAt);
		if (!(entity instanceof EntityFallingBlock) && wrapper != null && wrapper.wrapping.coordTransform != null) {
			if (entity instanceof EntityMountingWeaponBase || entity instanceof EntityArmorStand || entity instanceof EntityPig || entity instanceof EntityBoat) {
//				entity.startRiding(wrapper);
				wrapper.wrapping.fixEntity(entity, new Vector(entity));
				wrapper.wrapping.queueEntityForMounting(entity);
			}
			RotationMatrices.applyTransform(wrapper.wrapping.coordTransform.lToWTransform, wrapper.wrapping.coordTransform.lToWRotation, entity);
		}
		return world.spawnEntityInWorld(entity);
	}*/

	public static void onSendToAllNearExcept(PlayerList list, @Nullable EntityPlayer except, double x, double y, double z, double radius, int dimension, Packet<?> packetIn) {
		BlockPos pos = new BlockPos(x, y, z);
		World worldIn = null;
		if (except == null) {
			worldIn = DimensionManager.getWorld(dimension);
		} else {
			worldIn = except.world;
		}
		PhysicsWrapperEntity wrapper = ValkyrienWarfareMod.physicsManager.getObjectManagingPos(worldIn, pos);
		Vector packetPosition = new Vector(x, y, z);
		if (wrapper != null && wrapper.wrapping.coordTransform != null) {
			wrapper.wrapping.coordTransform.fromLocalToGlobal(packetPosition);

			if (packetIn instanceof SPacketSoundEffect) {
				SPacketSoundEffect soundEffect = (SPacketSoundEffect) packetIn;
				packetIn = new SPacketSoundEffect(soundEffect.sound, soundEffect.category, packetPosition.X, packetPosition.Y, packetPosition.Z, soundEffect.soundVolume, soundEffect.soundPitch);
			}
			//
			if (packetIn instanceof SPacketEffect) {
				SPacketEffect effect = (SPacketEffect) packetIn;
				BlockPos blockpos = new BlockPos(packetPosition.X, packetPosition.Y, packetPosition.Z);
				packetIn = new SPacketEffect(effect.soundType, blockpos, effect.soundData, effect.serverWide);
			}
		}

		x = packetPosition.X;
		y = packetPosition.Y;
		z = packetPosition.Z;

		// list.sendToAllNearExcept(except, packetPosition.X, packetPosition.Y, packetPosition.Z, radius, dimension, packetIn);

		for (int i = 0; i < list.playerEntityList.size(); ++i) {
			EntityPlayerMP entityplayermp = (EntityPlayerMP) list.playerEntityList.get(i);

			if (entityplayermp != except && entityplayermp.dimension == dimension) {
				// NOTE: These are set to use the last variables for a good reason; dont change them
				double d0 = x - entityplayermp.posX;
				double d1 = y - entityplayermp.posY;
				double d2 = z - entityplayermp.posZ;

				if (d0 * d0 + d1 * d1 + d2 * d2 < radius * radius) {
					entityplayermp.connection.sendPacket(packetIn);
				} else {
					d0 = x - entityplayermp.lastTickPosX;
					d1 = y - entityplayermp.lastTickPosY;
					d2 = z - entityplayermp.lastTickPosZ;
					if (d0 * d0 + d1 * d1 + d2 * d2 < radius * radius) {
						entityplayermp.connection.sendPacket(packetIn);
					}
				}
			}
		}
	}

	public static void onSetBlockState(IBlockState newState, int flags, World world, BlockPos pos){
		IBlockState oldState = world.getBlockState(pos);
		PhysicsWrapperEntity wrapper = ValkyrienWarfareMod.physicsManager.getObjectManagingPos(world, pos);
		if (wrapper != null) {
			wrapper.wrapping.onSetBlockState(oldState, newState, pos);
		}
    }

	public static RayTraceResult onRayTraceBlocks(World world, Vec3d vec31, Vec3d vec32, boolean stopOnLiquid, boolean ignoreBlockWithoutBoundingBox, boolean returnLastUncollidableBlock) {
		return rayTraceBlocksIgnoreShip(world, vec31, vec32, stopOnLiquid, ignoreBlockWithoutBoundingBox, returnLastUncollidableBlock, null);
	}

	public static RayTraceResult rayTraceBlocksIgnoreShip(World world, Vec3d vec31, Vec3d vec32, boolean stopOnLiquid, boolean ignoreBlockWithoutBoundingBox, boolean returnLastUncollidableBlock, PhysicsWrapperEntity toIgnore) {
		RayTraceResult vanillaTrace = world.rayTraceBlocks(vec31, vec32, stopOnLiquid, ignoreBlockWithoutBoundingBox, returnLastUncollidableBlock);
		WorldPhysObjectManager physManager = ValkyrienWarfareMod.physicsManager.getManagerForWorld(world);
		if (physManager == null) {
			return vanillaTrace;
		}

		Vec3d playerEyesPos = vec31;
		Vec3d playerReachVector = vec32.subtract(vec31);

		AxisAlignedBB playerRangeBB = new AxisAlignedBB(vec31.xCoord, vec31.yCoord, vec31.zCoord, vec32.xCoord, vec32.yCoord, vec32.zCoord);

		List<PhysicsWrapperEntity> nearbyShips = physManager.getNearbyPhysObjects(playerRangeBB);
		//Get rid of the Ship that we're not supposed to be RayTracing for
		nearbyShips.remove(toIgnore);

		boolean changed = false;

		double reachDistance = playerReachVector.lengthVector();
		double worldResultDistFromPlayer = 420000000D;
		if (vanillaTrace != null && vanillaTrace.hitVec != null) {
			worldResultDistFromPlayer = vanillaTrace.hitVec.distanceTo(vec31);
		}
		for (PhysicsWrapperEntity wrapper : nearbyShips) {
			playerEyesPos = vec31;
			playerReachVector = vec32.subtract(vec31);
			// TODO: Re-enable
			if (world.isRemote) {
				// ValkyrienWarfareMod.proxy.updateShipPartialTicks(wrapper);
			}
			// Transform the coordinate system for the player eye pos
			playerEyesPos = RotationMatrices.applyTransform(wrapper.wrapping.coordTransform.RwToLTransform, playerEyesPos);
			playerReachVector = RotationMatrices.applyTransform(wrapper.wrapping.coordTransform.RwToLRotation, playerReachVector);
			Vec3d playerEyesReachAdded = playerEyesPos.addVector(playerReachVector.xCoord * reachDistance, playerReachVector.yCoord * reachDistance, playerReachVector.zCoord * reachDistance);
			RayTraceResult resultInShip = world.rayTraceBlocks(playerEyesPos, playerEyesReachAdded, stopOnLiquid, ignoreBlockWithoutBoundingBox, returnLastUncollidableBlock);
			if (resultInShip != null && resultInShip.hitVec != null && resultInShip.typeOfHit == Type.BLOCK) {
				double shipResultDistFromPlayer = resultInShip.hitVec.distanceTo(playerEyesPos);
				if (shipResultDistFromPlayer < worldResultDistFromPlayer) {
					worldResultDistFromPlayer = shipResultDistFromPlayer;
					resultInShip.hitVec = RotationMatrices.applyTransform(wrapper.wrapping.coordTransform.RlToWTransform, resultInShip.hitVec);
					vanillaTrace = resultInShip;
				}
			}
		}

		return vanillaTrace;
	}
}
