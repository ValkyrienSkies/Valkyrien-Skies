package org.valkyrienskies.mod.client.render;

import net.minecraft.client.renderer.entity.Render;
import net.minecraft.client.renderer.entity.RenderManager;
import net.minecraftforge.fml.client.registry.IRenderFactory;
import org.valkyrienskies.mod.common.entity.PhysicsWrapperEntity;

public class PhysicsWrapperEntityRenderFactory implements IRenderFactory<PhysicsWrapperEntity> {

    @Override
    public Render createRenderFor(RenderManager manager) {
        return new PhysicsWrapperEntityRenderer(manager);
    }

}
