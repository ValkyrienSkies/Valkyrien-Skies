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

package valkyrienwarfare.mixin.server.management;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.network.Packet;
import net.minecraft.network.play.server.SPacketEffect;
import net.minecraft.network.play.server.SPacketSoundEffect;
import net.minecraft.server.management.PlayerList;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraftforge.common.DimensionManager;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyArgs;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import org.spongepowered.asm.mixin.injection.invoke.arg.Args;
import valkyrienwarfare.ValkyrienWarfareMod;
import valkyrienwarfare.math.Vector;
import valkyrienwarfare.physics.management.PhysicsWrapperEntity;

import javax.annotation.Nullable;
import java.util.List;

@Mixin(PlayerList.class)
public abstract class MixinPlayerList {

//    @Shadow
//    @Final
//    public List<EntityPlayerMP> playerEntityList;

    @Shadow
    public abstract void sendToAllNearExcept(@Nullable EntityPlayer except, double x, double y, double z, double radius, int dimension, Packet<?> packetIn);


    @Inject(method = "sendToAllNearExcept(Lnet/minecraft/entity/player/EntityPlayer;DDDDILnet/minecraft/network/Packet;)V",
            at = @At(value = "HEAD"), cancellable = true)
    public void injectionToSendToAll(EntityPlayer except, double x, double y, double z, double radius, int dimension, Packet<?> packetIn, CallbackInfo info) {
        BlockPos pos = new BlockPos(x, y, z);
        World worldIn;
        if (except == null) {
            worldIn = DimensionManager.getWorld(dimension);
        } else {
            worldIn = except.world;
        }
        PhysicsWrapperEntity wrapper = ValkyrienWarfareMod.VW_PHYSICS_MANAGER.getObjectManagingPos(worldIn, pos);
        if (wrapper != null && wrapper.getPhysicsObject().getShipTransformationManager() != null) {
            // Don't call the rest of the method.
            info.cancel();
            Vector packetPosition = new Vector(x, y, z);
            wrapper.getPhysicsObject().getShipTransformationManager().fromLocalToGlobal(packetPosition);
            // Special treatment for certain packets.
            if (packetIn instanceof SPacketSoundEffect) {
                SPacketSoundEffect soundEffect = (SPacketSoundEffect) packetIn;
                packetIn = new SPacketSoundEffect(soundEffect.sound, soundEffect.category, packetPosition.X, packetPosition.Y, packetPosition.Z, soundEffect.soundVolume, soundEffect.soundPitch);
            }

            if (packetIn instanceof SPacketEffect) {
                SPacketEffect effect = (SPacketEffect) packetIn;
                BlockPos blockpos = new BlockPos(packetPosition.X, packetPosition.Y, packetPosition.Z);
                packetIn = new SPacketEffect(effect.soundType, blockpos, effect.soundData, effect.serverWide);
            }

            // Call the original method again.
            sendToAllNearExcept(except, packetPosition.X, packetPosition.Y, packetPosition.Z, radius, dimension, packetIn);
        } else {
            return;
        }
    }

    /**
     * SHUT UP IDEA
     *
     * @author DaPorkchop_
     */
    /*
    @Overwrite
    public void sendToAllNearExcept(@Nullable EntityPlayer except, double x, double y, double z, double radius, int dimension, Packet<?> packetIn) {
        BlockPos pos = new BlockPos(x, y, z);
        World worldIn;
        if (except == null) {
            worldIn = DimensionManager.getWorld(dimension);
        } else {
            worldIn = except.world;
        }
        PhysicsWrapperEntity wrapper = ValkyrienWarfareMod.VW_PHYSICS_MANAGER.getObjectManagingPos(worldIn, pos);
        Vector packetPosition = new Vector(x, y, z);
        if (wrapper != null && wrapper.getPhysicsObject().getShipTransformationManager() != null) {
            wrapper.getPhysicsObject().getShipTransformationManager().fromLocalToGlobal(packetPosition);
            // Special treatment for certain packets.
            if (packetIn instanceof SPacketSoundEffect) {
                SPacketSoundEffect soundEffect = (SPacketSoundEffect) packetIn;
                packetIn = new SPacketSoundEffect(soundEffect.sound, soundEffect.category, packetPosition.X, packetPosition.Y, packetPosition.Z, soundEffect.soundVolume, soundEffect.soundPitch);
            }

            if (packetIn instanceof SPacketEffect) {
                SPacketEffect effect = (SPacketEffect) packetIn;
                BlockPos blockpos = new BlockPos(packetPosition.X, packetPosition.Y, packetPosition.Z);
                packetIn = new SPacketEffect(effect.soundType, blockpos, effect.soundData, effect.serverWide);
            }
        }

        x = packetPosition.X;
        y = packetPosition.Y;
        z = packetPosition.Z;

        // Original method here.
        for (int i = 0; i < this.playerEntityList.size(); ++i) {
            EntityPlayerMP entityplayermp = this.playerEntityList.get(i);
            entityplayermp.connection.sendPacket(packetIn);
        }
    }
    */

}
