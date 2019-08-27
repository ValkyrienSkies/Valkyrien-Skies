package org.valkyrienskies.mixin.client.renderer;

import net.minecraft.client.renderer.ViewFrustum;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.valkyrienskies.mod.common.physics.management.PhysicsObject;
import org.valkyrienskies.mod.common.util.ValkyrienUtils;

import java.util.Optional;

@Mixin(ViewFrustum.class)
public class MixinViewFrustum {

    @Shadow
    @Final
    protected World world;

    @Inject(at = {@At("HEAD")}, method = "markBlocksForUpdate")
    public void pre_markBlocksForUpdate(int minX, int minY, int minZ, int maxX, int maxY, int maxZ,
                                        boolean updateImmediately, CallbackInfo info) {
        Optional<PhysicsObject> physicsObject =
                ValkyrienUtils.getPhysicsObject(world, new BlockPos(minX, minY, minZ));
        physicsObject.ifPresent(p ->
                p.getShipRenderer().updateRange(minX, minY, minZ, maxX, maxY, maxZ, updateImmediately));
    }
}
