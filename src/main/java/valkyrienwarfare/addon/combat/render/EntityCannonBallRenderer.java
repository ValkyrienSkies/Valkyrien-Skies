/*
 * Adapted from the Wizardry License
 *
 * Copyright (c) 2015-2018 the Valkyrien Warfare team
 *
 * Permission is hereby granted to any persons and/or organizations using this software to copy, modify, merge, publish, and distribute it.
 * Said persons and/or organizations are not allowed to use the software or any derivatives of the work for commercial use or any other means to generate income unless it is to be used as a part of a larger project (IE: "modpacks"), nor are they allowed to claim this software as their own.
 *
 * The persons and/or organizations are also disallowed from sub-licensing and/or trademarking this software without explicit permission from the Valkyrien Warfare team.
 *
 * Any persons and/or organizations using this software must disclose their source code and have it publicly available, include this license, provide sufficient credit to the original authors of the project (IE: The Valkyrien Warfare team), as well as provide a link to the original project.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND NON INFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
 *
 */

package valkyrienwarfare.addon.combat.render;

import net.minecraft.block.state.IBlockState;
import net.minecraft.client.renderer.BufferBuilder;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.culling.ICamera;
import net.minecraft.client.renderer.entity.Render;
import net.minecraft.client.renderer.entity.RenderManager;
import net.minecraft.client.renderer.texture.TextureMap;
import net.minecraft.util.ResourceLocation;
import net.minecraft.world.World;
import valkyrienwarfare.addon.combat.ValkyrienWarfareCombat;
import valkyrienwarfare.addon.combat.entity.EntityCannonBall;
import valkyrienwarfare.api.IRenderVW;
import valkyrienwarfare.mod.client.render.FastBlockModelRenderer;

public class EntityCannonBallRenderer extends Render<EntityCannonBall> implements IRenderVW {

    public static IRenderVW instance;

    {
        instance = this;
    }

    private IBlockState cannonballState;

    protected EntityCannonBallRenderer(RenderManager renderManager) {
        super(renderManager);
    }

    @Override
    public void doRender(EntityCannonBall entity, double x, double y, double z, float entityYaw, float partialTicks) {
        World world = entity.world;

        this.bindTexture(TextureMap.LOCATION_BLOCKS_TEXTURE);
        GlStateManager.pushMatrix();
        GlStateManager.disableLighting();
        Tessellator tessellator = Tessellator.getInstance();
        BufferBuilder BufferBuilder = tessellator.getBuffer();

        GlStateManager.translate((float) (x - .25D), (float) (y - .07D), (float) (z + .278D));
        FastBlockModelRenderer.renderBlockModel(BufferBuilder, tessellator, world, cannonballState, entity.getBrightnessForRender());

        if (this.renderOutlines) {
            GlStateManager.disableOutlineMode();
            GlStateManager.disableColorMaterial();
        }

        GlStateManager.enableLighting();
        GlStateManager.popMatrix();

        super.doRender(entity, x, y, z, entityYaw, partialTicks);
    }

    @Override
    public boolean shouldRender(EntityCannonBall livingEntity, ICamera camera, double camX, double camY, double camZ) {
        return true;
    }

    @Override
    protected ResourceLocation getEntityTexture(EntityCannonBall entity) {
        return null;
    }

    @Override
    public void cacheStates() {
        cannonballState = ValkyrienWarfareCombat.INSTANCE.fakecannonblock.getStateFromMeta(2);
    }
}
