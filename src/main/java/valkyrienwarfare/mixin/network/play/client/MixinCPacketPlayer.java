package valkyrienwarfare.mixin.network.play.client;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;
import org.spongepowered.asm.mixin.Shadow;

import net.minecraft.entity.Entity;
import net.minecraft.network.NetHandlerPlayServer;
import net.minecraft.network.play.INetHandlerPlayServer;
import net.minecraft.network.play.client.CPacketPlayer;
import net.minecraft.world.World;
import valkyrienwarfare.api.Vector;
import valkyrienwarfare.physics.management.PhysicsWrapperEntity;

@Mixin(CPacketPlayer.class)
public class MixinCPacketPlayer {

    @Shadow
    protected double x;
    @Shadow
    protected double y;
    @Shadow
    protected double z;
    private int worldBelowID = -1;
    private double localX;
    private double localY;
    private double localZ;

    // Todo: This doesnt work yet
    @Overwrite
    public void processPacket(INetHandlerPlayServer handler) {
//        System.out.println();
        if (worldBelowID != -1) {
            System.out.println("RTest");
            try {
                NetHandlerPlayServer serverHandler = (NetHandlerPlayServer) handler;
                World world = serverHandler.player.getEntityWorld();
                Entity theEntity = world.getEntityByID(worldBelowID);
                PhysicsWrapperEntity worldBelow = (PhysicsWrapperEntity) theEntity;
                Vector positionInGlobal = new Vector(localX, localY, localZ, worldBelow.wrapping.coordTransform.lToWTransform);
                x = positionInGlobal.X;
                y = positionInGlobal.Y;
                z = positionInGlobal.Z;
            } catch(Exception e) {
                return;
            }
        }
        handler.processPlayer(CPacketPlayer.class.cast(this));
    }

}
