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
import net.minecraft.network.play.server.SPacketPlayerPosLook;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.text.TextComponentString;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import valkyrienwarfare.ValkyrienWarfareMod;
import valkyrienwarfare.api.TransformType;
import valkyrienwarfare.math.Vector;
import valkyrienwarfare.mod.physmanagement.chunk.PhysicsChunkManager;
import valkyrienwarfare.physics.management.PhysicsWrapperEntity;

import java.util.Set;

//TODO: a lot of these mixins can probably be done using overrides instead of overwrites, i should have a look at some point
@Mixin(value = NetHandlerPlayServer.class, priority = 5)
public abstract class MixinNetHandlerPlayServer {

    @Shadow
    public EntityPlayerMP player;
    private NetHandlerPlayServer thisAsNetHandler = NetHandlerPlayServer.class.cast(this);
    private boolean redirectingSetPlayerLocation = false;

    /**
     * Fixes things such that when mods try to teleport players into the ship space,
     * VW will either redirect the teleport or block it. Looking at you
     * SimpleTeleporters mod >:/
     *
     * @param x
     * @param y
     * @param z
     * @param yaw
     * @param pitch
     * @param relativeSet
     * @param callbackInfo
     */
    @Inject(method = "setPlayerLocation(DDDFFLjava/util/Set;)V", at = @At("HEAD"), cancellable = true)
    public void onSetPlayerLocation(double x, double y, double z, float yaw, float pitch,
                                    Set<SPacketPlayerPosLook.EnumFlags> relativeSet, CallbackInfo callbackInfo) {
        if (!redirectingSetPlayerLocation) {
            BlockPos pos = new BlockPos(x, y, z);
            // If the player is being teleported to ship space then we have to stop it.
            if (PhysicsChunkManager.isLikelyShipChunk(pos.getX() >> 4, pos.getZ() >> 4)) {
                callbackInfo.cancel();
                redirectingSetPlayerLocation = true;
                World world = player.getEntityWorld();
                PhysicsWrapperEntity physicsEntity = ValkyrienWarfareMod.VW_PHYSICS_MANAGER.getObjectManagingPos(world,
                        pos);
                if (physicsEntity != null) {
                    Vector tpPos = new Vector(x, y, z);
                    physicsEntity.getPhysicsObject().getShipTransformationManager().getCurrentTickTransform()
                            .transform(tpPos, TransformType.SUBSPACE_TO_GLOBAL);
                    // Now call this again with the transformed position.
                    // player.sendMessage(new TextComponentString("Transformed the player tp from <"
                    // + x + ":" + y + ":" + z + "> to" + tpPos));
                    thisAsNetHandler.setPlayerLocation(tpPos.X, tpPos.Y, tpPos.Z, yaw, pitch, relativeSet);
                } else {
                    player.sendMessage(new TextComponentString(
                            "Tried teleporting you to an unloaded ship; teleportation canceled."));
                }
                redirectingSetPlayerLocation = false;
            }
        }
    }
    
}
