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

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.network.NetHandlerPlayServer;
import net.minecraft.network.play.INetHandlerPlayServer;
import net.minecraft.network.play.client.CPacketPlayerDigging;
import net.minecraft.util.math.BlockPos;
import valkyrienwarfare.ValkyrienWarfareMod;
import valkyrienwarfare.api.RotationMatrices;
import valkyrienwarfare.mod.physmanagement.interaction.INHPServerVW;
import valkyrienwarfare.mod.physmanagement.interaction.PlayerDataBackup;
import valkyrienwarfare.physics.data.TransformType;
import valkyrienwarfare.physics.management.PhysicsWrapperEntity;

@Mixin(CPacketPlayerDigging.class)
public abstract class MixinCPacketPlayerDigging {

    private final CPacketPlayerDigging thisPacketTryUse = CPacketPlayerDigging.class.cast(this);
    private PlayerDataBackup playerBackup;

    @Inject(method = "processPacket", at = @At(value = "HEAD"))
    public void preHandleUseItemPacket(INetHandlerPlayServer server, CallbackInfo info) {
        INHPServerVW vw = (INHPServerVW) (NetHandlerPlayServer) server;
        EntityPlayerMP player = vw.getEntityPlayerFromHandler();
        if (player.getServerWorld().isCallingFromMinecraftThread()) {
            BlockPos packetPos = thisPacketTryUse.getPosition();
            PhysicsWrapperEntity wrapper = ValkyrienWarfareMod.physicsManager.getObjectManagingPos(player.world,
                    packetPos);
            if (wrapper != null && wrapper.wrapping.coordTransform != null) {
                playerBackup = new PlayerDataBackup(player);
                RotationMatrices.applyTransform(wrapper.wrapping.coordTransform.getCurrentTickTransform(), player,
                        TransformType.GLOBAL_TO_LOCAL);
            }
        }
    }

    @Inject(method = "processPacket", at = @At(value = "TAIL"))
    public void postHandleUseItemPacket(INetHandlerPlayServer server, CallbackInfo info) {
        INHPServerVW vw = (INHPServerVW) (NetHandlerPlayServer) server;
        EntityPlayerMP player = vw.getEntityPlayerFromHandler();
        if (player.getServerWorld().isCallingFromMinecraftThread()) {
            BlockPos packetPos = thisPacketTryUse.getPosition();
            PhysicsWrapperEntity wrapper = ValkyrienWarfareMod.physicsManager.getObjectManagingPos(player.world,
                    packetPos);
            if (wrapper != null && wrapper.wrapping.coordTransform != null) {
                playerBackup.restorePlayerToBackup();
                // RotationMatrices.applyTransform(wrapper.wrapping.coordTransform.getCurrentTickTransform(),
                // player,
                // TransformType.LOCAL_TO_GLOBAL);
            }
        }
    }
}
