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

package valkyrienwarfare.mixin.client.renderer;

import com.google.common.base.Predicate;
import com.google.common.base.Predicates;
import net.minecraft.block.Block;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.ActiveRenderInfo;
import net.minecraft.client.renderer.EntityRenderer;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.item.EntityItemFrame;
import net.minecraft.entity.passive.EntityAnimal;
import net.minecraft.util.EntitySelectors;
import net.minecraft.util.math.*;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;
import org.spongepowered.asm.mixin.Shadow;
import valkyrienwarfare.ValkyrienWarfareMod;
import valkyrienwarfare.addon.control.piloting.IShipPilot;
import valkyrienwarfare.api.MixinMethods;
import valkyrienwarfare.api.RotationMatrices;
import valkyrienwarfare.api.StupidPredicate;
import valkyrienwarfare.api.Vector;
import valkyrienwarfare.math.Quaternion;
import valkyrienwarfare.physicsmanagement.PhysicsWrapperEntity;

import java.util.List;

//import valkyrienwarfare.api.MixinMethods;

@Mixin(EntityRenderer.class)
public abstract class MixinEntityRenderer {
	
	@Shadow
	@Final
	public Minecraft mc;
	
	@Shadow
	public float thirdPersonDistancePrev;
	
	@Shadow
	public boolean cloudFog;
	
	@Shadow
	public Entity pointedEntity;
	
