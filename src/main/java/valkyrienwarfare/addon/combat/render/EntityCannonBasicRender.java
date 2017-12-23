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

package valkyrienwarfare.addon.combat.render;

import net.minecraft.block.state.IBlockState;
import net.minecraft.client.renderer.BufferBuilder;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.entity.Render;
import net.minecraft.client.renderer.entity.RenderManager;
import net.minecraft.client.renderer.texture.TextureMap;
import net.minecraft.util.ResourceLocation;
import org.lwjgl.opengl.GL11;
import valkyrienwarfare.addon.combat.ValkyrienWarfareCombat;
import valkyrienwarfare.addon.combat.entity.EntityCannonBasic;
import valkyrienwarfare.render.FastBlockModelRenderer;

public class EntityCannonBasicRender extends Render<EntityCannonBasic> {

	private final IBlockState baseState, headState;

	protected EntityCannonBasicRender(RenderManager renderManager) {
		super(renderManager);
		baseState = ValkyrienWarfareCombat.INSTANCE.fakecannonblock.getStateFromMeta(0);
		headState = ValkyrienWarfareCombat.INSTANCE.fakecannonblock.getStateFromMeta(1);
	}

	@Override
	public void doRender(EntityCannonBasic entity, double x, double y, double z, float entityYaw, float partialTicks) {
		float paritalTickYaw = entity.prevRotationYaw + (entity.rotationYaw - entity.prevRotationYaw) * partialTicks;
		float paritalTickPitch = entity.prevRotationPitch + (entity.rotationPitch - entity.prevRotationPitch) * partialTicks;

		double renderYaw = -paritalTickYaw + 90f;
		double renderPitch = paritalTickPitch;

		bindTexture(TextureMap.LOCATION_BLOCKS_TEXTURE);

//		entity.posX += 15;

		Tessellator tessellator = Tessellator.getInstance();
		BufferBuilder BufferBuilder = tessellator.getBuffer();

		double oldX = BufferBuilder.xOffset;
		double oldY = BufferBuilder.yOffset;
		double oldZ = BufferBuilder.zOffset;

		GL11.glPushMatrix();

		GlStateManager.disableLighting();

		if (this.renderOutlines) {
			GlStateManager.enableColorMaterial();
			GlStateManager.enableOutlineMode(this.getTeamColor(entity));
		}

		BufferBuilder.setTranslation(0, 0, 0);

		GL11.glTranslated(x, y, z);

		double offsetAngle = entity.getBaseAngleOffset();
		GL11.glRotated(offsetAngle, 0, 1D, 0);

		GL11.glPushMatrix();

		GL11.glTranslated(-.1D, 0, 0);
		renderBase(entity, x, y, z, entityYaw, partialTicks);

		GL11.glPopMatrix();

		GL11.glTranslated(.15D, .5D, 0);

		GL11.glRotated(renderYaw - offsetAngle, 0, 1D, 0);
		GL11.glRotated(renderPitch, 0, 0, 1D);

		GL11.glTranslated(-.8D, 0, -0.25);

		GL11.glPushMatrix();
		renderHead(entity, x, y, z, entityYaw, partialTicks);
		GL11.glPopMatrix();

		if (this.renderOutlines) {
			GlStateManager.disableOutlineMode();
			GlStateManager.disableColorMaterial();
		}

		BufferBuilder.setTranslation(oldX, oldY, oldZ);
		GlStateManager.disableLighting();
		GlStateManager.resetColor();
		GlStateManager.enableLighting();

		GL11.glPopMatrix();


	}

	private void renderBase(EntityCannonBasic entity, double x, double y, double z, float entityYaw, float partialTicks) {
		Tessellator tessellator = Tessellator.getInstance();
		BufferBuilder BufferBuilder = tessellator.getBuffer();
		GL11.glPushMatrix();
		GL11.glTranslated(-0.5D, 0, -0.5D);
		FastBlockModelRenderer.renderBlockModel(BufferBuilder, tessellator, entity.world, baseState, entity.getBrightnessForRender());
		GL11.glPopMatrix();
	}

	private void renderHead(EntityCannonBasic entity, double x, double y, double z, float entityYaw, float partialTicks) {
		Tessellator tessellator = Tessellator.getInstance();
		BufferBuilder BufferBuilder = tessellator.getBuffer();
		GL11.glPushMatrix();
		GL11.glTranslated(-0.5D, 0, -0.5D);
		FastBlockModelRenderer.renderBlockModel(BufferBuilder, tessellator, entity.world, headState, entity.getBrightnessForRender());
		GL11.glPopMatrix();
	}

	@Override
	protected ResourceLocation getEntityTexture(EntityCannonBasic entity) {
		return null;
	}

}
