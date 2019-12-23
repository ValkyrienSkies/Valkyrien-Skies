package org.valkyrienskies.mod.client.render;

import net.minecraft.client.renderer.entity.Render;
import net.minecraft.client.renderer.entity.RenderManager;
import net.minecraft.client.renderer.texture.TextureMap;
import net.minecraft.util.ResourceLocation;
import org.valkyrienskies.mod.common.entity.PhysicsWrapperEntity;

/**
 * This class is the default MC way of rendering ships. Nothing renders here on purpose.
 *
 * @author thebest108
 */
public class PhysicsWrapperEntityRenderer extends Render<PhysicsWrapperEntity> {

    public PhysicsWrapperEntityRenderer(RenderManager renderManager) {
        super(renderManager);
    }

    @Override
    protected ResourceLocation getEntityTexture(PhysicsWrapperEntity entity) {
        return TextureMap.LOCATION_BLOCKS_TEXTURE;
    }

}
