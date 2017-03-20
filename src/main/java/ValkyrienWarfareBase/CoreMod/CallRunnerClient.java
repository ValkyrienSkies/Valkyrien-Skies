package ValkyrienWarfareBase.CoreMod;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.lwjgl.opengl.GL11;

import ValkyrienWarfareBase.ValkyrienWarfareMod;
import ValkyrienWarfareBase.API.RotationMatrices;
import ValkyrienWarfareBase.API.Vector;
import ValkyrienWarfareBase.Collision.Polygon;
import ValkyrienWarfareBase.Math.Quaternion;
import ValkyrienWarfareBase.PhysicsManagement.PhysicsWrapperEntity;
import ValkyrienWarfareBase.PhysicsManagement.WorldPhysObjectManager;
import ValkyrienWarfareBase.Proxy.ClientProxy;
import ValkyrienWarfareControl.Piloting.ClientPilotingManager;
import net.minecraft.block.Block;
import net.minecraft.block.BlockChest;
import net.minecraft.block.BlockEnderChest;
import net.minecraft.block.BlockSign;
import net.minecraft.block.BlockSkull;
import net.minecraft.block.material.Material;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.Minecraft;
import net.minecraft.client.particle.Particle;
import net.minecraft.client.particle.ParticleManager;
import net.minecraft.client.renderer.ActiveRenderInfo;
import net.minecraft.client.renderer.BlockRendererDispatcher;
import net.minecraft.client.renderer.DestroyBlockProgress;
import net.minecraft.client.renderer.EntityRenderer;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.RenderGlobal;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.VertexBuffer;
import net.minecraft.client.renderer.culling.ICamera;
import net.minecraft.client.renderer.entity.RenderManager;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.renderer.texture.TextureMap;
import net.minecraft.client.renderer.tileentity.TileEntityRendererDispatcher;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.passive.EntityAnimal;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.BlockRenderLayer;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.RayTraceResult;
import net.minecraft.util.math.RayTraceResult.Type;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.EnumSkyBlock;
import net.minecraft.world.World;

public class CallRunnerClient extends CallRunner {

    public static AxisAlignedBB getRenderBoundingBox(TileEntity tile){
    	AxisAlignedBB toReturn = tile.getRenderBoundingBox();
    	BlockPos pos = tile.getPos();
    	PhysicsWrapperEntity wrapper = ValkyrienWarfareMod.physicsManager.getObjectManagingPos(Minecraft.getMinecraft().theWorld, pos);
    	if(wrapper != null){
    		Polygon inWorldPoly = new Polygon(toReturn, wrapper.wrapping.coordTransform.lToWTransform);
    		return inWorldPoly.getEnclosedAABB();
    	}
    	return toReturn;
    }

