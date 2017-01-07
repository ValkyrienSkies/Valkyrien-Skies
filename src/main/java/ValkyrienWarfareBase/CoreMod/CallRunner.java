package ValkyrienWarfareBase.CoreMod;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;

import javax.annotation.Nullable;

import com.google.common.base.Predicate;
import com.mojang.authlib.GameProfile;

import ValkyrienWarfareBase.ValkyrienWarfareMod;
import ValkyrienWarfareBase.API.RotationMatrices;
import ValkyrienWarfareBase.API.Vector;
import ValkyrienWarfareBase.Collision.EntityCollisionInjector;
import ValkyrienWarfareBase.Collision.EntityPolygon;
import ValkyrienWarfareBase.Collision.Polygon;
import ValkyrienWarfareBase.Physics.BlockMass;
import ValkyrienWarfareBase.Physics.PhysicsQueuedForce;
import ValkyrienWarfareBase.PhysicsManagement.PhysicsWrapperEntity;
import ValkyrienWarfareBase.PhysicsManagement.WorldPhysObjectManager;
import ValkyrienWarfareCombat.Entity.EntityMountingWeaponBase;
import net.minecraft.block.Block;
import net.minecraft.block.BlockLiquid;
import net.minecraft.block.material.Material;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.item.EntityFallingBlock;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.inventory.Container;
import net.minecraft.network.Packet;
import net.minecraft.network.play.server.SPacketEffect;
import net.minecraft.network.play.server.SPacketSoundEffect;
import net.minecraft.server.management.PlayerList;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.SoundCategory;
import net.minecraft.util.SoundEvent;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.RayTraceResult;
import net.minecraft.util.math.RayTraceResult.Type;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.Explosion;
import net.minecraft.world.World;
import net.minecraft.world.chunk.Chunk;
import net.minecraft.world.gen.ChunkProviderServer;
import net.minecraftforge.common.DimensionManager;

public class CallRunner {

	public static Vec3d onGetLookVec(Entity entity) {
		// System.out.println("test");
		return entity.getLookVec();
	}

	public static BlockPos onGetPrecipitationHeight(World world, BlockPos pos) {
		BlockPos percipitationHeightPos = world.getPrecipitationHeight(pos);
		// TODO: Fix this
		if (world.isRemote && false) {
			int yRange = 999999;

			boolean inWorldSpace = !ValkyrienWarfareMod.chunkManager.isChunkInShipRange(world, pos.getX() >> 4, pos.getZ() >> 4);
			// Dont run this on any Ship Chunks
			if (inWorldSpace) {
				AxisAlignedBB rainCheckBB = new AxisAlignedBB(pos.getX(), pos.getY() - yRange, pos.getZ(), pos.getX() + 1, pos.getY() + yRange, pos.getZ() + 1);
				List<PhysicsWrapperEntity> wrappers = ValkyrienWarfareMod.physicsManager.getManagerForWorld(world).getNearbyPhysObjects(rainCheckBB);
				for (PhysicsWrapperEntity wrapper : wrappers) {
					Vector traceStart = new Vector(percipitationHeightPos.getX() + .5D, percipitationHeightPos.getY() + .5D, percipitationHeightPos.getZ() + .5D);
					RotationMatrices.applyTransform(wrapper.wrapping.coordTransform.wToLTransform, traceStart);

					Vector currentTrace = new Vector(traceStart);

					Vector rayTraceVector = new Vector(0, 1, 0);
					RotationMatrices.applyTransform(wrapper.wrapping.coordTransform.lToWRotation, rayTraceVector);

					int range = 120;

					while (range > 0 && currentTrace.Y > 0 && currentTrace.Y < 255) {
						currentTrace.add(rayTraceVector);

						BlockPos toCheck = new BlockPos(Math.round(currentTrace.X), Math.round(currentTrace.Y), Math.round(currentTrace.Z));

						IBlockState iblockstate = wrapper.wrapping.VKChunkCache.getBlockState(toCheck);
						Material material = iblockstate.getMaterial();

						if (material.blocksMovement()) {
							Vector currentInWorld = new Vector(currentTrace);
							RotationMatrices.applyTransform(wrapper.wrapping.coordTransform.lToWTransform, currentInWorld);
							// System.out.println("test");
							return new BlockPos(Math.round(currentInWorld.X), Math.round(currentInWorld.Y), Math.round(currentInWorld.Z));
						}

						range--;
					}

				}
			}
		}
		return percipitationHeightPos;
	}

