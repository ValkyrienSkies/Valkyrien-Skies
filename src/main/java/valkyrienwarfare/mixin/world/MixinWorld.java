/*
 * Adapted from the Wizardry License
 *
 * Copyright (c) 2015-2017 the Valkyrien Warfare team
 *
 * Permission is hereby granted to any persons and/or organizations using this software to copy, modify, merge, publish, and distribute it.
 * Said persons and/or organizations are not allowed to use the software or any derivatives of the work for commercial use or any other means to generate income unless it is to be used as a part of a larger project (IE: "modpacks"), nor are they allowed to claim this software as their own.
 *
 * The persons and/or organizations are also disallowed from sub-licensing and/or trademarking this software without explicit permission from the Valkyrien Warfare team.
 *
 * Any persons and/or organizations using this software must disclose their source code and have it publicly available, include this license, provide sufficient credit to the original authors of the project (IE: The Valkyrien Warfare team), as well as provide a link to the original project.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND NON INFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
 */

package valkyrienwarfare.mixin.world;

import com.google.common.base.Predicate;
import com.google.common.collect.Lists;
import net.minecraft.block.Block;
import net.minecraft.block.material.Material;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.SoundCategory;
import net.minecraft.util.SoundEvent;
import net.minecraft.util.math.*;
import net.minecraft.world.IWorldEventListener;
import net.minecraft.world.World;
import net.minecraft.world.biome.Biome;
import net.minecraft.world.chunk.Chunk;
import org.spongepowered.asm.mixin.*;
import org.spongepowered.asm.mixin.Interface.Remap;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import valkyrienwarfare.ValkyrienWarfareMod;
import valkyrienwarfare.addon.control.nodenetwork.INodeProvider;
import valkyrienwarfare.api.RotationMatrices;
import valkyrienwarfare.api.Vector;
import valkyrienwarfare.collision.Polygon;
import valkyrienwarfare.fixes.WorldChunkloadingCrashFix;
import valkyrienwarfare.physicsmanagement.PhysicsWrapperEntity;
import valkyrienwarfare.physicsmanagement.WorldPhysObjectManager;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

@Mixin(World.class)
@Implements(@Interface(iface = WorldChunkloadingCrashFix.class, prefix = "vw$", remap = Remap.NONE))
public abstract class MixinWorld {

	private static double MAX_ENTITY_RADIUS_ALT = 2.0D;
	@Shadow
	@Final
	public boolean isRemote;
	@Shadow
	List<IWorldEventListener> eventListeners;

	private World thisClassAsWorld = World.class.cast(this);
	@Shadow
	boolean processingLoadedTiles;
	@Shadow
	List<TileEntity> addedTileEntityList;

	@Overwrite
    public void spawnParticle(int particleID, boolean ignoreRange, double x, double y, double z, double xSpeed, double ySpeed, double zSpeed, int... parameters) {
        BlockPos pos = new BlockPos(x, y, z);
        PhysicsWrapperEntity wrapper = ValkyrienWarfareMod.physicsManager.getObjectManagingPos(World.class.cast(this), pos);
		if (wrapper != null) {
            Vector newPosVec = new Vector(x, y, z);
            RotationMatrices.applyTransform(wrapper.wrapping.coordTransform.lToWTransform, newPosVec);
            x = newPosVec.X;
            y = newPosVec.Y;
            z = newPosVec.Z;
        }
		for (int i = 0; i < this.eventListeners.size(); ++i) {
            this.eventListeners.get(i).spawnParticle(particleID, ignoreRange, x, y, z, xSpeed, ySpeed, zSpeed, parameters);
        }
	}

	@Shadow
	public IBlockState getBlockState(BlockPos pos) {
		return null;
	}

	public RayTraceResult onRayTraceBlocks(Vec3d vec31, Vec3d vec32, boolean stopOnLiquid, boolean ignoreBlockWithoutBoundingBox, boolean returnLastUncollidableBlock) {
		return rayTraceBlocksIgnoreShip(vec31, vec32, stopOnLiquid, ignoreBlockWithoutBoundingBox, returnLastUncollidableBlock, null);
	}