	public static void onOrientCamera(EntityRenderer renderer, float partialTicks) {
		Entity entity = renderer.mc.getRenderViewEntity();
		float f = entity.getEyeHeight();
		double d0 = entity.prevPosX + (entity.posX - entity.prevPosX) * (double) partialTicks;
		double d1 = entity.prevPosY + (entity.posY - entity.prevPosY) * (double) partialTicks;
		double d2 = entity.prevPosZ + (entity.posZ - entity.prevPosZ) * (double) partialTicks;

		Vector eyeVector = new Vector(0D, f, 0D);

		PhysicsWrapperEntity fixedOnto = ValkyrienWarfareMod.physicsManager.getShipFixedOnto(entity);
		if (fixedOnto != null) {
			Quaternion orientationQuat = fixedOnto.wrapping.renderer.getSmoothRotationQuat(partialTicks);

			double[] radians = orientationQuat.toRadians();

			float moddedPitch = (float) Math.toDegrees(radians[0]);
			float moddedYaw = (float) Math.toDegrees(radians[1]);
			float moddedRoll = (float) Math.toDegrees(radians[2]);

			double[] orientationMatrix = RotationMatrices.getRotationMatrix(moddedPitch, moddedYaw, moddedRoll);

			RotationMatrices.applyTransform(orientationMatrix, eyeVector);

			Vector playerPosition = new Vector(fixedOnto.wrapping.getLocalPositionForEntity(entity));

			RotationMatrices.applyTransform(fixedOnto.wrapping.coordTransform.RlToWTransform, playerPosition);

			d0 = playerPosition.X;
			d1 = playerPosition.Y;
			d2 = playerPosition.Z;
		}

		d0 += eyeVector.X;
		d1 += eyeVector.Y;
		d2 += eyeVector.Z;

		if (entity instanceof EntityLivingBase && ((EntityLivingBase) entity).isPlayerSleeping()) {
			f = (float) ((double) f + 1.0D);
			GlStateManager.translate(0.0F, 0.3F, 0.0F);

			if (!renderer.mc.gameSettings.debugCamEnable) {
				BlockPos blockpos = new BlockPos(entity);
				IBlockState iblockstate = renderer.mc.theWorld.getBlockState(blockpos);
				net.minecraftforge.client.ForgeHooksClient.orientBedCamera(renderer.mc.theWorld, blockpos, iblockstate, entity);

				GlStateManager.rotate(entity.prevRotationYaw + (entity.rotationYaw - entity.prevRotationYaw) * partialTicks + 180.0F, 0.0F, -1.0F, 0.0F);
				GlStateManager.rotate(entity.prevRotationPitch + (entity.rotationPitch - entity.prevRotationPitch) * partialTicks, -1.0F, 0.0F, 0.0F);
			}
		} else if (renderer.mc.gameSettings.thirdPersonView > 0) {
			PhysicsWrapperEntity cameraEntity = ClientPilotingManager.getPilotedWrapperEntity();

			double d3 = (double) (renderer.thirdPersonDistancePrev + (4.0F - renderer.thirdPersonDistancePrev) * partialTicks);

			if (cameraEntity != null) {
				d3 = ClientPilotingManager.getThirdPersonViewDist(d3);
			}

			if (renderer.mc.gameSettings.debugCamEnable) {
				GlStateManager.translate(0.0F, 0.0F, (float) (-d3));
			} else {
				float f1 = entity.rotationYaw;
				float f2 = entity.rotationPitch;

				if (renderer.mc.gameSettings.thirdPersonView == 2) {
					f2 += 180.0F;
				}

				double d4 = (double) (-MathHelper.sin(f1 * 0.017453292F) * MathHelper.cos(f2 * 0.017453292F)) * d3;
				double d5 = (double) (MathHelper.cos(f1 * 0.017453292F) * MathHelper.cos(f2 * 0.017453292F)) * d3;
				double d6 = (double) (-MathHelper.sin(f2 * 0.017453292F)) * d3;

				for (int i = 0; i < 8; ++i) {
					float f3 = (float) ((i & 1) * 2 - 1);
					float f4 = (float) ((i >> 1 & 1) * 2 - 1);
					float f5 = (float) ((i >> 2 & 1) * 2 - 1);
					f3 = f3 * 0.1F;
					f4 = f4 * 0.1F;
					f5 = f5 * 0.1F;

					// Theres some sort of strange java bug here; I'll just avoid calling the method and inline it myself!
					// RayTraceResult raytraceresult = PilotShipManager.rayTraceExcludingWrapper(renderer.mc.theWorld,new Vec3d(d0 + (double)f3, d1 + (double)f4, d2 + (double)f5), new Vec3d(d0 - d4 + (double)f3 + (double)f5, d1 - d6 + (double)f4, d2 - d5 + (double)f5),false,false,false,cameraEntity);

					RayTraceResult raytraceresult;

					if (ClientPilotingManager.getPilotedWrapperEntity() == null) {
						raytraceresult = CallRunner.onRayTraceBlocks(Minecraft.getMinecraft().theWorld, new Vec3d(d0 + (double) f3, d1 + (double) f4, d2 + (double) f5), new Vec3d(d0 - d4 + (double) f3 + (double) f5, d1 - d6 + (double) f4, d2 - d5 + (double) f5), false, false, false);
					} else {
						Vec3d vec31 = new Vec3d(d0 + (double) f3, d1 + (double) f4, d2 + (double) f5);
						Vec3d vec32 = new Vec3d(d0 - d4 + (double) f3 + (double) f5, d1 - d6 + (double) f4, d2 - d5 + (double) f5);

						raytraceresult = renderer.mc.theWorld.rayTraceBlocks(vec31, vec32, false, false, false);
						WorldPhysObjectManager physManager = ValkyrienWarfareMod.physicsManager.getManagerForWorld(renderer.mc.theWorld);
						AxisAlignedBB playerRangeBB = new AxisAlignedBB(vec31.xCoord - 1D, vec31.yCoord - 1D, vec31.zCoord - 1D, vec31.xCoord + 1D, vec31.yCoord + 1D, vec31.zCoord + 1D);
						List<PhysicsWrapperEntity> nearbyShips = physManager.getNearbyPhysObjects(playerRangeBB);
						boolean changed = false;
						Vec3d playerEyesPos = vec31;
						Vec3d playerReachVector = vec32.subtract(vec31);
						double reachDistance = playerReachVector.lengthVector();
						double worldResultDistFromPlayer = 420D;
						if (raytraceresult != null && raytraceresult.hitVec != null) {
							worldResultDistFromPlayer = raytraceresult.hitVec.distanceTo(vec31);
						}

						for (PhysicsWrapperEntity wrapper : nearbyShips) {
							if (ClientPilotingManager.getPilotedWrapperEntity() != null && wrapper.getEntityId() != ClientPilotingManager.getPilotedWrapperEntity().getEntityId()) {
								playerEyesPos = vec31;
								playerReachVector = vec32.subtract(vec31);

								// Transform the coordinate system for the player eye pos
								playerEyesPos = RotationMatrices.applyTransform(wrapper.wrapping.coordTransform.RwToLTransform, playerEyesPos);
								playerReachVector = RotationMatrices.applyTransform(wrapper.wrapping.coordTransform.RwToLRotation, playerReachVector);
								Vec3d playerEyesReachAdded = playerEyesPos.addVector(playerReachVector.xCoord * reachDistance, playerReachVector.yCoord * reachDistance, playerReachVector.zCoord * reachDistance);
								RayTraceResult resultInShip = renderer.mc.theWorld.rayTraceBlocks(playerEyesPos, playerEyesReachAdded, false, false, false);
								if (resultInShip != null && resultInShip.hitVec != null && resultInShip.typeOfHit == Type.BLOCK) {
									double shipResultDistFromPlayer = resultInShip.hitVec.distanceTo(playerEyesPos);
									if (shipResultDistFromPlayer < worldResultDistFromPlayer) {
										worldResultDistFromPlayer = shipResultDistFromPlayer;
										resultInShip.hitVec = RotationMatrices.applyTransform(wrapper.wrapping.coordTransform.RlToWTransform, resultInShip.hitVec);
										raytraceresult = resultInShip;
									}
								}
							}
						}

					}

					if (raytraceresult != null) {
						double d7 = raytraceresult.hitVec.distanceTo(new Vec3d(d0, d1, d2));

						if (d7 < d3) {
							d3 = d7;
						}
					}
				}

				if (renderer.mc.gameSettings.thirdPersonView == 2) {
					GlStateManager.rotate(180.0F, 0.0F, 1.0F, 0.0F);
				}

				GlStateManager.rotate(entity.rotationPitch - f2, 1.0F, 0.0F, 0.0F);
				GlStateManager.rotate(entity.rotationYaw - f1, 0.0F, 1.0F, 0.0F);
				GlStateManager.translate(0.0F, 0.0F, (float) (-d3));
				GlStateManager.rotate(f1 - entity.rotationYaw, 0.0F, 1.0F, 0.0F);
				GlStateManager.rotate(f2 - entity.rotationPitch, 1.0F, 0.0F, 0.0F);
			}
		} else {
			GlStateManager.translate(0.0F, 0.0F, 0.05F);
		}

		if (!renderer.mc.gameSettings.debugCamEnable) {
			float yaw = entity.prevRotationYaw + (entity.rotationYaw - entity.prevRotationYaw) * partialTicks + 180.0F;
			float pitch = entity.prevRotationPitch + (entity.rotationPitch - entity.prevRotationPitch) * partialTicks;
			float roll = 0.0F;
			if (entity instanceof EntityAnimal) {
				EntityAnimal entityanimal = (EntityAnimal) entity;
				yaw = entityanimal.prevRotationYawHead + (entityanimal.rotationYawHead - entityanimal.prevRotationYawHead) * partialTicks + 180.0F;
			}
			IBlockState state = ActiveRenderInfo.getBlockStateAtEntityViewpoint(renderer.mc.theWorld, entity, partialTicks);
			net.minecraftforge.client.event.EntityViewRenderEvent.CameraSetup event = new net.minecraftforge.client.event.EntityViewRenderEvent.CameraSetup(renderer, entity, state, partialTicks, yaw, pitch, roll);
			net.minecraftforge.common.MinecraftForge.EVENT_BUS.post(event);

			GlStateManager.rotate(event.getRoll(), 0.0F, 0.0F, 1.0F);
			GlStateManager.rotate(event.getPitch(), 1.0F, 0.0F, 0.0F);
			GlStateManager.rotate(event.getYaw(), 0.0F, 1.0F, 0.0F);

			if (fixedOnto != null) {
				Quaternion orientationQuat = fixedOnto.wrapping.renderer.getSmoothRotationQuat(partialTicks);

				double[] radians = orientationQuat.toRadians();

				float moddedPitch = (float) Math.toDegrees(radians[0]);
				float moddedYaw = (float) Math.toDegrees(radians[1]);
				float moddedRoll = (float) Math.toDegrees(radians[2]);

				GlStateManager.rotate(-moddedRoll, 0.0F, 0.0F, 1.0F);
				GlStateManager.rotate(-moddedYaw, 0.0F, 1.0F, 0.0F);
				GlStateManager.rotate(-moddedPitch, 1.0F, 0.0F, 0.0F);
			}
		}

		GL11.glTranslated(-eyeVector.X, -eyeVector.Y, -eyeVector.Z);
		d0 = entity.prevPosX + (entity.posX - entity.prevPosX) * (double) partialTicks;
		d1 = entity.prevPosY + (entity.posY - entity.prevPosY) * (double) partialTicks + (double) f;
		d2 = entity.prevPosZ + (entity.posZ - entity.prevPosZ) * (double) partialTicks;
		renderer.cloudFog = renderer.mc.renderGlobal.hasCloudFog(d0, d1, d2, partialTicks);
	}

