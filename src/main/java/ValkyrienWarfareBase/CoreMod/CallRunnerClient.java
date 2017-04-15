package ValkyrienWarfareBase.CoreMod;

import java.lang.reflect.Field;
import java.util.Iterator;
import java.util.List;

import org.lwjgl.opengl.GL11;

import ValkyrienWarfareBase.ValkyrienWarfareMod;
import ValkyrienWarfareBase.API.RotationMatrices;
import ValkyrienWarfareBase.API.Vector;
import ValkyrienWarfareBase.Collision.Polygon;
import ValkyrienWarfareBase.Math.Quaternion;
import ValkyrienWarfareBase.PhysicsManagement.PhysicsWrapperEntity;
import ValkyrienWarfareBase.Proxy.ClientProxy;
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
import net.minecraftforge.client.MinecraftForgeClient;

public class CallRunnerClient extends CallRunner {

	private static Field drawingBatchName;

	static{
		try {
			drawingBatchName = TileEntityRendererDispatcher.class.getDeclaredField("drawingBatch");
			drawingBatchName.setAccessible(true);
		} catch (Exception e) {}
	}

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

		BlockPos playerPos = new BlockPos(entity);

		PhysicsWrapperEntity wrapper = ValkyrienWarfareMod.physicsManager.getObjectManagingPos(entity.worldObj, playerPos);

//		Minecraft.getMinecraft().thePlayer.sleeping = false;

		if(wrapper != null){
			Vector playerPosNew = new Vector(entity.posX, entity.posY, entity.posZ);
			RotationMatrices.applyTransform(wrapper.wrapping.coordTransform.lToWTransform, playerPosNew);

			entity.posX = entity.prevPosX = entity.lastTickPosX = playerPosNew.X;
			entity.posY = entity.prevPosY = entity.lastTickPosY = playerPosNew.Y;
			entity.posZ = entity.prevPosZ = entity.lastTickPosZ = playerPosNew.Z;
		}

		Vector eyeVector = new Vector(0, entity.getEyeHeight(), 0);

		if (entity instanceof EntityLivingBase && ((EntityLivingBase)entity).isPlayerSleeping()){
			eyeVector.Y += .7D;
        }

		double d0 = entity.prevPosX + (entity.posX - entity.prevPosX) * (double)partialTicks;
        double d1 = entity.prevPosY + (entity.posY - entity.prevPosY) * (double)partialTicks;
        double d2 = entity.prevPosZ + (entity.posZ - entity.prevPosZ) * (double)partialTicks;

		PhysicsWrapperEntity fixedOnto = ValkyrienWarfareMod.physicsManager.getShipFixedOnto(entity);
		//Probably overkill, but this should 100% fix the crash in issue #78
		if (fixedOnto != null && fixedOnto.wrapping != null && fixedOnto.wrapping.renderer != null && fixedOnto.wrapping.renderer.offsetPos != null) {
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

//			entity.posX = entity.prevPosX = entity.lastTickPosX = d0;
//			entity.posY = entity.prevPosY = entity.lastTickPosY = d1;
//			entity.posZ = entity.prevPosZ = entity.lastTickPosZ = d2;
		}

        d0 += eyeVector.X;
        d1 += eyeVector.Y;
        d2 += eyeVector.Z;



