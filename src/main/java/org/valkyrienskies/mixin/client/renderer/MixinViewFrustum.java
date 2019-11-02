package org.valkyrienskies.mixin.client.renderer;

import net.minecraft.client.renderer.ViewFrustum;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ViewFrustum.class)
public class MixinViewFrustum {

    @Shadow
    @Final
    protected World world;

    @Inject(at = {@At("HEAD")}, method = "markBlocksForUpdate")
    public void pre_markBlocksForUpdate(int minX, int minY, int minZ, int maxX, int maxY, int maxZ,
        boolean updateImmediately, CallbackInfo info) {

    }
}