	// TODO: This may become a performance issue
	public static int onGetCombinedLight(World world, BlockPos pos, int lightValue) {
		try {
			int i = world.getLightFromNeighborsFor(EnumSkyBlock.SKY, pos);
			int j = world.getLightFromNeighborsFor(EnumSkyBlock.BLOCK, pos);
			AxisAlignedBB lightBB = new AxisAlignedBB(pos.getX() - 2, pos.getY() - 2, pos.getZ() - 2, pos.getX() + 2, pos.getY() + 2, pos.getZ() + 2);
			List<PhysicsWrapperEntity> physEnts = ValkyrienWarfareMod.physicsManager.getManagerForWorld(world).getNearbyPhysObjects(lightBB);

			for (PhysicsWrapperEntity physEnt : physEnts) {
				BlockPos posInLocal = RotationMatrices.applyTransform(physEnt.wrapping.coordTransform.wToLTransform, pos);
				int localI = world.getLightFromNeighborsFor(EnumSkyBlock.SKY, posInLocal);
				int localJ = world.getLightFromNeighborsFor(EnumSkyBlock.BLOCK, posInLocal);
				if (localI == 0 && localJ == 0) {
					localI = world.getLightFromNeighborsFor(EnumSkyBlock.SKY, posInLocal.up());
					localJ = world.getLightFromNeighborsFor(EnumSkyBlock.BLOCK, posInLocal.up());
				}
				if (localI == 0 && localJ == 0) {
					localI = world.getLightFromNeighborsFor(EnumSkyBlock.SKY, posInLocal.down());
					localJ = world.getLightFromNeighborsFor(EnumSkyBlock.BLOCK, posInLocal.down());
				}
				if (localI == 0 && localJ == 0) {
					localI = world.getLightFromNeighborsFor(EnumSkyBlock.SKY, posInLocal.north());
					localJ = world.getLightFromNeighborsFor(EnumSkyBlock.BLOCK, posInLocal.north());
				}
				if (localI == 0 && localJ == 0) {
					localI = world.getLightFromNeighborsFor(EnumSkyBlock.SKY, posInLocal.south());
					localJ = world.getLightFromNeighborsFor(EnumSkyBlock.BLOCK, posInLocal.south());
				}
				if (localI == 0 && localJ == 0) {
					localI = world.getLightFromNeighborsFor(EnumSkyBlock.SKY, posInLocal.east());
					localJ = world.getLightFromNeighborsFor(EnumSkyBlock.BLOCK, posInLocal.east());
				}
				if (localI == 0 && localJ == 0) {
					localI = world.getLightFromNeighborsFor(EnumSkyBlock.SKY, posInLocal.west());
					localJ = world.getLightFromNeighborsFor(EnumSkyBlock.BLOCK, posInLocal.west());
				}

				i = Math.min(localI, i);
				j = Math.max(localJ, j);
			}

			if (j < lightValue) {
				j = lightValue;
			}

			return i << 20 | j << 4;

		} catch (Exception e) {
			System.err.println("Something just went wrong here!!!!");
			e.printStackTrace();
			try{
				return world.getCombinedLight(pos, lightValue);
			}catch(Exception ee){
				ee.printStackTrace();
				return 0;
			}
		}
	}

