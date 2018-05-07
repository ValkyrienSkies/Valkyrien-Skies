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
import valkyrienwarfare.mod.network.IExtendedCPacketPlayer;
import valkyrienwarfare.mod.physmanagement.interaction.IDraggable;
import valkyrienwarfare.physics.management.PhysicsWrapperEntity;

// Made abstract because the super class already implements this interface (from MixinCPacketPlayer), the compiled side of java just doesn't
// know it yet.
@Mixin(CPacketPlayer.Position.class)
public abstract class MixinCPacketPlayer$Position extends CPacketPlayer implements IExtendedCPacketPlayer {

    @Inject(method = "<init>*", at = @At("RETURN"))
    private void postInit(CallbackInfo info) {
        if (isClientPacket()) {
            if(isPlayerStandingOnShip()) {
                PhysicsWrapperEntity worldBelow = getWorldBelowFeet();
                Vector positionInLocal = new Vector(x, y, z, worldBelow.wrapping.coordTransform.wToLTransform);
                this.setLocalCoords(positionInLocal.X, positionInLocal.Y, positionInLocal.Z);
                this.setWorldBelowFeetID(worldBelow.getEntityId());
            }
        }
    }
    
    @Inject(method = "readPacketData", at = @At("RETURN"))
    public void readPacketDataPost(PacketBuffer buf, CallbackInfo info) {
        this.setWorldBelowFeetID(buf.readInt());
        this.setLocalCoords(buf.readDouble(), buf.readDouble(), buf.readDouble());
    }

    @Inject(method = "writePacketData", at = @At("RETURN"))
    public void writePacketDataPost(PacketBuffer buf, CallbackInfo info) {
        buf.writeInt(this.getWorldBelowFeetID());
        buf.writeDouble(this.getLocalX());
        buf.writeDouble(this.getLocalY());
        buf.writeDouble(this.getLocalZ());
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
