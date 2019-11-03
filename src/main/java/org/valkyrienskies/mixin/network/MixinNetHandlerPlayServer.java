/*
 * Adapted from the Wizardry License
 *
 * Copyright (c) 2015-2019 the Valkyrien Skies team
 *
 * Permission is hereby granted to any persons and/or organizations using this software to copy, modify, merge, publish, and distribute it.
 * Said persons and/or organizations are not allowed to use the software or any derivatives of the work for commercial use or any other means to generate income unless it is to be used as a part of a larger project (IE: "modpacks"), nor are they allowed to claim this software as their own.
 *
 * The persons and/or organizations are also disallowed from sub-licensing and/or trademarking this software without explicit permission from the Valkyrien Skies team.
 *
 * Any persons and/or organizations using this software must disclose their source code and have it publicly available, include this license, provide sufficient credit to the original authors of the project (IE: The Valkyrien Skies team), as well as provide a link to the original project.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND NON INFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
 *
 */

package org.valkyrienskies.mixin.network;

import java.util.Optional;
import java.util.Set;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.network.NetHandlerPlayServer;
import net.minecraft.network.play.server.SPacketPlayerPosLook;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.text.TextComponentString;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.valkyrienskies.mod.common.math.Vector;
import org.valkyrienskies.mod.common.physics.management.physo.PhysicsObject;
import org.valkyrienskies.mod.common.physmanagement.chunk.ShipChunkAllocator;
import org.valkyrienskies.mod.common.util.ValkyrienUtils;
import valkyrienwarfare.api.TransformType;

/**
 * Todo: Replace this with forge events and capabilities.
 */
@Deprecated
@Mixin(value = NetHandlerPlayServer.class, priority = 5)
public abstract class MixinNetHandlerPlayServer {

    @Shadow
    public EntityPlayerMP player;
    private NetHandlerPlayServer thisAsNetHandler = NetHandlerPlayServer.class.cast(this);
    private boolean redirectingSetPlayerLocation = false;

    /**
     * Fixes things such that when mods try to teleport players into the ship space, VS will either
     * redirect the teleport or block it. Looking at you SimpleTeleporters mod >:/
     */
    @Inject(method = "setPlayerLocation(DDDFFLjava/util/Set;)V", at = @At("HEAD"), cancellable = true)
    public void onSetPlayerLocation(double x, double y, double z, float yaw, float pitch,
        Set<SPacketPlayerPosLook.EnumFlags> relativeSet, CallbackInfo callbackInfo) {
        if (!redirectingSetPlayerLocation) {
            BlockPos pos = new BlockPos(x, y, z);
            // If the player is being teleported to ship space then we have to stop it.
            if (ShipChunkAllocator.isLikelyShipChunk(pos.getX() >> 4, pos.getZ() >> 4)) {
                callbackInfo.cancel();
                redirectingSetPlayerLocation = true;
                World world = player.getEntityWorld();
                Optional<PhysicsObject> physicsObject = ValkyrienUtils.getPhysoManagingBlock(world, pos);
                if (physicsObject.isPresent()) {
                    Vector tpPos = new Vector(x, y, z);
                    physicsObject.get()
                        .getShipTransformationManager()
                        .getCurrentTickTransform()
                        .transform(tpPos, TransformType.SUBSPACE_TO_GLOBAL);
                    // Now call this again with the transformed position.
                    // player.sendMessage(new TextComponentString("Transformed the player tp from <"
                    // + x + ":" + y + ":" + z + "> to" + tpPos));
                    thisAsNetHandler
                        .setPlayerLocation(tpPos.x, tpPos.y, tpPos.z, yaw, pitch, relativeSet);
                } else {
                    player.sendMessage(new TextComponentString(
                        "Tried teleporting you to an unloaded ship; teleportation canceled."));
                }
                redirectingSetPlayerLocation = false;
            }
        }
    }

}