	public static void onAddEffect(ParticleManager manager, Particle effect) {
		BlockPos pos = new BlockPos(effect.posX, effect.posY, effect.posZ);
		PhysicsWrapperEntity wrapper = ValkyrienWarfareMod.physicsManager.getObjectManagingPos(effect.worldObj, pos);
		if (wrapper != null) {
			Vector posVec = new Vector(effect.posX, effect.posY, effect.posZ);
			Vector velocity = new Vector(effect.motionX,effect.motionY,effect.motionZ);
			wrapper.wrapping.coordTransform.fromLocalToGlobal(posVec);
			RotationMatrices.applyTransform(wrapper.wrapping.coordTransform.lToWRotation, velocity);
			effect.setPosition(posVec.X, posVec.Y, posVec.Z);
			effect.motionX = velocity.X;effect.motionY = velocity.Y;effect.motionZ = velocity.Z;
		}
		manager.addEffect(effect);
	}

	public static void onDrawBlockDamageTexture(RenderGlobal renderGlobal, Tessellator tessellatorIn, VertexBuffer worldRendererIn, Entity entityIn, float partialTicks) {
		double d0 = entityIn.lastTickPosX + (entityIn.posX - entityIn.lastTickPosX) * (double) partialTicks;
		double d1 = entityIn.lastTickPosY + (entityIn.posY - entityIn.lastTickPosY) * (double) partialTicks;
		double d2 = entityIn.lastTickPosZ + (entityIn.posZ - entityIn.lastTickPosZ) * (double) partialTicks;

		if (!renderGlobal.damagedBlocks.isEmpty()) {
			renderGlobal.renderEngine.bindTexture(TextureMap.LOCATION_BLOCKS_TEXTURE);
			renderGlobal.preRenderDamagedBlocks();
			worldRendererIn.begin(7, DefaultVertexFormats.BLOCK);
			worldRendererIn.setTranslation(-d0, -d1, -d2);
			worldRendererIn.noColor();
			Iterator<DestroyBlockProgress> iterator = renderGlobal.damagedBlocks.values().iterator();

			while (iterator.hasNext()) {
				DestroyBlockProgress destroyblockprogress = (DestroyBlockProgress) iterator.next();
				BlockPos blockpos = destroyblockprogress.getPosition();
				double d3 = (double) blockpos.getX() - d0;
				double d4 = (double) blockpos.getY() - d1;
				double d5 = (double) blockpos.getZ() - d2;
				Block block = renderGlobal.theWorld.getBlockState(blockpos).getBlock();
				TileEntity te = renderGlobal.theWorld.getTileEntity(blockpos);
				boolean hasBreak = block instanceof BlockChest || block instanceof BlockEnderChest || block instanceof BlockSign || block instanceof BlockSkull;
				if (!hasBreak)
					hasBreak = te != null && te.canRenderBreaking();

				if (!hasBreak) {
					PhysicsWrapperEntity wrapper = ValkyrienWarfareMod.physicsManager.getObjectManagingPos(renderGlobal.theWorld, blockpos);
					if (wrapper == null && (d3 * d3 + d4 * d4 + d5 * d5 > 1024.0D)) {
						iterator.remove();
					} else {
						IBlockState iblockstate = renderGlobal.theWorld.getBlockState(blockpos);
						if (wrapper != null) {
							wrapper.wrapping.renderer.setupTranslation(partialTicks);
							worldRendererIn.setTranslation(-wrapper.wrapping.renderer.offsetPos.getX(), -wrapper.wrapping.renderer.offsetPos.getY(), -wrapper.wrapping.renderer.offsetPos.getZ());
						}
						if (iblockstate.getMaterial() != Material.AIR) {
							int i = destroyblockprogress.getPartialBlockDamage();
							TextureAtlasSprite textureatlassprite = renderGlobal.destroyBlockIcons[i];
							BlockRendererDispatcher blockrendererdispatcher = renderGlobal.mc.getBlockRendererDispatcher();
							try {
								blockrendererdispatcher.renderBlockDamage(iblockstate, blockpos, textureatlassprite, renderGlobal.theWorld);
							} catch (Exception e) {
								e.printStackTrace();
							}
						}
						worldRendererIn.setTranslation(-d0, -d1, -d2);
						// TODO: Reverse the Matrix Transforms here
						if (wrapper != null) {
							tessellatorIn.draw();
							worldRendererIn.begin(7, DefaultVertexFormats.BLOCK);
							wrapper.wrapping.renderer.inverseTransform(partialTicks);
						}
					}
				}
			}

			tessellatorIn.draw();
			worldRendererIn.setTranslation(0.0D, 0.0D, 0.0D);
			renderGlobal.postRenderDamagedBlocks();
		}

	}

