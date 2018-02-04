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

import net.minecraft.block.material.Material;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.item.ItemBucket;
import net.minecraft.item.ItemStack;
import net.minecraft.network.NetHandlerPlayServer;
import net.minecraft.network.PacketThreadUtil;
import net.minecraft.network.play.client.CPacketPlayerDigging;
import net.minecraft.network.play.client.CPacketPlayerTryUseItemOnBlock;
import net.minecraft.network.play.client.CPacketUpdateSign;
import net.minecraft.network.play.server.SPacketBlockChange;
import net.minecraft.server.MinecraftServer;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.tileentity.TileEntitySign;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.text.TextComponentString;
import net.minecraft.util.text.TextComponentTranslation;
import net.minecraft.util.text.TextFormatting;
import net.minecraft.world.WorldServer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import valkyrienwarfare.ValkyrienWarfareMod;
import valkyrienwarfare.api.RotationMatrices;
import valkyrienwarfare.api.Vector;
import valkyrienwarfare.mod.physmanagement.interaction.EntityDraggable;
import valkyrienwarfare.mod.physmanagement.interaction.IDraggable;
import valkyrienwarfare.mod.physmanagement.interaction.INHPServerVW;
import valkyrienwarfare.mod.physmanagement.interaction.PlayerDataBackup;
import valkyrienwarfare.physics.management.PhysicsWrapperEntity;

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
    public void makeIDraggableNotUseless(NetHandlerPlayServer server)   {
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
}