	public RayTraceResult rayTraceBlocksIgnoreShip(Vec3d vec31, Vec3d vec32, boolean stopOnLiquid, boolean ignoreBlockWithoutBoundingBox, boolean returnLastUncollidableBlock, PhysicsWrapperEntity toIgnore) {
		RayTraceResult vanillaTrace = rayTraceBlocksOriginal(World.class.cast(this), vec31, vec32, stopOnLiquid, ignoreBlockWithoutBoundingBox, returnLastUncollidableBlock);
		WorldPhysObjectManager physManager = ValkyrienWarfareMod.physicsManager.getManagerForWorld(World.class.cast(this));
		if (physManager == null) {
			return vanillaTrace;
		}

		Vec3d playerEyesPos = vec31;
		Vec3d playerReachVector = vec32.subtract(vec31);

        AxisAlignedBB playerRangeBB = new AxisAlignedBB(vec31.x, vec31.y, vec31.z, vec32.x, vec32.y, vec32.z);

		List<PhysicsWrapperEntity> nearbyShips = physManager.getNearbyPhysObjects(playerRangeBB);
		//Get rid of the Ship that we're not supposed to be RayTracing for
		nearbyShips.remove(toIgnore);

		boolean changed = false;

		double reachDistance = playerReachVector.lengthVector();
		double worldResultDistFromPlayer = 420000000D;
		if (vanillaTrace != null && vanillaTrace.hitVec != null) {
			worldResultDistFromPlayer = vanillaTrace.hitVec.distanceTo(vec31);
		}

		PhysicsWrapperEntity transformedEntity = null;

		for (PhysicsWrapperEntity wrapper : nearbyShips) {
			playerEyesPos = vec31;
			playerReachVector = vec32.subtract(vec31);
			// TODO: Re-enable
			if (World.class.cast(this).isRemote) {
				// ValkyrienWarfareMod.proxy.updateShipPartialTicks(wrapper);
			}
			// Transform the coordinate system for the player eye pos
			playerEyesPos = RotationMatrices.applyTransform(wrapper.wrapping.coordTransform.RwToLTransform, playerEyesPos);
			playerReachVector = RotationMatrices.applyTransform(wrapper.wrapping.coordTransform.RwToLRotation, playerReachVector);
            Vec3d playerEyesReachAdded = playerEyesPos.addVector(playerReachVector.x * reachDistance, playerReachVector.y * reachDistance, playerReachVector.z * reachDistance);
            RayTraceResult resultInShip = rayTraceBlocksOriginal(World.class.cast(this), playerEyesPos, playerEyesReachAdded, stopOnLiquid, ignoreBlockWithoutBoundingBox, returnLastUncollidableBlock);
			if (resultInShip != null && resultInShip.hitVec != null && resultInShip.typeOfHit == RayTraceResult.Type.BLOCK) {
				double shipResultDistFromPlayer = resultInShip.hitVec.distanceTo(playerEyesPos);
				if (shipResultDistFromPlayer < worldResultDistFromPlayer) {
					worldResultDistFromPlayer = shipResultDistFromPlayer;
					resultInShip.hitVec = RotationMatrices.applyTransform(wrapper.wrapping.coordTransform.RlToWTransform, resultInShip.hitVec);
					vanillaTrace = resultInShip;
					transformedEntity = wrapper;
				}
			}
		}

		if (transformedEntity != null) {
			Vec3d hitVec2 = vanillaTrace.hitVec;
			hitVec2 = RotationMatrices.applyTransform(transformedEntity.wrapping.coordTransform.RwToLTransform, hitVec2);
			vanillaTrace.hitVec = hitVec2;
		}

		return vanillaTrace;
	}