	public static void onDrawSelectionBox(RenderGlobal renderGlobal, EntityPlayer player, RayTraceResult movingObjectPositionIn, int execute, float partialTicks) {
		PhysicsWrapperEntity wrapper = ValkyrienWarfareMod.physicsManager.getObjectManagingPos(player.worldObj, movingObjectPositionIn.getBlockPos());
		if (wrapper != null) {;
			wrapper.wrapping.renderer.setupTranslation(partialTicks);

			Tessellator tessellator = Tessellator.getInstance();
			VertexBuffer vertexbuffer = tessellator.getBuffer();

			double xOff = (player.lastTickPosX + (player.posX - player.lastTickPosX) * (double)partialTicks) - wrapper.wrapping.renderer.offsetPos.getX();
			double yOff = (player.lastTickPosY + (player.posY - player.lastTickPosY) * (double)partialTicks) - wrapper.wrapping.renderer.offsetPos.getY();
			double zOff = (player.lastTickPosZ + (player.posZ - player.lastTickPosZ) * (double)partialTicks) - wrapper.wrapping.renderer.offsetPos.getZ();

			vertexbuffer.xOffset += xOff;
			vertexbuffer.yOffset += yOff;
			vertexbuffer.zOffset += zOff;

			renderGlobal.drawSelectionBox(player, movingObjectPositionIn, execute, partialTicks);

			vertexbuffer.xOffset -= xOff;
			vertexbuffer.yOffset -= yOff;
			vertexbuffer.zOffset -= zOff;

			wrapper.wrapping.renderer.inverseTransform(partialTicks);
		} else {
			renderGlobal.drawSelectionBox(player, movingObjectPositionIn, execute, partialTicks);
		}
	}

