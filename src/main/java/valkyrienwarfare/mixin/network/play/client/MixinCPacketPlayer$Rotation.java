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

package valkyrienwarfare.mixin.network.play.client;

import net.minecraft.client.Minecraft;
import net.minecraft.network.PacketBuffer;
import net.minecraft.network.play.client.CPacketPlayer;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import valkyrienwarfare.api.Vector;
import valkyrienwarfare.mod.network.IExtendedCPacketPlayer;
import valkyrienwarfare.mod.physmanagement.interaction.IDraggable;
import valkyrienwarfare.physics.data.TransformType;
import valkyrienwarfare.physics.management.PhysicsWrapperEntity;

// Made abstract because the super class already implements this interface (from MixinCPacketPlayer), the compiled side of java just doesn't
// know it yet.
@Mixin(CPacketPlayer.Rotation.class)
public abstract class MixinCPacketPlayer$Rotation extends CPacketPlayer implements IExtendedCPacketPlayer {

    @Inject(method = "<init>*", at = @At("RETURN"))
    private void postInit(CallbackInfo info) {
        if (isClientPacket()) {
            if (isPlayerStandingOnShip()) {
                PhysicsWrapperEntity worldBelow = getWorldBelowFeet();
                Vector positionInLocal = new Vector(x, y, z); // , worldBelow.wrapping.coordTransform.wToLTransform);
                worldBelow.getPhysicsObject().coordTransform.getCurrentTickTransform().transform(positionInLocal, TransformType.GLOBAL_TO_LOCAL);
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
