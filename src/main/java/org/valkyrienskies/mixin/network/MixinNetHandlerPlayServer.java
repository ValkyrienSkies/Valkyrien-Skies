package org.valkyrienskies.mixin.network;

import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.network.NetHandlerPlayServer;
import net.minecraft.network.play.server.SPacketPlayerPosLook;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.text.TextComponentString;
import net.minecraft.world.World;
import org.joml.Vector3d;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.valkyrienskies.mod.common.config.VSConfig;
import org.valkyrienskies.mod.common.ships.ShipData;
import org.valkyrienskies.mod.common.ships.chunk_claims.ShipChunkAllocator;
import org.valkyrienskies.mod.common.util.ValkyrienUtils;
import valkyrienwarfare.api.TransformType;

import java.util.Optional;
import java.util.Set;

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
            if (ShipChunkAllocator.isBlockInShipyard(pos)) {
                callbackInfo.cancel();
                redirectingSetPlayerLocation = true;
                World world = player.getEntityWorld();
                Optional<ShipData> ship = ValkyrienUtils.getShipManagingBlock(world, pos);
                if (ship.isPresent()) {
                    Vector3d tpPos = new Vector3d(x, y, z);
                    ship.get()
                        .getShipTransform()
                        .transformPosition(tpPos, TransformType.SUBSPACE_TO_GLOBAL);
                    // Now call this again with the transformed position.
                    // player.sendMessage(new TextComponentString("Transformed the player tp from <"
                    // + x + ":" + y + ":" + z + "> to" + tpPos));
                    thisAsNetHandler
                        .setPlayerLocation(tpPos.x, tpPos.y, tpPos.z, yaw, pitch, relativeSet);

                    if (VSConfig.showAnnoyingDebugOutput) {
                        System.out.printf(
                            "Player was teleported to %.1f, %.1f, %.1f, redirected to %.1f, %.1f, %.1f\n",
                            x, y, z, tpPos.x, tpPos.y, tpPos.z
                        );
                    }

                } else {
                    if (VSConfig.showAnnoyingDebugOutput) {
                        System.out.printf(
                            "Player was teleported to %.1f, %.1f, %.1f, cancelling because no ship found\n",
                            x, y, z
                        );
                    }
                    player.sendMessage(new TextComponentString(
                        "Tried teleporting you to shipyard but there was no ship; teleportation canceled."));
                }
                redirectingSetPlayerLocation = false;
            }
        }
    }

}
