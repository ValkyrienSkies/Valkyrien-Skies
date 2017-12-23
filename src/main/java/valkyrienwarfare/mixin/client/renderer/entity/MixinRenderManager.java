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

package valkyrienwarfare.mixin.client.renderer.entity;

import valkyrienwarfare.api.Vector;
import valkyrienwarfare.physicsmanagement.PhysicsWrapperEntity;
import valkyrienwarfare.ValkyrienWarfareMod;
import net.minecraft.block.Block;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.entity.Render;
import net.minecraft.client.renderer.entity.RenderManager;
import net.minecraft.client.renderer.texture.TextureManager;
import net.minecraft.crash.CrashReport;
import net.minecraft.crash.CrashReportCategory;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.ReportedException;
import net.minecraft.util.math.BlockPos;
import org.lwjgl.opengl.GL11;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;
import org.spongepowered.asm.mixin.Shadow;

@Mixin(RenderManager.class)
public abstract class MixinRenderManager {

	@Shadow
	public TextureManager renderEngine;
	@Shadow
	private boolean renderOutlines;
	@Shadow
	private boolean debugBoundingBox;

	@Shadow
	public <T extends Entity> Render<T> getEntityRenderObject(T entityIn) {
		return null;
	}

	@Shadow
	private void renderDebugBoundingBox(Entity entityIn, double x, double y, double z, float entityYaw, float partialTicks) {
	}

	@Overwrite
	public void doRenderEntity(Entity entityIn, double x, double y, double z, float yaw, float partialTicks, boolean p_188391_10_) {
		PhysicsWrapperEntity fixedOnto = ValkyrienWarfareMod.physicsManager.getShipFixedOnto(entityIn);

		if (fixedOnto != null) {
			double oldPosX = entityIn.posX;
			double oldPosY = entityIn.posY;
			double oldPosZ = entityIn.posZ;

			double oldLastPosX = entityIn.lastTickPosX;
			double oldLastPosY = entityIn.lastTickPosY;
			double oldLastPosZ = entityIn.lastTickPosZ;

			Vector localPosition = fixedOnto.wrapping.getLocalPositionForEntity(entityIn);

			fixedOnto.wrapping.renderer.setupTranslation(partialTicks);

			if (localPosition != null) {
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

			if (entityIn instanceof EntityPlayer) {
				EntityPlayer player = (EntityPlayer) entityIn;
				if (player.isPlayerSleeping()) {
					if (player.ridingEntity instanceof PhysicsWrapperEntity) {
						shipRidden = (PhysicsWrapperEntity) player.ridingEntity;
					}
//					shipRidden = ValkyrienWarfareMod.physicsManager.getShipFixedOnto(entityIn);

					if (shipRidden != null) {
						player.ridingEntity = null;
						makePlayerMount = true;

						//Now fix the rotation of sleeping players
						Vector playerPosInLocal = new Vector(fixedOnto.wrapping.getLocalPositionForEntity(entityIn));

						playerPosInLocal.subtract(.5D, .6875, .5);
						playerPosInLocal.roundToWhole();

						BlockPos bedPos = new BlockPos(playerPosInLocal.X, playerPosInLocal.Y, playerPosInLocal.Z);
						IBlockState state = entityIn.world.getBlockState(bedPos);

						Block block = state.getBlock();

						float angleYaw = 0;

//	                	player.setRenderOffsetForSleep(EnumFacing.SOUTH);

						if (block != null && block.isBed(state, entityIn.world, bedPos, entityIn)) {
							angleYaw = (float) (block.getBedDirection(state, entityIn.world, bedPos).getHorizontalIndex() * 90);
//	                    	angleYaw += 180;
						}
						GL11.glRotatef(angleYaw, 0, 1F, 0);
					}
				}
			}

			this.doRenderEntityOriginal(entityIn, x, y, z, yaw, partialTicks, p_188391_10_);

			if (makePlayerMount) {
				EntityPlayer player = (EntityPlayer) entityIn;

				player.ridingEntity = shipRidden;
			}

			if (localPosition != null) {
				fixedOnto.wrapping.renderer.inverseTransform(partialTicks);
			}

			entityIn.posX = oldPosX;
			entityIn.posY = oldPosY;
			entityIn.posZ = oldPosZ;

			entityIn.lastTickPosX = oldLastPosX;
			entityIn.lastTickPosY = oldLastPosY;
			entityIn.lastTickPosZ = oldLastPosZ;

		} else {
			this.doRenderEntityOriginal(entityIn, x, y, z, yaw, partialTicks, p_188391_10_);
		}
	}

	public void doRenderEntityOriginal(Entity entityIn, double x, double y, double z, float yaw, float partialTicks, boolean p_188391_10_) {
		Render<Entity> render = null;

		try {
			render = this.getEntityRenderObject(entityIn);

			if (render != null && this.renderEngine != null) {
				try {
					render.setRenderOutlines(this.renderOutlines);
					render.doRender(entityIn, x, y, z, yaw, partialTicks);
				} catch (Throwable throwable1) {
					throw new ReportedException(CrashReport.makeCrashReport(throwable1, "Rendering entity in world"));
				}

				try {
					if (!this.renderOutlines) {
						render.doRenderShadowAndFire(entityIn, x, y, z, yaw, partialTicks);
					}
				} catch (Throwable throwable2) {
					throw new ReportedException(CrashReport.makeCrashReport(throwable2, "Post-rendering entity in world"));
				}

				if (this.debugBoundingBox && !entityIn.isInvisible() && !p_188391_10_ && !Minecraft.getMinecraft().isReducedDebug()) {
					try {
						this.renderDebugBoundingBox(entityIn, x, y, z, yaw, partialTicks);
					} catch (Throwable throwable) {
						throw new ReportedException(CrashReport.makeCrashReport(throwable, "Rendering entity hitbox in world"));
					}
				}
			}
		} catch (Throwable throwable3) {
			CrashReport crashreport = CrashReport.makeCrashReport(throwable3, "Rendering entity in world");
			CrashReportCategory crashreportcategory = crashreport.makeCategory("entity being rendered");
			entityIn.addEntityCrashInfo(crashreportcategory);
			CrashReportCategory crashreportcategory1 = crashreport.makeCategory("Renderer details");
			crashreportcategory1.addCrashSection("Assigned renderer", render);
			crashreportcategory1.addCrashSection("Location", CrashReportCategory.getCoordinateInfo(x, y, z));
			crashreportcategory1.addCrashSection("Rotation", Float.valueOf(yaw));
			crashreportcategory1.addCrashSection("Delta", Float.valueOf(partialTicks));
			throw new ReportedException(crashreport);
		}
	}
}
