package org.valkyrienskies.mixin.network.play.client;

import java.util.Optional;
import net.minecraft.network.NetHandlerPlayServer;
import net.minecraft.network.play.INetHandlerPlayServer;
import net.minecraft.network.play.client.CPacketUpdateSign;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.valkyrienskies.fixes.ITransformablePacket;
import org.valkyrienskies.mod.common.entity.PhysicsWrapperEntity;
import org.valkyrienskies.mod.common.physics.management.PhysicsObject;
import org.valkyrienskies.mod.common.util.ValkyrienUtils;

@Mixin(CPacketUpdateSign.class)
public class MixinCPacketUpdateSign implements ITransformablePacket {

    private final CPacketUpdateSign thisAsPacketSign = CPacketUpdateSign.class.cast(this);

    @Inject(method = "processPacket", at = @At(value = "HEAD"))
    public void preHandleUseItemPacket(INetHandlerPlayServer server, CallbackInfo info) {
        this.doPreProcessing(server, false);
    }

    @Inject(method = "processPacket", at = @At(value = "RETURN"))
    public void postHandleUseItemPacket(INetHandlerPlayServer server, CallbackInfo info) {
        this.doPostProcessing(server, false);
    }

    @Override
    public PhysicsWrapperEntity getPacketParent(NetHandlerPlayServer server) {
        World world = server.player.getEntityWorld();
        Optional<PhysicsObject> physicsObject = ValkyrienUtils
            .getPhysicsObject(world, thisAsPacketSign.getPosition());
        return physicsObject.map(PhysicsObject::getWrapperEntity).orElse(null);
    }
}