        if (entity instanceof EntityLivingBase && ((EntityLivingBase)entity).isPlayerSleeping())
        {
//            f = (float)((double)f + 1.0D);
//            GlStateManager.translate(0.0F, 0.3F, 0.0F);

            if (!renderer.mc.gameSettings.debugCamEnable)
            {
            	//VW code starts here
                if(fixedOnto != null){
                    Vector playerPosInLocal = new Vector(fixedOnto.wrapping.getLocalPositionForEntity(entity));

                    playerPosInLocal.subtract(.5D, .6875, .5);
                    playerPosInLocal.roundToWhole();

                    BlockPos bedPos = new BlockPos(playerPosInLocal.X, playerPosInLocal.Y, playerPosInLocal.Z);
                    IBlockState state = renderer.mc.theWorld.getBlockState(bedPos);

                    Block block = state.getBlock();

                	float angleYaw = 0;

                    if (block != null && block.isBed(state, entity.worldObj, bedPos, entity)){
                    	angleYaw = (float)(block.getBedDirection(state, entity.worldObj, bedPos).getHorizontalIndex() * 90);
                    	angleYaw += 180;
                    }

                    entity.rotationYaw = entity.prevRotationYaw = angleYaw;

                    entity.rotationPitch = entity.prevRotationPitch = 0;

                }else{
                	BlockPos blockpos = new BlockPos(entity);
                    IBlockState iblockstate = renderer.mc.theWorld.getBlockState(blockpos);

                	net.minecraftforge.client.ForgeHooksClient.orientBedCamera(renderer.mc.theWorld, blockpos, iblockstate, entity);
                	GlStateManager.rotate(entity.prevRotationYaw + (entity.rotationYaw - entity.prevRotationYaw) * partialTicks + 180.0F, 0.0F, -1.0F, 0.0F);
                	GlStateManager.rotate(entity.prevRotationPitch + (entity.rotationPitch - entity.prevRotationPitch) * partialTicks, -1.0F, 0.0F, 0.0F);
                }

            }
        }
        else if (renderer.mc.gameSettings.thirdPersonView > 0)
        {
            double d3 = (double)(renderer.thirdPersonDistancePrev + (4.0F - renderer.thirdPersonDistancePrev) * partialTicks);

            if (renderer.mc.gameSettings.debugCamEnable)
            {
                GlStateManager.translate(0.0F, 0.0F, (float)(-d3));
            }
            else
            {
                float f1 = entity.rotationYaw;
                float f2 = entity.rotationPitch;

                if (renderer.mc.gameSettings.thirdPersonView == 2)
                {
                    f2 += 180.0F;
                }

                double d4 = (double)(-MathHelper.sin(f1 * 0.017453292F) * MathHelper.cos(f2 * 0.017453292F)) * d3;
                double d5 = (double)(MathHelper.cos(f1 * 0.017453292F) * MathHelper.cos(f2 * 0.017453292F)) * d3;
                double d6 = (double)(-MathHelper.sin(f2 * 0.017453292F)) * d3;

                for (int i = 0; i < 8; ++i)
                {
                    float f3 = (float)((i & 1) * 2 - 1);
                    float f4 = (float)((i >> 1 & 1) * 2 - 1);
                    float f5 = (float)((i >> 2 & 1) * 2 - 1);
                    f3 = f3 * 0.1F;
                    f4 = f4 * 0.1F;
                    f5 = f5 * 0.1F;
                    RayTraceResult raytraceresult = renderer.mc.theWorld.rayTraceBlocks(new Vec3d(d0 + (double)f3, d1 + (double)f4, d2 + (double)f5), new Vec3d(d0 - d4 + (double)f3 + (double)f5, d1 - d6 + (double)f4, d2 - d5 + (double)f5));

                    if (raytraceresult != null)
                    {
                        double d7 = raytraceresult.hitVec.distanceTo(new Vec3d(d0, d1, d2));

                        if (d7 < d3)
                        {
                            d3 = d7;
                        }
                    }
                }

                if (renderer.mc.gameSettings.thirdPersonView == 2)
                {
                    GlStateManager.rotate(180.0F, 0.0F, 1.0F, 0.0F);
                }

                GlStateManager.rotate(entity.rotationPitch - f2, 1.0F, 0.0F, 0.0F);
                GlStateManager.rotate(entity.rotationYaw - f1, 0.0F, 1.0F, 0.0F);
                GlStateManager.translate(0.0F, 0.0F, (float)(-d3));
                GlStateManager.rotate(f1 - entity.rotationYaw, 0.0F, 1.0F, 0.0F);
                GlStateManager.rotate(f2 - entity.rotationPitch, 1.0F, 0.0F, 0.0F);
            }
        }
        else
        {
            GlStateManager.translate(0.0F, 0.0F, 0.05F);
        }

        if (!renderer.mc.gameSettings.debugCamEnable)
        {
            float yaw = entity.prevRotationYaw + (entity.rotationYaw - entity.prevRotationYaw) * partialTicks + 180.0F;
            float pitch = entity.prevRotationPitch + (entity.rotationPitch - entity.prevRotationPitch) * partialTicks;
            float roll = 0.0F;
            if (entity instanceof EntityAnimal)
            {
                EntityAnimal entityanimal = (EntityAnimal)entity;
                yaw = entityanimal.prevRotationYawHead + (entityanimal.rotationYawHead - entityanimal.prevRotationYawHead) * partialTicks + 180.0F;
            }
            IBlockState state = ActiveRenderInfo.getBlockStateAtEntityViewpoint(renderer.mc.theWorld, entity, partialTicks);
            net.minecraftforge.client.event.EntityViewRenderEvent.CameraSetup event = new net.minecraftforge.client.event.EntityViewRenderEvent.CameraSetup(renderer, entity, state, partialTicks, yaw, pitch, roll);
            net.minecraftforge.common.MinecraftForge.EVENT_BUS.post(event);
            GlStateManager.rotate(event.getRoll(), 0.0F, 0.0F, 1.0F);
            GlStateManager.rotate(event.getPitch(), 1.0F, 0.0F, 0.0F);
            GlStateManager.rotate(event.getYaw(), 0.0F, 1.0F, 0.0F);
        }