	@Overwrite
	public void orientCamera(float partialTicks) {
		Entity entity = this.mc.getRenderViewEntity();
		
		BlockPos playerPos = new BlockPos(entity);
		
		PhysicsWrapperEntity wrapper = ValkyrienWarfareMod.physicsManager.getObjectManagingPos(entity.world, playerPos);

//		Minecraft.getMinecraft().thePlayer.sleeping = false;
		
		if (wrapper != null) {
			Vector playerPosNew = new Vector(entity.posX, entity.posY, entity.posZ);
			RotationMatrices.applyTransform(wrapper.wrapping.coordTransform.lToWTransform, playerPosNew);
			
			entity.posX = entity.prevPosX = entity.lastTickPosX = playerPosNew.X;
			entity.posY = entity.prevPosY = entity.lastTickPosY = playerPosNew.Y;
			entity.posZ = entity.prevPosZ = entity.lastTickPosZ = playerPosNew.Z;
		}
		
		Vector eyeVector = new Vector(0, entity.getEyeHeight(), 0);
		
		if (entity instanceof EntityLivingBase && ((EntityLivingBase) entity).isPlayerSleeping()) {
			eyeVector.Y += .7D;
		}
		
		double d0 = entity.prevPosX + (entity.posX - entity.prevPosX) * (double) partialTicks;
		double d1 = entity.prevPosY + (entity.posY - entity.prevPosY) * (double) partialTicks;
		double d2 = entity.prevPosZ + (entity.posZ - entity.prevPosZ) * (double) partialTicks;
		
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
		
		if (entity instanceof EntityLivingBase && ((EntityLivingBase) entity).isPlayerSleeping()) {
//            f = (float)((double)f + 1.0D);
//            GlStateManager.translate(0.0F, 0.3F, 0.0F);
			
			if (!this.mc.gameSettings.debugCamEnable) {
				//VW code starts here
				if (fixedOnto != null) {
					Vector playerPosInLocal = new Vector(fixedOnto.wrapping.getLocalPositionForEntity(entity));
					
					playerPosInLocal.subtract(.5D, .6875, .5);
					playerPosInLocal.roundToWhole();
					
					BlockPos bedPos = new BlockPos(playerPosInLocal.X, playerPosInLocal.Y, playerPosInLocal.Z);
					IBlockState state = this.mc.world.getBlockState(bedPos);
					
					Block block = state.getBlock();
					
					float angleYaw = 0;
					
					if (block != null && block.isBed(state, entity.world, bedPos, entity)) {
						angleYaw = (float) (block.getBedDirection(state, entity.world, bedPos).getHorizontalIndex() * 90);
						angleYaw += 180;
					}
					
					entity.rotationYaw = entity.prevRotationYaw = angleYaw;
					
					entity.rotationPitch = entity.prevRotationPitch = 0;
					
				} else {
					BlockPos blockpos = new BlockPos(entity);
					IBlockState iblockstate = this.mc.world.getBlockState(blockpos);
					
					net.minecraftforge.client.ForgeHooksClient.orientBedCamera(this.mc.world, blockpos, iblockstate, entity);
					GlStateManager.rotate(entity.prevRotationYaw + (entity.rotationYaw - entity.prevRotationYaw) * partialTicks + 180.0F, 0.0F, -1.0F, 0.0F);
					GlStateManager.rotate(entity.prevRotationPitch + (entity.rotationPitch - entity.prevRotationPitch) * partialTicks, -1.0F, 0.0F, 0.0F);
				}
				
			}
		} else if (this.mc.gameSettings.thirdPersonView > 0) {
			double d3 = (double) (this.thirdPersonDistancePrev + (4.0F - this.thirdPersonDistancePrev) * partialTicks);
			
			IShipPilot shipPilot = IShipPilot.class.cast(Minecraft.getMinecraft().player);
			
			if (shipPilot.isPilotingShip()) {
				//TODO: Make this number scale with the Ship
				d3 = 15D;
			}
			
			if (this.mc.gameSettings.debugCamEnable) {
				GlStateManager.translate(0.0F, 0.0F, (float) (-d3));
			} else {
				float f1 = entity.rotationYaw;
				float f2 = entity.rotationPitch;
				
				if (this.mc.gameSettings.thirdPersonView == 2) {
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
					
					IShipPilot pilot = IShipPilot.class.cast(Minecraft.getMinecraft().player);
					
					RayTraceResult raytraceresult = MixinMethods.rayTraceBlocksIgnoreShip(Minecraft.getMinecraft().world, new Vec3d(d0 + (double) f3, d1 + (double) f4, d2 + (double) f5), new Vec3d(d0 - d4 + (double) f3 + (double) f5, d1 - d6 + (double) f4, d2 - d5 + (double) f5), false, false, false, pilot.getPilotedShip());
//                    renderer.mc.theWorld.rayTraceBlocks(new Vec3d(d0 + (double)f3, d1 + (double)f4, d2 + (double)f5), new Vec3d(d0 - d4 + (double)f3 + (double)f5, d1 - d6 + (double)f4, d2 - d5 + (double)f5));
					
					if (raytraceresult != null) {
						double d7 = raytraceresult.hitVec.distanceTo(new Vec3d(d0, d1, d2));
						
						if (d7 < d3) {
							d3 = d7;
						}
					}
				}
				
				if (this.mc.gameSettings.thirdPersonView == 2) {
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
		
		if (!this.mc.gameSettings.debugCamEnable) {
			float yaw = entity.prevRotationYaw + (entity.rotationYaw - entity.prevRotationYaw) * partialTicks + 180.0F;
			float pitch = entity.prevRotationPitch + (entity.rotationPitch - entity.prevRotationPitch) * partialTicks;
			float roll = 0.0F;
			if (entity instanceof EntityAnimal) {
				EntityAnimal entityanimal = (EntityAnimal) entity;
				yaw = entityanimal.prevRotationYawHead + (entityanimal.rotationYawHead - entityanimal.prevRotationYawHead) * partialTicks + 180.0F;
			}
			IBlockState state = ActiveRenderInfo.getBlockStateAtEntityViewpoint(this.mc.world, entity, partialTicks);
			net.minecraftforge.client.event.EntityViewRenderEvent.CameraSetup event = new net.minecraftforge.client.event.EntityViewRenderEvent.CameraSetup(EntityRenderer.class.cast(this), entity, state, partialTicks, yaw, pitch, roll);
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
		d0 = entity.prevPosX + (entity.posX - entity.prevPosX) * (double) partialTicks + eyeVector.X;
		d1 = entity.prevPosY + (entity.posY - entity.prevPosY) * (double) partialTicks + eyeVector.Y;
		d2 = entity.prevPosZ + (entity.posZ - entity.prevPosZ) * (double) partialTicks + eyeVector.Z;
		this.cloudFog = this.mc.renderGlobal.hasCloudFog(d0, d1, d2, partialTicks);
	}
	
	
	@Overwrite
	public void getMouseOver(float partialTicks) {
		Entity entity = this.mc.getRenderViewEntity();
		
		if (entity != null) {
			if (this.mc.world != null) {
				this.mc.mcProfiler.startSection("pick");
				this.mc.pointedEntity = null;
				double d0 = (double) this.mc.playerController.getBlockReachDistance();
				this.mc.objectMouseOver = entity.rayTrace(d0, partialTicks);
				Vec3d vec3d = entity.getPositionEyes(partialTicks);
				boolean flag = false;
				int i = 3;
				double d1 = d0;
				
				if (this.mc.playerController.extendedReach()) {
					d1 = 6.0D;
					d0 = d1;
				} else {
					if (d0 > 3.0D) {
						flag = true;
					}
				}
				
				if (this.mc.objectMouseOver != null) {
					d1 = this.mc.objectMouseOver.hitVec.distanceTo(vec3d);
					
					PhysicsWrapperEntity wrapper = ValkyrienWarfareMod.physicsManager.getObjectManagingPos(mc.world, mc.objectMouseOver.getBlockPos());
					
					if (wrapper != null) {
						d1 = this.mc.objectMouseOver.hitVec.distanceTo(RotationMatrices.applyTransform(wrapper.wrapping.coordTransform.wToLTransform, vec3d));
					}
				}
				
				Vec3d vec3d1 = entity.getLook(1.0F);
				Vec3d vec3d2 = vec3d.addVector(vec3d1.x * d0, vec3d1.y * d0, vec3d1.z * d0);
				this.pointedEntity = null;
				Vec3d vec3d3 = null;
				float f = 1.0F;
				
				Predicate pred = new StupidPredicate();

				List<Entity> list = this.mc.world.getEntitiesInAABBexcluding(entity, entity.getEntityBoundingBox().offset(vec3d1.x * d0, vec3d1.y * d0, vec3d1.z * d0).expand(1.0D, 1.0D, 1.0D), Predicates.and(EntitySelectors.NOT_SPECTATING, pred));
				double d2 = d1;
				
				for (int j = 0; j < list.size(); ++j) {
					Entity entity1 = list.get(j);
					AxisAlignedBB axisalignedbb = entity1.getEntityBoundingBox().expandXyz((double) entity1.getCollisionBorderSize());
					RayTraceResult raytraceresult = axisalignedbb.calculateIntercept(vec3d, vec3d2);
					
					if (axisalignedbb.isVecInside(vec3d)) {
						if (d2 >= 0.0D) {
							this.pointedEntity = entity1;
							vec3d3 = raytraceresult == null ? vec3d : raytraceresult.hitVec;
							d2 = 0.0D;
						}
					} else if (raytraceresult != null) {
						double d3 = vec3d.distanceTo(raytraceresult.hitVec);
						
						if (d3 < d2 || d2 == 0.0D) {
							if (entity1.getLowestRidingEntity() == entity.getLowestRidingEntity() && !entity.canRiderInteract()) {
								if (d2 == 0.0D) {
									this.pointedEntity = entity1;
									vec3d3 = raytraceresult.hitVec;
								}
							} else {
								this.pointedEntity = entity1;
								vec3d3 = raytraceresult.hitVec;
								d2 = d3;
							}
						}
					}
				}
				
				if (this.pointedEntity != null && flag && vec3d.distanceTo(vec3d3) > 3.0D) {
					this.pointedEntity = null;
					this.mc.objectMouseOver = new RayTraceResult(RayTraceResult.Type.MISS, vec3d3, null, new BlockPos(vec3d3));
				}
				
				if (this.pointedEntity != null && (d2 < d1 || this.mc.objectMouseOver == null)) {
					this.mc.objectMouseOver = new RayTraceResult(this.pointedEntity, vec3d3);
					
					if (this.pointedEntity instanceof EntityLivingBase || this.pointedEntity instanceof EntityItemFrame) {
						this.mc.pointedEntity = this.pointedEntity;
					}
				}
				
				this.mc.mcProfiler.endSection();
			}
		}
	}
	
}