	@Nullable
	public RayTraceResult rayTraceBlocksOriginal(World world, Vec3d vec31, Vec3d vec32, boolean stopOnLiquid, boolean ignoreBlockWithoutBoundingBox, boolean returnLastUncollidableBlock) {
        if (!Double.isNaN(vec31.x) && !Double.isNaN(vec31.y) && !Double.isNaN(vec31.z)) {
            if (!Double.isNaN(vec32.x) && !Double.isNaN(vec32.y) && !Double.isNaN(vec32.z)) {
                int i = MathHelper.floor(vec32.x);
                int j = MathHelper.floor(vec32.y);
                int k = MathHelper.floor(vec32.z);
                int l = MathHelper.floor(vec31.x);
                int i1 = MathHelper.floor(vec31.y);
                int j1 = MathHelper.floor(vec31.z);
                BlockPos blockpos = new BlockPos(l, i1, j1);
				IBlockState iblockstate = world.getBlockState(blockpos);
				Block block = iblockstate.getBlock();

				if ((!ignoreBlockWithoutBoundingBox || iblockstate.getCollisionBoundingBox(world, blockpos) != Block.NULL_AABB) && block.canCollideCheck(iblockstate, stopOnLiquid)) {
					RayTraceResult raytraceresult = iblockstate.collisionRayTrace(world, blockpos, vec31, vec32);

					if (raytraceresult != null) {
						return raytraceresult;
					}
				}

				RayTraceResult raytraceresult2 = null;
				int k1 = 200;

				while (k1-- >= 0) {
                    if (Double.isNaN(vec31.x) || Double.isNaN(vec31.y) || Double.isNaN(vec31.z)) {
                        return null;
					}

					if (l == i && i1 == j && j1 == k) {
						return returnLastUncollidableBlock ? raytraceresult2 : null;
					}

					boolean flag2 = true;
					boolean flag = true;
					boolean flag1 = true;
					double d0 = 999.0D;
					double d1 = 999.0D;
					double d2 = 999.0D;

					if (i > l) {
						d0 = (double) l + 1.0D;
					} else if (i < l) {
						d0 = (double) l + 0.0D;
					} else {
						flag2 = false;
					}

					if (j > i1) {
						d1 = (double) i1 + 1.0D;
					} else if (j < i1) {
						d1 = (double) i1 + 0.0D;
					} else {
						flag = false;
					}

					if (k > j1) {
						d2 = (double) j1 + 1.0D;
					} else if (k < j1) {
						d2 = (double) j1 + 0.0D;
					} else {
						flag1 = false;
					}

					double d3 = 999.0D;
					double d4 = 999.0D;
					double d5 = 999.0D;
                    double d6 = vec32.x - vec31.x;
                    double d7 = vec32.y - vec31.y;
                    double d8 = vec32.z - vec31.z;

					if (flag2) {
                        d3 = (d0 - vec31.x) / d6;
                    }

					if (flag) {
                        d4 = (d1 - vec31.y) / d7;
                    }

					if (flag1) {
                        d5 = (d2 - vec31.z) / d8;
                    }

					if (d3 == -0.0D) {
						d3 = -1.0E-4D;
					}

					if (d4 == -0.0D) {
						d4 = -1.0E-4D;
					}

					if (d5 == -0.0D) {
						d5 = -1.0E-4D;
					}

					EnumFacing enumfacing;

					if (d3 < d4 && d3 < d5) {
						enumfacing = i > l ? EnumFacing.WEST : EnumFacing.EAST;
                        vec31 = new Vec3d(d0, vec31.y + d7 * d3, vec31.z + d8 * d3);
                    } else if (d4 < d5) {
						enumfacing = j > i1 ? EnumFacing.DOWN : EnumFacing.UP;
                        vec31 = new Vec3d(vec31.x + d6 * d4, d1, vec31.z + d8 * d4);
                    } else {
						enumfacing = k > j1 ? EnumFacing.NORTH : EnumFacing.SOUTH;
                        vec31 = new Vec3d(vec31.x + d6 * d5, vec31.y + d7 * d5, d2);
                    }

                    l = MathHelper.floor(vec31.x) - (enumfacing == EnumFacing.EAST ? 1 : 0);
                    i1 = MathHelper.floor(vec31.y) - (enumfacing == EnumFacing.UP ? 1 : 0);
                    j1 = MathHelper.floor(vec31.z) - (enumfacing == EnumFacing.SOUTH ? 1 : 0);
                    blockpos = new BlockPos(l, i1, j1);
					IBlockState iblockstate1 = world.getBlockState(blockpos);
					Block block1 = iblockstate1.getBlock();

					if (!ignoreBlockWithoutBoundingBox || iblockstate1.getMaterial() == Material.PORTAL || iblockstate1.getCollisionBoundingBox(world, blockpos) != Block.NULL_AABB) {
						if (block1.canCollideCheck(iblockstate1, stopOnLiquid)) {
							RayTraceResult raytraceresult1 = iblockstate1.collisionRayTrace(world, blockpos, vec31, vec32);

							if (raytraceresult1 != null) {
								return raytraceresult1;
							}
						} else {
							raytraceresult2 = new RayTraceResult(RayTraceResult.Type.MISS, vec31, enumfacing, blockpos);
						}
					}
				}

				return returnLastUncollidableBlock ? raytraceresult2 : null;
			} else {
				return null;
			}
		} else {
			return null;
		}
	}