        if (fixedOnto != null && fixedOnto.wrapping != null && fixedOnto.wrapping.renderer != null && fixedOnto.wrapping.renderer.offsetPos != null) {
			Quaternion orientationQuat = fixedOnto.wrapping.renderer.getSmoothRotationQuat(partialTicks);

			double[] radians = orientationQuat.toRadians();

			float moddedPitch = (float) Math.toDegrees(radians[0]);
			float moddedYaw = (float) Math.toDegrees(radians[1]);
			float moddedRoll = (float) Math.toDegrees(radians[2]);

			GlStateManager.rotate(-moddedRoll, 0.0F, 0.0F, 1.0F);
			GlStateManager.rotate(-moddedYaw, 0.0F, 1.0F, 0.0F);
			GlStateManager.rotate(-moddedPitch, 1.0F, 0.0F, 0.0F);
		}


        GlStateManager.translate(-eyeVector.X, -eyeVector.Y, -eyeVector.Z);
        d0 = entity.prevPosX + (entity.posX - entity.prevPosX) * (double)partialTicks + eyeVector.X;
        d1 = entity.prevPosY + (entity.posY - entity.prevPosY) * (double)partialTicks + eyeVector.Y;
        d2 = entity.prevPosZ + (entity.posZ - entity.prevPosZ) * (double)partialTicks + eyeVector.Z;
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
		if (wrapper != null && wrapper.wrapping != null && wrapper.wrapping.renderer != null && wrapper.wrapping.renderer.offsetPos != null) {;
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

			boolean makePlayerMount = false;
			PhysicsWrapperEntity shipRidden = null;

			if(entityIn instanceof EntityPlayer){
				EntityPlayer player = (EntityPlayer)entityIn;
				if(player.isPlayerSleeping()){
					if(player.ridingEntity instanceof PhysicsWrapperEntity){
						shipRidden = (PhysicsWrapperEntity) player.ridingEntity;
					}
//					shipRidden = ValkyrienWarfareMod.physicsManager.getShipFixedOnto(entityIn);

					if(shipRidden != null){
						player.ridingEntity = null;
						makePlayerMount = true;

						//Now fix the rotation of sleeping players
						Vector playerPosInLocal = new Vector(fixedOnto.wrapping.getLocalPositionForEntity(entityIn));

	                    playerPosInLocal.subtract(.5D, .6875, .5);
	                    playerPosInLocal.roundToWhole();

	                    BlockPos bedPos = new BlockPos(playerPosInLocal.X, playerPosInLocal.Y, playerPosInLocal.Z);
	                    IBlockState state = entityIn.worldObj.getBlockState(bedPos);

	                    Block block = state.getBlock();

	                	float angleYaw = 0;

//	                	player.setRenderOffsetForSleep(EnumFacing.SOUTH);

	                    if (block != null && block.isBed(state, entityIn.worldObj, bedPos, entityIn)){
	                    	angleYaw = (float)(block.getBedDirection(state, entityIn.worldObj, bedPos).getHorizontalIndex() * 90);
//	                    	angleYaw += 180;
	                    }
	                    GL11.glRotatef(angleYaw, 0, 1F, 0);
					}
				}
			}

			manager.doRenderEntity(entityIn, x, y, z, yaw, partialTicks, p_188391_10_);

			if(makePlayerMount){
				EntityPlayer player = (EntityPlayer)entityIn;

				player.ridingEntity = shipRidden;
			}

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
			try{
				boolean drawingBatchOrig = drawingBatchName.getBoolean(dispatch);

				if(drawingBatchOrig){
					dispatch.drawBatch(MinecraftForgeClient.getRenderPass());
					dispatch.preDrawBatch();
				}

				wrapper.wrapping.renderer.setupTranslation(partialTicks);

				double playerX = TileEntityRendererDispatcher.instance.staticPlayerX;
				double playerY = TileEntityRendererDispatcher.instance.staticPlayerY;
				double playerZ = TileEntityRendererDispatcher.instance.staticPlayerZ;

				TileEntityRendererDispatcher.instance.staticPlayerX = wrapper.wrapping.renderer.offsetPos.getX();
				TileEntityRendererDispatcher.instance.staticPlayerY = wrapper.wrapping.renderer.offsetPos.getY();
				TileEntityRendererDispatcher.instance.staticPlayerZ = wrapper.wrapping.renderer.offsetPos.getZ();

				if(drawingBatchOrig){
					dispatch.renderTileEntity(tileentityIn, partialTicks, destroyStage);
					dispatch.drawBatch(MinecraftForgeClient.getRenderPass());
					dispatch.preDrawBatch();
				}else{
					dispatch.renderTileEntity(tileentityIn, partialTicks, destroyStage);
				}
				TileEntityRendererDispatcher.instance.staticPlayerX = playerX;
				TileEntityRendererDispatcher.instance.staticPlayerY = playerY;
				TileEntityRendererDispatcher.instance.staticPlayerZ = playerZ;

				wrapper.wrapping.renderer.inverseTransform(partialTicks);
			}catch(Exception e){
				e.printStackTrace();
			}
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