	public static void doRenderEntity(RenderManager manager, Entity entityIn, double x, double y, double z, float yaw, float partialTicks, boolean p_188391_10_){
		PhysicsWrapperEntity fixedOnto = ValkyrienWarfareMod.physicsManager.getShipFixedOnto(entityIn);

		if(fixedOnto != null){
			double oldPosX = entityIn.posX;
			double oldPosY = entityIn.posY;
			double oldPosZ = entityIn.posZ;

			double oldLastPosX = entityIn.lastTickPosX;
			double oldLastPosY = entityIn.lastTickPosY;
			double oldLastPosZ = entityIn.lastTickPosZ;

			Vector localPosition = fixedOnto.wrapping.getLocalPositionForEntity(entityIn);

			fixedOnto.wrapping.renderer.setupTranslation(partialTicks);

			if(localPosition != null){
				localPosition = new Vector(localPosition);

				localPosition.X -= fixedOnto.wrapping.renderer.offsetPos.getX();
				localPosition.Y -= fixedOnto.wrapping.renderer.offsetPos.getY();
				localPosition.Z -= fixedOnto.wrapping.renderer.offsetPos.getZ();

				x = entityIn.posX = entityIn.lastTickPosX = localPosition.X;
				y = entityIn.posY = entityIn.lastTickPosY = localPosition.Y;
				z = entityIn.posZ = entityIn.lastTickPosZ = localPosition.Z;

			}

			manager.doRenderEntity(entityIn, x, y, z, yaw, partialTicks, p_188391_10_);

			if(localPosition != null){
				fixedOnto.wrapping.renderer.inverseTransform(partialTicks);
			}

			entityIn.posX = oldPosX;
			entityIn.posY = oldPosY;
			entityIn.posZ = oldPosZ;

			entityIn.lastTickPosX = oldLastPosX;
			entityIn.lastTickPosY = oldLastPosY;
			entityIn.lastTickPosZ = oldLastPosZ;

		}else{
			manager.doRenderEntity(entityIn, x, y, z, yaw, partialTicks, p_188391_10_);
		}
	}