	@Overwrite
	public RayTraceResult rayTraceBlocks(Vec3d vec31, Vec3d vec32, boolean stopOnLiquid, boolean ignoreBlockWithoutBoundingBox, boolean returnLastUncollidableBlock) {
		return onRayTraceBlocks(vec31, vec32, stopOnLiquid, ignoreBlockWithoutBoundingBox, returnLastUncollidableBlock);
	}

	@Shadow
	protected abstract boolean isChunkLoaded(int x, int z, boolean allowEmpty);

	@Shadow
	public Chunk getChunkFromChunkCoords(int chunkX, int chunkZ) {
		return null;
	}

	public <T extends Entity> List<T> getEntitiesWithinAABBOriginal(Class<? extends T> clazz, AxisAlignedBB aabb, @Nullable Predicate<? super T> filter) {
		int i = MathHelper.floor((aabb.minX - MAX_ENTITY_RADIUS_ALT) / 16.0D);
		int j = MathHelper.ceil((aabb.maxX + MAX_ENTITY_RADIUS_ALT) / 16.0D);
		int k = MathHelper.floor((aabb.minZ - MAX_ENTITY_RADIUS_ALT) / 16.0D);
		int l = MathHelper.ceil((aabb.maxZ + MAX_ENTITY_RADIUS_ALT) / 16.0D);
		List<T> list = Lists.newArrayList();

		for (int i1 = i; i1 < j; ++i1) {
			for (int j1 = k; j1 < l; ++j1) {
				if (this.isChunkLoaded(i1, j1, true)) {
					this.getChunkFromChunkCoords(i1, j1).getEntitiesOfTypeWithinAABB(clazz, aabb, list, filter);
				}
			}
		}

		return list;
	}

	public List<Entity> getEntitiesInAABBexcludingOriginal(@Nullable Entity entityIn, AxisAlignedBB boundingBox, @Nullable Predicate<? super Entity> predicate) {
		List<Entity> list = Lists.newArrayList();
		int i = MathHelper.floor((boundingBox.minX - MAX_ENTITY_RADIUS_ALT) / 16.0D);
		int j = MathHelper.floor((boundingBox.maxX + MAX_ENTITY_RADIUS_ALT) / 16.0D);
		int k = MathHelper.floor((boundingBox.minZ - MAX_ENTITY_RADIUS_ALT) / 16.0D);
		int l = MathHelper.floor((boundingBox.maxZ + MAX_ENTITY_RADIUS_ALT) / 16.0D);

		for (int i1 = i; i1 <= j; ++i1) {
			for (int j1 = k; j1 <= l; ++j1) {
				if (this.isChunkLoaded(i1, j1, true)) {
					this.getChunkFromChunkCoords(i1, j1).getEntitiesWithinAABBForEntity(entityIn, boundingBox, list, predicate);
				}
			}
		}

		return list;
	}

	@Overwrite
	public void markBlockRangeForRenderUpdate(int x1, int y1, int z1, int x2, int y2, int z2) {
//		System.out.println((x2-x1)*(y2-y1)*(z2-z1));
//		System.out.println(x1+":"+x2+":"+y1+":"+y2+":"+z1+":"+z2);

		//Stupid OpenComputers fix, blame those assholes
		if (x2 == 1 && y1 == 0 && z2 == 1) {
			x2 = x1 + 1;
			x1--;

			y1 = y2 - 1;
			y2++;

			z2 = z1 + 1;
			z2--;
		}

		int midX = (x1 + x2) / 2;
		int midY = (y1 + y2) / 2;
		int midZ = (z1 + z2) / 2;
		BlockPos newPos = new BlockPos(midX, midY, midZ);
		PhysicsWrapperEntity wrapper = ValkyrienWarfareMod.physicsManager.getObjectManagingPos(World.class.cast(this), newPos);
		if (wrapper != null && wrapper.wrapping.renderer != null) {
			wrapper.wrapping.renderer.updateRange(x1 - 1, y1 - 1, z1 - 1, x2 + 1, y2 + 1, z2 + 1);
		}

		if (wrapper == null) {
			this.markBlockRangeForRenderUpdateOriginal(x1, y1, z1, x2, y2, z2);
		}
	}

