package valkyrienwarfare.mixin.client;

import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.WorldClient;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.MathHelper;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;
import valkyrienwarfare.api.TransformType;
import valkyrienwarfare.mod.common.ValkyrienWarfareMod;
import valkyrienwarfare.mod.common.math.Vector;
import valkyrienwarfare.mod.common.physics.management.PhysicsWrapperEntity;

import java.util.List;

@Mixin(Minecraft.class)
public class MixinMinecraft {

    private static final double SHIP_CHECK_RADIUS = 15;

    @Redirect(method = "runTick", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/multiplayer/WorldClient;doVoidFogParticles(III)V"))
    private void valkyrien$redirect$DoVoidParticles(WorldClient worldClient, int posX, int posY, int posZ) {
        AxisAlignedBB axisAlignedBB = new AxisAlignedBB(posX - SHIP_CHECK_RADIUS, posY - SHIP_CHECK_RADIUS, posZ - SHIP_CHECK_RADIUS, posX + SHIP_CHECK_RADIUS, posY + SHIP_CHECK_RADIUS, posZ + SHIP_CHECK_RADIUS);
        List<PhysicsWrapperEntity> physEntities = ValkyrienWarfareMod.VW_PHYSICS_MANAGER.getManagerForWorld(worldClient)
                .getNearbyPhysObjects(axisAlignedBB);
        for (PhysicsWrapperEntity wrapper : physEntities) {
            Vector playPosInShip = new Vector(posX + .5, posY + .5, posZ + .5);
            wrapper.getPhysicsObject()
                    .getShipTransformationManager()
                    .getCurrentTickTransform()
                    .transform(playPosInShip, TransformType.GLOBAL_TO_SUBSPACE);
            worldClient.doVoidFogParticles(MathHelper.floor(playPosInShip.X), MathHelper.floor(playPosInShip.Y), MathHelper.floor(playPosInShip.Z));
        }
        worldClient.doVoidFogParticles(posX, posY, posZ);
    }
}