	public static void renderTileEntity(TileEntityRendererDispatcher dispatch, TileEntity tileentityIn, float partialTicks, int destroyStage){
		BlockPos pos = tileentityIn.getPos();
		PhysicsWrapperEntity wrapper = ValkyrienWarfareMod.physicsManager.getObjectManagingPos(tileentityIn.getWorld(), pos);

		if(wrapper != null && wrapper.wrapping != null && wrapper.wrapping.renderer != null){
			wrapper.wrapping.renderer.setupTranslation(partialTicks);

			double playerX = TileEntityRendererDispatcher.instance.staticPlayerX;
			double playerY = TileEntityRendererDispatcher.instance.staticPlayerY;
			double playerZ = TileEntityRendererDispatcher.instance.staticPlayerZ;

			TileEntityRendererDispatcher.instance.staticPlayerX = wrapper.wrapping.renderer.offsetPos.getX();
			TileEntityRendererDispatcher.instance.staticPlayerY = wrapper.wrapping.renderer.offsetPos.getY();
			TileEntityRendererDispatcher.instance.staticPlayerZ = wrapper.wrapping.renderer.offsetPos.getZ();

			dispatch.renderTileEntity(tileentityIn, partialTicks, destroyStage);

			TileEntityRendererDispatcher.instance.staticPlayerX = playerX;
			TileEntityRendererDispatcher.instance.staticPlayerY = playerY;
			TileEntityRendererDispatcher.instance.staticPlayerZ = playerZ;

			wrapper.wrapping.renderer.inverseTransform(partialTicks);
		}else{
			dispatch.renderTileEntity(tileentityIn, partialTicks, destroyStage);
		}
    }

