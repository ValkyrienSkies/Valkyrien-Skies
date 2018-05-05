package valkyrienwarfare.mixin.network.play.client;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import net.minecraft.client.Minecraft;
import net.minecraft.network.PacketBuffer;
import net.minecraft.network.play.client.CPacketPlayer;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import valkyrienwarfare.api.Vector;
import valkyrienwarfare.mod.physmanagement.interaction.IDraggable;
import valkyrienwarfare.physics.management.PhysicsWrapperEntity;

@Mixin(CPacketPlayer.PositionRotation.class)
public class MixinCPacketPlayer$PositionRotation extends CPacketPlayer {

    private int worldBelowID = -1;
    private double localX;
    private double localY;
    private double localZ;
    
    @Inject(method = "<init>*", at = @At("RETURN"))
    private void postInit(CallbackInfo info) {
        if (isClientPacket()) {
            if(isPlayerStandingOnShip()) {
                PhysicsWrapperEntity worldBelow = getWorldBelowFeet();
                Vector positionInLocal = new Vector(x, y, z, worldBelow.wrapping.coordTransform.wToLTransform);
                localX = positionInLocal.X;
                localY = positionInLocal.Y;
                localZ = positionInLocal.Z;
                worldBelowID = worldBelow.getEntityId();
            }
        }
    }
    
    @Inject(method = "readPacketData", at = @At("RETURN"))
    public void readPacketDataPost(PacketBuffer buf, CallbackInfo info) {
        this.worldBelowID = buf.readInt();
        this.localX = buf.readDouble();
        this.localY = buf.readDouble();
        this.localZ = buf.readDouble();
    }

    @Inject(method = "writePacketData", at = @At("RETURN"))
    public void writePacketDataPost(PacketBuffer buf, CallbackInfo info) {
        buf.writeInt(worldBelowID);
        buf.writeDouble(localX);
        buf.writeDouble(localY);
        buf.writeDouble(localZ);
    }
    
    // Returns true if this packet was made by a client, and is set to be modified
    private boolean isClientPacket() {
        return x != 0 || y != 0 || z != 0;
    }
    
    @SideOnly(Side.CLIENT)
    private boolean isPlayerStandingOnShip() {
        return getWorldBelowFeet() != null;
    }
    
    @SideOnly(Side.CLIENT)
    private PhysicsWrapperEntity getWorldBelowFeet() {
        Object o = Minecraft.getMinecraft().player;
        IDraggable draggable = (IDraggable) o;
        return draggable.getWorldBelowFeet();
    }

}