	public static boolean onIsOnLadder(EntityLivingBase base) {
		boolean vanilla = base.isOnLadder();
		if (vanilla) {
			return true;
		}
		if (base instanceof EntityPlayer && ((EntityPlayer) base).isSpectator()) {
			return false;
		}
		List<PhysicsWrapperEntity> nearbyPhys = ValkyrienWarfareMod.physicsManager.getManagerForWorld(base.worldObj).getNearbyPhysObjects(base.getEntityBoundingBox());
		for (PhysicsWrapperEntity physWrapper : nearbyPhys) {
			Vector playerPos = new Vector(base);
			physWrapper.wrapping.coordTransform.fromGlobalToLocal(playerPos);
			int i = MathHelper.floor_double(playerPos.X);
			int j = MathHelper.floor_double(playerPos.Y);
			int k = MathHelper.floor_double(playerPos.Z);

			BlockPos blockpos = new BlockPos(i, j, k);
			IBlockState iblockstate = base.worldObj.getBlockState(blockpos);
			Block block = iblockstate.getBlock();

			boolean isSpectator = (base instanceof EntityPlayer && ((EntityPlayer) base).isSpectator());
			if (isSpectator)
				return false;

			EntityPolygon playerPoly = new EntityPolygon(base.getEntityBoundingBox(), physWrapper.wrapping.coordTransform.wToLTransform, base);
			AxisAlignedBB bb = playerPoly.getEnclosedAABB();
			for (int x = MathHelper.floor_double(bb.minX); x < bb.maxX; x++) {
				for (int y = MathHelper.floor_double(bb.minY); y < bb.maxY; y++) {
					for (int z = MathHelper.floor_double(bb.minZ); z < bb.maxZ; z++) {
						BlockPos pos = new BlockPos(x, y, z);
						IBlockState checkState = base.worldObj.getBlockState(pos);
						if (checkState.getBlock().isLadder(checkState, base.worldObj, pos, base)) {
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

	public static void onExplosionA(Explosion e) {
		Vector center = new Vector(e.explosionX, e.explosionY, e.explosionZ);
		World worldIn = e.worldObj;
		float radius = e.explosionSize;

		AxisAlignedBB toCheck = new AxisAlignedBB(center.X - radius, center.Y - radius, center.Z - radius, center.X + radius, center.Y + radius, center.Z + radius);
		List<PhysicsWrapperEntity> shipsNear = ValkyrienWarfareMod.physicsManager.getManagerForWorld(e.worldObj).getNearbyPhysObjects(toCheck);
		e.doExplosionA();
		// TODO: Make this compatible and shit!
		for (PhysicsWrapperEntity ship : shipsNear) {
			Vector inLocal = new Vector(center);
			RotationMatrices.applyTransform(ship.wrapping.coordTransform.wToLTransform, inLocal);
			// inLocal.roundToWhole();
			Explosion expl = new Explosion(ship.worldObj, null, inLocal.X, inLocal.Y, inLocal.Z, radius, false, false);

			double waterRange = .6D;

			boolean cancelDueToWater = false;

			for (int x = (int) Math.floor(expl.explosionX - waterRange); x <= Math.ceil(expl.explosionX + waterRange); x++) {
				for (int y = (int) Math.floor(expl.explosionY - waterRange); y <= Math.ceil(expl.explosionY + waterRange); y++) {
					for (int z = (int) Math.floor(expl.explosionZ - waterRange); z <= Math.ceil(expl.explosionZ + waterRange); z++) {
						if (!cancelDueToWater) {
							IBlockState state = e.worldObj.getBlockState(new BlockPos(x, y, z));
							if (state.getBlock() instanceof BlockLiquid) {
								cancelDueToWater = true;
							}
						}
					}
				}
			}

			expl.doExplosionA();

			double affectedPositions = 0D;

			for (Object o : expl.affectedBlockPositions) {
				BlockPos pos = (BlockPos) o;
				IBlockState state = ship.worldObj.getBlockState(pos);
				Block block = state.getBlock();
				if (!block.isAir(state, worldIn, (BlockPos) o) || ship.wrapping.explodedPositionsThisTick.contains((BlockPos) o)) {
					affectedPositions++;
				}
			}

			if (!cancelDueToWater) {
				for (Object o : expl.affectedBlockPositions) {
					BlockPos pos = (BlockPos) o;

					IBlockState state = ship.worldObj.getBlockState(pos);
					Block block = state.getBlock();
					if (!block.isAir(state, worldIn, (BlockPos) o) || ship.wrapping.explodedPositionsThisTick.contains((BlockPos) o)) {
						if (block.canDropFromExplosion(expl)) {
							block.dropBlockAsItemWithChance(ship.worldObj, pos, state, 1.0F / expl.explosionSize, 0);
						}
						block.onBlockExploded(ship.worldObj, pos, expl);
						if (!worldIn.isRemote) {
							Vector posVector = new Vector(pos.getX() + .5, pos.getY() + .5, pos.getZ() + .5);

							ship.wrapping.coordTransform.fromLocalToGlobal(posVector);

							double mass = BlockMass.basicMass.getMassFromState(state, pos, ship.worldObj);

							double explosionForce = Math.sqrt(e.explosionSize) * 1000D * mass;

							Vector forceVector = new Vector(pos.getX() + .5 - expl.explosionX, pos.getY() + .5 - expl.explosionY, pos.getZ() + .5 - expl.explosionZ);

							double vectorDist = forceVector.length();

							forceVector.normalize();

							forceVector.multiply(explosionForce / vectorDist);

							RotationMatrices.doRotationOnly(ship.wrapping.coordTransform.lToWRotation, forceVector);

							PhysicsQueuedForce queuedForce = new PhysicsQueuedForce(forceVector, posVector, false, 1);

							if (!ship.wrapping.explodedPositionsThisTick.contains(pos)) {
								ship.wrapping.explodedPositionsThisTick.add(pos);
							}

							ship.wrapping.queueForce(queuedForce);
						}
					}
				}
			}

		}
	}

	public static <T extends Entity> List<T> onGetEntitiesWithinAABB(World world, Class<? extends T> clazz, AxisAlignedBB aabb, @Nullable Predicate<? super T> filter) {
		BlockPos pos = new BlockPos((aabb.minX + aabb.maxX) / 2D, (aabb.minY + aabb.maxY) / 2D, (aabb.minZ + aabb.maxZ) / 2D);
		PhysicsWrapperEntity wrapper = ValkyrienWarfareMod.physicsManager.getObjectManagingPos(world, pos);
		if (wrapper != null) {
			Polygon poly = new Polygon(aabb, wrapper.wrapping.coordTransform.lToWTransform);
			aabb = poly.getEnclosedAABB().contract(.3D);
		}
		return world.getEntitiesWithinAABB(clazz, aabb, filter);
	}

	public static List<Entity> onGetEntitiesInAABBexcluding(World world, @Nullable Entity entityIn, AxisAlignedBB boundingBox, @Nullable Predicate<? super Entity> predicate) {
		BlockPos pos = new BlockPos((boundingBox.minX + boundingBox.maxX) / 2D, (boundingBox.minY + boundingBox.maxY) / 2D, (boundingBox.minZ + boundingBox.maxZ) / 2D);
		PhysicsWrapperEntity wrapper = ValkyrienWarfareMod.physicsManager.getObjectManagingPos(world, pos);
		if (wrapper != null) {
			Polygon poly = new Polygon(boundingBox, wrapper.wrapping.coordTransform.lToWTransform);
			boundingBox = poly.getEnclosedAABB().contract(.3D);
		}
		return world.getEntitiesInAABBexcluding(entityIn, boundingBox, predicate);
	}

	public static Iterator<Chunk> onGetPersistentChunkIterable(World world, Iterator<Chunk> chunkIterator) {
		Iterator<Chunk> vanillaResult = world.getPersistentChunkIterable(chunkIterator);
		ArrayList<Chunk> newResultArray = new ArrayList<Chunk>();
		while (vanillaResult.hasNext()) {
			newResultArray.add(vanillaResult.next());
		}
		WorldPhysObjectManager manager = ValkyrienWarfareMod.physicsManager.getManagerForWorld(world);
		ArrayList<PhysicsWrapperEntity> physEntities = (ArrayList<PhysicsWrapperEntity>) manager.physicsEntities.clone();
		for (PhysicsWrapperEntity wrapper : physEntities) {
			for (Chunk[] chunkArray : wrapper.wrapping.claimedChunks) {
				for (Chunk chunk : chunkArray) {
					newResultArray.add(chunk);
				}
			}
		}
		return newResultArray.iterator();
	}

	public static boolean onCanInteractWith(Container con, EntityPlayer player) {
		boolean vanilla = con.canInteractWith(player);
		return true;
	}

	public static double onGetDistanceSq(Entity entity, double x, double y, double z) {
		double vanilla = entity.getDistanceSq(x, y, z);
		if (vanilla < 64.0D) {
			return vanilla;
		} else {
			BlockPos pos = new BlockPos(x, y, z);
			PhysicsWrapperEntity wrapper = ValkyrienWarfareMod.physicsManager.getObjectManagingPos(entity.worldObj, pos);
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

	public static double onGetDistanceSq(Entity entity, BlockPos pos) {
		double vanilla = entity.getDistanceSq(pos);
		if (vanilla < 64.0D) {
			return vanilla;
		} else {
			PhysicsWrapperEntity wrapper = ValkyrienWarfareMod.physicsManager.getObjectManagingPos(entity.worldObj, pos);
			if (wrapper != null) {
				Vector posVec = new Vector(pos.getX() + .5D, pos.getY() + .5D, pos.getZ() + .5D);
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

	public static void onPlaySound1(World world, @Nullable EntityPlayer player, BlockPos pos, SoundEvent soundIn, SoundCategory category, float volume, float pitch) {
		PhysicsWrapperEntity wrapper = ValkyrienWarfareMod.physicsManager.getObjectManagingPos(world, pos);
		if (wrapper != null) {
			Vector posVec = new Vector(pos.getX() + .5D, pos.getY() + .5D, pos.getZ() + .5D);
			wrapper.wrapping.coordTransform.fromLocalToGlobal(posVec);
			world.playSound(player, posVec.X, posVec.Y, posVec.Z, soundIn, category, volume, pitch);
		} else {
			world.playSound(player, pos, soundIn, category, volume, pitch);
		}
	}

	public static void onPlaySound2(World world, @Nullable EntityPlayer player, double x, double y, double z, SoundEvent soundIn, SoundCategory category, float volume, float pitch) {
		Vector posVec = new Vector(x, y, z);
		BlockPos pos = new BlockPos(x, y, z);
		PhysicsWrapperEntity wrapper = ValkyrienWarfareMod.physicsManager.getObjectManagingPos(world, pos);
		if (wrapper != null) {
			wrapper.wrapping.coordTransform.fromLocalToGlobal(posVec);
		}
		world.playSound(player, posVec.X, posVec.Y, posVec.Z, soundIn, category, volume, pitch);
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

	public static double onGetDistanceSq(TileEntity ent, double x, double y, double z) {
		PhysicsWrapperEntity wrapper = ValkyrienWarfareMod.physicsManager.getObjectManagingPos(ent.getWorld(), ent.getPos());
		if (wrapper != null) {
			Vector vec = new Vector(x, y, z);
			wrapper.wrapping.coordTransform.fromGlobalToLocal(vec);
			return ent.getDistanceSq(vec.X, vec.Y, vec.Z);
		}
		return ent.getDistanceSq(x, y, z);
	}

	public static boolean onSpawnEntityInWorld(World world, Entity entity) {
		BlockPos posAt = new BlockPos(entity);
		PhysicsWrapperEntity wrapper = ValkyrienWarfareMod.physicsManager.getObjectManagingPos(world, posAt);
		if (!(entity instanceof EntityFallingBlock) && wrapper != null && wrapper.wrapping.coordTransform != null) {
			RotationMatrices.applyTransform(wrapper.wrapping.coordTransform.lToWTransform, wrapper.wrapping.coordTransform.lToWRotation, entity);
			if (entity instanceof EntityMountingWeaponBase) {
				// wrapper.wrapping.fixEntity(entity);
			}
		}
		return world.spawnEntityInWorld(entity);
	}

	public static void onSendToAllNearExcept(PlayerList list, @Nullable EntityPlayer except, double x, double y, double z, double radius, int dimension, Packet<?> packetIn) {
		BlockPos pos = new BlockPos(x, y, z);
		World worldIn = null;
		if (except == null) {
			worldIn = DimensionManager.getWorld(dimension);
		} else {
			worldIn = except.worldObj;
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

	public static boolean onSetBlockState(World world, BlockPos pos, IBlockState newState, int flags) {
		IBlockState oldState = world.getBlockState(pos);
		boolean toReturn = world.setBlockState(pos, newState, flags);
		PhysicsWrapperEntity wrapper = ValkyrienWarfareMod.physicsManager.getObjectManagingPos(world, pos);
		if (wrapper != null) {
			if(!world.isRemote){
				wrapper.wrapping.pilotingController.onSetBlockInShip(pos, newState);
			}
			wrapper.wrapping.onSetBlockState(oldState, newState, pos);
			if (world.isRemote) {
				wrapper.wrapping.renderer.markForUpdate();
			}
		}
		return toReturn;
	}

	public static void onMarkBlockRangeForRenderUpdate(World worldFor, int x1, int y1, int z1, int x2, int y2, int z2) {
		worldFor.markBlockRangeForRenderUpdate(x1, y1, z1, x2, y2, z2);
		/*
		 * if(worldFor.isRemote){ int midX = (x1+x2)/2; int midY = (y1+y2)/2; int midZ = (z1+z2)/2; BlockPos pos = new BlockPos(midX,midY,midZ); PhysicsWrapperEntity wrapper = ValkyrienWarfareMod.physicsManager.getObjectManagingPos(worldFor, pos); if(wrapper!=null&&wrapper.wrapping.renderer!=null){ wrapper.wrapping.renderer.updateRange(x1, y1, z1, x2, y2, z2); } }
		 */
	}

	public static void onNotifyBlockUpdate(World worldFor, BlockPos pos, IBlockState oldState, IBlockState newState, int flags) {
		worldFor.notifyBlockUpdate(pos, oldState, newState, flags);
	}

	public static RayTraceResult onRayTraceBlocks(World world, Vec3d vec31, Vec3d vec32, boolean stopOnLiquid, boolean ignoreBlockWithoutBoundingBox, boolean returnLastUncollidableBlock) {
		RayTraceResult vanillaTrace = world.rayTraceBlocks(vec31, vec32, stopOnLiquid, ignoreBlockWithoutBoundingBox, returnLastUncollidableBlock);
		WorldPhysObjectManager physManager = ValkyrienWarfareMod.physicsManager.getManagerForWorld(world);
		if (physManager == null) {
			return vanillaTrace;
		}
		AxisAlignedBB playerRangeBB = new AxisAlignedBB(vec31.xCoord - 1D, vec31.yCoord - 1D, vec31.zCoord - 1D, vec31.xCoord + 1D, vec31.yCoord + 1D, vec31.zCoord + 1D);
		List<PhysicsWrapperEntity> nearbyShips = physManager.getNearbyPhysObjects(playerRangeBB);
		boolean changed = false;
		Vec3d playerEyesPos = vec31;
		Vec3d playerReachVector = vec32.subtract(vec31);
		double reachDistance = playerReachVector.lengthVector();
		double worldResultDistFromPlayer = 420D;
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

	public static EntityPlayerMP onCreatePlayerForUser(PlayerList playerList, GameProfile profile) {
		// UUID uuid = EntityPlayer.getUUID(profile);
		// List<EntityPlayerMP> list = Lists.<EntityPlayerMP>newArrayList();
		//
		// for (int i = 0; i < playerList.playerEntityList.size(); ++i)
		// {
		// EntityPlayerMP entityplayermp = (EntityPlayerMP)playerList.playerEntityList.get(i);
		//
		// if (entityplayermp.getUniqueID().equals(uuid))
		// {
		// list.add(entityplayermp);
		// }
		// }
		//
		// EntityPlayerMP entityplayermp2 = (EntityPlayerMP)playerList.uuidToPlayerMap.get(profile.getId());
		//
		// if (entityplayermp2 != null && !list.contains(entityplayermp2))
		// {
		// list.add(entityplayermp2);
		// }
		//
		// for (EntityPlayerMP entityplayermp1 : list)
		// {
		// entityplayermp1.connection.kickPlayerFromServer("You logged in from another location");
		// }
		//
		// PlayerInteractionManager playerinteractionmanager;
		//
		// if (playerList.mcServer.isDemo())
		// {
		// playerinteractionmanager = new DemoWorldManager(playerList.mcServer.worldServerForDimension(0));
		// }
		// else
		// {
		// playerinteractionmanager = new CustomPlayerInteractionManager(playerList.mcServer.worldServerForDimension(0));
		// }
		//
		// return new EntityPlayerMP(playerList.mcServer, playerList.mcServer.worldServerForDimension(0), profile, playerinteractionmanager);
		return playerList.createPlayerForUser(profile);
	}

	@SuppressWarnings("unused")
	public static EntityPlayerMP onRecreatePlayerEntity(PlayerList playerList, EntityPlayerMP playerIn, int dimension, boolean conqueredEnd) {
		// World world = playerList.mcServer.worldServerForDimension(dimension);
		// if (world == null)
		// {
		// dimension = 0;
		// }
		// else if (!world.provider.canRespawnHere())
		// {
		// dimension = world.provider.getRespawnDimension(playerIn);
		// }
		//
		// playerIn.getServerWorld().getEntityTracker().removePlayerFromTrackers(playerIn);
		// playerIn.getServerWorld().getEntityTracker().untrackEntity(playerIn);
		// playerIn.getServerWorld().getPlayerChunkMap().removePlayer(playerIn);
		// playerList.playerEntityList.remove(playerIn);
		// playerList.mcServer.worldServerForDimension(playerIn.dimension).removeEntityDangerously(playerIn);
		// BlockPos blockpos = playerIn.getBedLocation(dimension);
		// boolean flag = playerIn.isSpawnForced(dimension);
		// playerIn.dimension = dimension;
		// PlayerInteractionManager playerinteractionmanager;
		//
		// if (playerList.mcServer.isDemo())
		// {
		// playerinteractionmanager = new DemoWorldManager(playerList.mcServer.worldServerForDimension(playerIn.dimension));
		// }
		// else
		// {
		// playerinteractionmanager = new PlayerInteractionManager(playerList.mcServer.worldServerForDimension(playerIn.dimension));
		// }
		//
		// EntityPlayerMP entityplayermp = new EntityPlayerMP(playerList.mcServer, playerList.mcServer.worldServerForDimension(playerIn.dimension), playerIn.getGameProfile(), playerinteractionmanager);
		// entityplayermp.connection = playerIn.connection;
		// entityplayermp.clonePlayer(playerIn, conqueredEnd);
		// entityplayermp.dimension = dimension;
		// entityplayermp.setEntityId(playerIn.getEntityId());
		// entityplayermp.setCommandStats(playerIn);
		// entityplayermp.setPrimaryHand(playerIn.getPrimaryHand());
		//
		// for (String s : playerIn.getTags())
		// {
		// entityplayermp.addTag(s);
		// }
		//
		// WorldServer worldserver = playerList.mcServer.worldServerForDimension(playerIn.dimension);
		//// playerList.setPlayerGameTypeBasedOnOther(entityplayermp, playerIn, worldserver);
		//
		//
		//
		// if (playerIn != null)
		// {
		// entityplayermp.interactionManager.setGameType(playerIn.interactionManager.getGameType());
		// }
		// else if (playerList.gameType != null)
		// {
		// entityplayermp.interactionManager.setGameType(playerList.gameType);
		// }
		//
		// entityplayermp.interactionManager.initializeGameType(worldserver.getWorldInfo().getGameType());
		//
		//
		// if (blockpos != null)
		// {
		// BlockPos blockpos1 = EntityPlayer.getBedSpawnLocation(playerList.mcServer.worldServerForDimension(playerIn.dimension), blockpos, flag);
		//
		// if (blockpos1 != null)
		// {
		// entityplayermp.setLocationAndAngles((double)((float)blockpos1.getX() + 0.5F), (double)((float)blockpos1.getY() + 0.1F), (double)((float)blockpos1.getZ() + 0.5F), 0.0F, 0.0F);
		// entityplayermp.setSpawnPoint(blockpos, flag);
		// }
		// else
		// {
		// entityplayermp.connection.sendPacket(new SPacketChangeGameState(0, 0.0F));
		// }
		// }
		//
		// worldserver.getChunkProvider().provideChunk((int)entityplayermp.posX >> 4, (int)entityplayermp.posZ >> 4);
		//
		// while (!worldserver.getCollisionBoxes(entityplayermp, entityplayermp.getEntityBoundingBox()).isEmpty() && entityplayermp.posY < 256.0D)
		// {
		// entityplayermp.setPosition(entityplayermp.posX, entityplayermp.posY + 1.0D, entityplayermp.posZ);
		// }
		//
		// entityplayermp.connection.sendPacket(new SPacketRespawn(entityplayermp.dimension, entityplayermp.worldObj.getDifficulty(), entityplayermp.worldObj.getWorldInfo().getTerrainType(), entityplayermp.interactionManager.getGameType()));
		// BlockPos blockpos2 = worldserver.getSpawnPoint();
		// entityplayermp.connection.setPlayerLocation(entityplayermp.posX, entityplayermp.posY, entityplayermp.posZ, entityplayermp.rotationYaw, entityplayermp.rotationPitch);
		// entityplayermp.connection.sendPacket(new SPacketSpawnPosition(blockpos2));
		// entityplayermp.connection.sendPacket(new SPacketSetExperience(entityplayermp.experience, entityplayermp.experienceTotal, entityplayermp.experienceLevel));
		// playerList.updateTimeAndWeatherForPlayer(entityplayermp, worldserver);
		// playerList.updatePermissionLevel(entityplayermp);
		// worldserver.getPlayerChunkMap().addPlayer(entityplayermp);
		// worldserver.spawnEntityInWorld(entityplayermp);
		// playerList.playerEntityList.add(entityplayermp);
		// playerList.uuidToPlayerMap.put(entityplayermp.getUniqueID(), entityplayermp);
		// entityplayermp.addSelfToInternalCraftingInventory();
		// entityplayermp.setHealth(entityplayermp.getHealth());
		// net.minecraftforge.fml.common.FMLCommonHandler.instance().firePlayerRespawnEvent(entityplayermp);
		// return entityplayermp;
		return playerList.recreatePlayerEntity(playerIn, dimension, conqueredEnd);
	}

	public static void onEntityMove(Entity entity, double dx, double dy, double dz) {
		if (!EntityCollisionInjector.alterEntityMovement(entity, dx, dy, dz)) {
			entity.moveEntity(dx, dy, dz);
		}
	}

	public static void onEntityRemoved(World world, Entity removed) {
		if (removed instanceof PhysicsWrapperEntity) {
			ValkyrienWarfareMod.physicsManager.onShipUnload((PhysicsWrapperEntity) removed);
		}
		world.onEntityRemoved(removed);
	}

	public static void onEntityAdded(World world, Entity added) {
		if (added instanceof PhysicsWrapperEntity) {
			ValkyrienWarfareMod.physicsManager.onShipLoad((PhysicsWrapperEntity) added);
		}
		world.onEntityAdded(added);
	}

	public static void onChunkUnload(ChunkProviderServer provider, Chunk chunk) {
		if (!ValkyrienWarfareMod.chunkManager.isChunkInShipRange(provider.worldObj, chunk.xPosition, chunk.zPosition)) {
			if (!chunk.worldObj.isSpawnChunk(chunk.xPosition, chunk.zPosition)) {
				for (int i = 0; i < chunk.entityLists.length; ++i) {
					Collection<Entity> c = chunk.entityLists[i];
					for (Entity entity : c) {
						if (entity instanceof PhysicsWrapperEntity) {
							ValkyrienWarfareMod.physicsManager.getManagerForWorld(entity.worldObj).physicsEntitiesToUnload.add((PhysicsWrapperEntity) entity);
						}
					}
				}
			}
			provider.unload(chunk);
		}
	}

}