	//TODO: Theres a lighting bug caused by Ships rendering TileEntities, perhaps use the RenderOverride to render them instead
	public static void onRenderEntities(RenderGlobal renderGlobal, Entity renderViewEntity, ICamera camera, float partialTicks) {
		((ClientProxy) ValkyrienWarfareMod.proxy).lastCamera = camera;

		renderGlobal.renderEntities(renderViewEntity, camera, partialTicks);
	}

	public static int onRenderBlockLayer(RenderGlobal renderer, BlockRenderLayer blockLayerIn, double partialTicks, int pass, Entity entityIn) {
		for (PhysicsWrapperEntity wrapper : ValkyrienWarfareMod.physicsManager.getManagerForWorld(renderer.theWorld).physicsEntities) {
			GL11.glPushMatrix();
			if (wrapper.wrapping.renderer != null && wrapper.wrapping.renderer.shouldRender()) {
				wrapper.wrapping.renderer.renderBlockLayer(blockLayerIn, partialTicks, pass);
			}
			GL11.glPopMatrix();
		}
		GlStateManager.resetColor();
		return renderer.renderBlockLayer(blockLayerIn, partialTicks, pass, entityIn);
	}

	public static BlockPos onGetPrecipitationHeightClient(World world, BlockPos posToCheck) {
		BlockPos pos = world.getPrecipitationHeight(posToCheck);
		// Servers shouldn't bother running this code

		Vector traceStart = new Vector(pos.getX() + .5D, Minecraft.getMinecraft().thePlayer.posY + 50D, pos.getZ() + .5D);
		Vector traceEnd = new Vector(pos.getX() + .5D, pos.getY() + .5D, pos.getZ() + .5D);

//		System.out.println(traceStart);
//		System.out.println(traceEnd);

		RayTraceResult result = CallRunner.onRayTraceBlocks(world, traceStart.toVec3d(), traceEnd.toVec3d(), true, true, false);

		if(result != null && result.typeOfHit != Type.MISS && result.getBlockPos() != null){

			PhysicsWrapperEntity wrapper = ValkyrienWarfareMod.physicsManager.getObjectManagingPos(world, result.getBlockPos());
			if(wrapper != null){
//				System.out.println("test");
				Vector blockPosVector = new Vector(result.getBlockPos().getX() + .5D, result.getBlockPos().getY() + .5D, result.getBlockPos().getZ() + .5D);
				wrapper.wrapping.coordTransform.fromLocalToGlobal(blockPosVector);
				BlockPos toReturn = new BlockPos(pos.getX(), blockPosVector.Y + .5D, pos.getZ());
				return toReturn;
			}
		}

		return pos;
	}

	public static Vec3d onGetPositionEyes(Entity entityFor, float partialTicks){
		Vec3d defaultOutput = entityFor.getPositionEyes(partialTicks);

		PhysicsWrapperEntity wrapper = ValkyrienWarfareMod.physicsManager.getShipFixedOnto(entityFor);

		if(wrapper != null){
			Vector playerPosition = new Vector(wrapper.wrapping.getLocalPositionForEntity(entityFor));

			RotationMatrices.applyTransform(wrapper.wrapping.coordTransform.RlToWTransform, playerPosition);

			Vector playerEyes = new Vector(0, entityFor.getEyeHeight(), 0);
			//Remove the original position added for the player's eyes
			RotationMatrices.applyTransform(wrapper.wrapping.coordTransform.lToWRotation, playerEyes);
			//Add the new rotate player eyes to the position
			playerPosition.add(playerEyes);
//			System.out.println("test");
			return playerPosition.toVec3d();
		}

		return defaultOutput;
	}

}