	public void markBlockRangeForRenderUpdateOriginal(int x1, int y1, int z1, int x2, int y2, int z2) {
		for (int i = 0; i < this.eventListeners.size(); ++i) {
			this.eventListeners.get(i).markBlockRangeForRenderUpdate(x1, y1, z1, x2, y2, z2);
		}
	}

	@Overwrite
	public <T extends Entity> List<T> getEntitiesWithinAABB(Class<? extends T> clazz, AxisAlignedBB aabb, @Nullable Predicate<? super T> filter) {
		List toReturn = this.getEntitiesWithinAABBOriginal(clazz, aabb, filter);

		if (ValkyrienWarfareMod.physicsManager == null) {
			return toReturn;
		}

		BlockPos pos = new BlockPos((aabb.minX + aabb.maxX) / 2D, (aabb.minY + aabb.maxY) / 2D, (aabb.minZ + aabb.maxZ) / 2D);
		PhysicsWrapperEntity wrapper = ValkyrienWarfareMod.physicsManager.getObjectManagingPos(World.class.cast(this), pos);
		if (wrapper != null) {
			Polygon poly = new Polygon(aabb, wrapper.wrapping.coordTransform.lToWTransform);
			aabb = poly.getEnclosedAABB();//.contract(.3D);
			toReturn.addAll(this.getEntitiesWithinAABBOriginal(clazz, aabb, filter));

			toReturn.remove(wrapper);
		}
		return toReturn;
	}

	@Overwrite
	public List<Entity> getEntitiesInAABBexcluding(@Nullable Entity entityIn, AxisAlignedBB boundingBox, @Nullable Predicate<? super Entity> predicate) {
		if ((boundingBox.maxX - boundingBox.minX) * (boundingBox.maxZ - boundingBox.minZ) > 1000000D) {
			return new ArrayList();
		}

		//Prevents the players item pickup AABB from merging with a PhysicsWrapperEntity AABB
		if (entityIn instanceof EntityPlayer) {
			EntityPlayer player = (EntityPlayer) entityIn;
			if (player.isRiding() && !player.getRidingEntity().isDead && player.getRidingEntity() instanceof PhysicsWrapperEntity) {
				AxisAlignedBB axisalignedbb = player.getEntityBoundingBox().union(player.getRidingEntity().getEntityBoundingBox()).expand(1.0D, 0.0D, 1.0D);

				if (boundingBox.equals(axisalignedbb)) {
					boundingBox = player.getEntityBoundingBox().expand(1.0D, 0.5D, 1.0D);
				}
			}
		}

		List toReturn = this.getEntitiesInAABBexcludingOriginal(entityIn, boundingBox, predicate);

		BlockPos pos = new BlockPos((boundingBox.minX + boundingBox.maxX) / 2D, (boundingBox.minY + boundingBox.maxY) / 2D, (boundingBox.minZ + boundingBox.maxZ) / 2D);
		PhysicsWrapperEntity wrapper = ValkyrienWarfareMod.physicsManager.getObjectManagingPos(World.class.cast(this), pos);
		if (wrapper != null) {
			Polygon poly = new Polygon(boundingBox, wrapper.wrapping.coordTransform.lToWTransform);
			boundingBox = poly.getEnclosedAABB().contract(.3D);
			toReturn.addAll(this.getEntitiesInAABBexcludingOriginal(entityIn, boundingBox, predicate));

			toReturn.remove(wrapper);
		}
		return toReturn;
	}

	//TODO: acutally move the sound, i don't think this does anything yet
	@Inject(method = "playSound(DDDLnet/minecraft/util/SoundEvent;Lnet/minecraft/util/SoundCategory;FFZ)V", at = @At("HEAD"))
	public void prePlaySound(double x, double y, double z, SoundEvent soundIn, SoundCategory category, float volume, float pitch, boolean distanceDelay, CallbackInfo callbackInfo) {
		BlockPos pos = new BlockPos(x, y, z);
		PhysicsWrapperEntity wrapper = ValkyrienWarfareMod.physicsManager.getObjectManagingPos(World.class.cast(this), pos);
		if (wrapper != null) {
			Vector posVec = new Vector(x, y, z);
			wrapper.wrapping.coordTransform.fromLocalToGlobal(posVec);
			x = posVec.X;
			y = posVec.Y;
			z = posVec.Z;
		}
	}

