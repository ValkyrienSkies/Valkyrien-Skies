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

package valkyrienwarfare.mixin.network;

import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.network.NetHandlerPlayServer;
import net.minecraft.network.Packet;
import net.minecraft.network.PacketThreadUtil;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;
import valkyrienwarfare.mod.physmanagement.interaction.EntityDraggable;
import valkyrienwarfare.mod.physmanagement.interaction.IDraggable;
import valkyrienwarfare.mod.physmanagement.interaction.INHPServerVW;

//TODO: a lot of these mixins can probably be done using overrides instead of overwrites, i should have a look at some point
@Mixin(NetHandlerPlayServer.class)
public abstract class MixinNetHandlerPlayServer implements INHPServerVW {
    @Shadow
    public EntityPlayerMP player;

    private double dummyBlockReachDist = 9999999999999999999999999999D;
    private double lastGoodBlockReachDist;

    @Redirect(method = "update",
            at = @At(value = "INVOKE",
                    target = "Lnet/minecraft/network/NetHandlerPlayServer;captureCurrentPosition()V"))
    public void makeIDraggableNotUseless(NetHandlerPlayServer server) {
        IDraggable draggable = EntityDraggable.getDraggableFromEntity(player);
        server.captureCurrentPosition();

        server.firstGoodX += draggable.getVelocityAddedToPlayer().X;
        server.firstGoodY += draggable.getVelocityAddedToPlayer().Y;
        server.firstGoodZ += draggable.getVelocityAddedToPlayer().Z;
        server.lastGoodX += draggable.getVelocityAddedToPlayer().X;
        server.lastGoodY += draggable.getVelocityAddedToPlayer().Y;
        server.lastGoodZ += draggable.getVelocityAddedToPlayer().Z;
    }

    @Override
    public double dummyBlockReachDist() {
        return dummyBlockReachDist;
    }

    @Override
    public void dummyBlockReachDist(double in) {
        dummyBlockReachDist = in;
    }

    @Override
    public double lastGoodBlockReachDist() {
        return lastGoodBlockReachDist;
    }

    @Override
    public void lastGoodBlockReachDist(double in) {
        lastGoodBlockReachDist = in;
    }

    @Override
    public void checkForPacketEnqueueTrap(Packet packetIn) {
        PacketThreadUtil.checkThreadAndEnqueue(packetIn, NetHandlerPlayServer.class.cast(this), player.getServerWorld());
    }

    @Override
    public EntityPlayerMP getEntityPlayerFromHandler() {
        return player;
    }

}
