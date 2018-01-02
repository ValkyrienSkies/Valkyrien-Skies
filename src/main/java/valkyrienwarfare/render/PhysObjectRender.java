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

package valkyrienwarfare.render;

import net.minecraft.block.material.Material;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.RenderGlobal;
import net.minecraft.client.renderer.entity.Render;
import net.minecraft.client.renderer.entity.RenderManager;
import net.minecraft.client.renderer.texture.TextureMap;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.RayTraceResult;
import valkyrienwarfare.physicsmanagement.PhysicsWrapperEntity;

/**
 * This class does nothing; on purpose
 *
 * @author thebest108
 */
public class PhysObjectRender extends Render<PhysicsWrapperEntity> {

	public static double renderX;
	public static double renderY;
	public static double renderZ;
	public static double renderYaw;

	public PhysObjectRender(RenderManager renderManager) {
		super(renderManager);
	}

	@Override
	public void doRender(PhysicsWrapperEntity wrapper, double x, double y, double z, float entityYaw, float partialTicks) {

		/*

		GL11.glPushMatrix();
		wrapper.wrapping.renderer.setupTranslation(partialTicks);

		GL11.glLineWidth(2.5f);
		GL11.glColor3d(1.0, 0.0, 0.0);
		GL11.glBegin(GL11.GL_LINES);
		GL11.glVertex3d(0, 0, 0D);
		GL11.glVertex3d(0, 1, 0);
		GL11.glEnd();

		IDraggable draggable = EntityDraggable.getDraggableFromEntity(Minecraft.getMinecraft().player);

//		partialTicks *= -1;

//		Minecraft.getMinecraft().player.posX += draggable.getVelocityAddedToPlayer().X * partialTicks;
//		Minecraft.getMinecraft().player.posY += draggable.getVelocityAddedToPlayer().Y * partialTicks;
//		Minecraft.getMinecraft().player.posZ += draggable.getVelocityAddedToPlayer().Z * partialTicks;
//		Minecraft.getMinecraft().player.prevPosX += draggable.getVelocityAddedToPlayer().X * partialTicks;
//		Minecraft.getMinecraft().player.prevPosY += draggable.getVelocityAddedToPlayer().Y * partialTicks;
//		Minecraft.getMinecraft().player.prevPosZ += draggable.getVelocityAddedToPlayer().Z * partialTicks;
//		Minecraft.getMinecraft().player.rotationYaw += draggable.getYawDifVelocity() * partialTicks;

        Minecraft.getMinecraft().entityRenderer.getMouseOver(partialTicks);

//        Minecraft.getMinecraft().player.posX -= draggable.getVelocityAddedToPlayer().X * partialTicks;
//		Minecraft.getMinecraft().player.posY -= draggable.getVelocityAddedToPlayer().Y * partialTicks;
//		Minecraft.getMinecraft().player.posZ -= draggable.getVelocityAddedToPlayer().Z * partialTicks;
//		Minecraft.getMinecraft().player.prevPosX -= draggable.getVelocityAddedToPlayer().X * partialTicks;
//		Minecraft.getMinecraft().player.prevPosY -= draggable.getVelocityAddedToPlayer().Y * partialTicks;
//		Minecraft.getMinecraft().player.prevPosZ -= draggable.getVelocityAddedToPlayer().Z * partialTicks;
//		Minecraft.getMinecraft().player.rotationYaw -= draggable.getYawDifVelocity() * partialTicks;

//		partialTicks *= -1;

        RayTraceResult movingObjectPositionIn = Minecraft.getMinecraft().objectMouseOver;

        Tessellator tessellator = Tessellator.getInstance();
        BufferBuilder BufferBuilder = tessellator.getBuffer();

        EntityPlayer player = Minecraft.getMinecraft().player;

        double xOff = wrapper.wrapping.renderer.offsetPos.getX();
        double yOff = wrapper.wrapping.renderer.offsetPos.getY();
        double zOff = wrapper.wrapping.renderer.offsetPos.getZ();

        double oldX = BufferBuilder.xOffset;
        double oldY = BufferBuilder.yOffset;
        double oldZ = BufferBuilder.zOffset;


        BufferBuilder.xOffset = -xOff;
        BufferBuilder.yOffset = -yOff;
        BufferBuilder.zOffset = -zOff;

//        GL11.glTranslated(-movingObjectPositionIn.getBlockPos().x, -movingObjectPositionIn.getBlockPos().y, -movingObjectPositionIn.getBlockPos().z);
        this.drawSelectionBoxOriginal(player, movingObjectPositionIn, 0, partialTicks);

        BufferBuilder.xOffset = oldX;
        BufferBuilder.yOffset = oldY;
        BufferBuilder.zOffset = oldZ;

//        wrapper.wrapping.renderer.inverseTransform(partialTicks);
		GL11.glPopMatrix();
		*/

		if (this.canRenderName(wrapper)) {
			this.renderLivingLabel(wrapper, wrapper.getDisplayName().getFormattedText(), x, y, z, 64);
		}
	}

	public void drawSelectionBoxOriginal(EntityPlayer player, RayTraceResult movingObjectPositionIn, int execute, float partialTicks) {
		if (execute == 0 && movingObjectPositionIn.typeOfHit == RayTraceResult.Type.BLOCK) {
			GlStateManager.enableBlend();
			GlStateManager.tryBlendFuncSeparate(GlStateManager.SourceFactor.SRC_ALPHA, GlStateManager.DestFactor.ONE_MINUS_SRC_ALPHA, GlStateManager.SourceFactor.ONE, GlStateManager.DestFactor.ZERO);
			GlStateManager.glLineWidth(2.0F);
			GlStateManager.disableTexture2D();
			GlStateManager.depthMask(false);
			BlockPos blockpos = movingObjectPositionIn.getBlockPos();
			IBlockState iblockstate = player.world.getBlockState(blockpos);

			if (iblockstate.getMaterial() != Material.AIR && player.world.getWorldBorder().contains(blockpos)) {
				double d0 = 0;// player.lastTickPosX + (player.posX - player.lastTickPosX) * (double) partialTicks;
				double d1 = 0;//player.lastTickPosY + (player.posY - player.lastTickPosY) * (double) partialTicks;
				double d2 = 0;//player.lastTickPosZ + (player.posZ - player.lastTickPosZ) * (double) partialTicks;
				RenderGlobal.drawSelectionBoundingBox(iblockstate.getSelectedBoundingBox(player.world, blockpos).expand(0.0020000000949949026D, 0.0020000000949949026D, 0.0020000000949949026D).offset(-d0, -d1, -d2), 0.0F, 0.0F, 0.0F, 0.4F);
			}

			GlStateManager.depthMask(true);
			GlStateManager.enableTexture2D();
			GlStateManager.disableBlend();
		}
	}

	@Override
	protected ResourceLocation getEntityTexture(PhysicsWrapperEntity entity) {
		return TextureMap.LOCATION_BLOCKS_TEXTURE;
	}

}