	@Inject(method = "setBlockState(Lnet/minecraft/util/math/BlockPos;Lnet/minecraft/block/state/IBlockState;I)Z", at = @At("HEAD"))
	public void duringMarkAndNotifyBlock(BlockPos pos, IBlockState newState, int flags, CallbackInfoReturnable callbackInfo) {
		IBlockState oldState = this.getBlockState(pos);
		PhysicsWrapperEntity wrapper = ValkyrienWarfareMod.physicsManager.getObjectManagingPos(World.class.cast(this), pos);
		if (wrapper != null) {
			wrapper.wrapping.onSetBlockState(oldState, newState, pos);
		}
	}

	@Inject(method = "getBiome(Lnet/minecraft/util/math/BlockPos;)Lnet/minecraft/world/biome/Biome;", at = @At("HEAD"), cancellable = true)
	public void preGetBiome(final BlockPos pos, CallbackInfoReturnable<Biome> callbackInfoReturnable) {
		try {
			if (ValkyrienWarfareMod.physicsManager == null || pos == null || thisClassAsWorld == null) {
				return;
			}
			PhysicsWrapperEntity wrapper = ValkyrienWarfareMod.physicsManager.getObjectManagingPos(thisClassAsWorld, pos);
			if (wrapper != null && wrapper.wrapping != null && wrapper.wrapping.coordTransform != null && wrapper.wrapping.coordTransform.lToWTransform != null && pos != null) {
				BlockPos realPos = RotationMatrices.applyTransform(wrapper.wrapping.coordTransform.lToWTransform, pos);
				Biome toReturn = thisClassAsWorld.getBiome(realPos);
				if (toReturn != null) {
					callbackInfoReturnable.setReturnValue(toReturn);
					return;
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		//do nothing and run vanilla
	}

	@Overwrite
	public void setTileEntity(BlockPos pos, @Nullable TileEntity tileEntityIn) {
		pos = pos.toImmutable(); // Forge - prevent mutable BlockPos leaks

		if (tileEntityIn instanceof INodeProvider) {
			PhysicsWrapperEntity wrapper = ValkyrienWarfareMod.physicsManager.getObjectManagingPos(thisClassAsWorld, pos);
			if (wrapper != null) {
				((INodeProvider) tileEntityIn).getNode().updateParentEntity(wrapper.wrapping);
			}
		}

		if (!this.isOutsideBuildHeight(pos)) {
			if (tileEntityIn != null && !tileEntityIn.isInvalid()) {
				if (processingLoadedTiles) {
					tileEntityIn.setPos(pos);
					if (tileEntityIn.getWorld() != thisClassAsWorld)
						tileEntityIn.setWorld(thisClassAsWorld); // Forge - set the world early as vanilla doesn't set it until next tick
					Iterator<TileEntity> iterator = this.addedTileEntityList.iterator();

					while (iterator.hasNext()) {
						TileEntity tileentity = iterator.next();

						if (tileentity.getPos().equals(pos)) {
							tileentity.invalidate();
							iterator.remove();
						}
					}

					this.addedTileEntityList.add(tileEntityIn);
				} else {
					Chunk chunk = thisClassAsWorld.getChunkFromBlockCoords(pos);
					if (chunk != null) chunk.addTileEntity(pos, tileEntityIn);
					thisClassAsWorld.addTileEntity(tileEntityIn);
				}
			}
		}
	}

	@Intrinsic(displace = true)
	public Iterator<Chunk> vw$getPersistentChunkIterable(Iterator<Chunk> chunkIterator) {
		ArrayList<Chunk> persistantChunks = new ArrayList<Chunk>();
		while (chunkIterator.hasNext()) {
			Chunk chunk = chunkIterator.next();
			persistantChunks.add(chunk);
		}
		Iterator<Chunk> replacementIterator = persistantChunks.iterator();

		return getPersistentChunkIterable(replacementIterator);
	}

	@Shadow
	public abstract Iterator<Chunk> getPersistentChunkIterable(Iterator<Chunk> chunkIterator);

	@Shadow
	abstract boolean isOutsideBuildHeight(BlockPos pos);

}
