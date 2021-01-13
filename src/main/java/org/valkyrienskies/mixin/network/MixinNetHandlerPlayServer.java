package org.valkyrienskies.mixin.network;

import com.google.common.collect.ImmutableList;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.network.NetHandlerPlayServer;
import net.minecraft.network.play.client.CPacketPlayer;
import net.minecraft.network.play.server.SPacketPlayerPosLook;
import net.minecraft.util.Tuple;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.text.TextComponentString;
import net.minecraft.world.World;
import net.minecraft.world.WorldServer;
import org.joml.Vector3d;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.*;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.valkyrienskies.mod.common.config.VSConfig;
import org.valkyrienskies.mod.common.network.IHasPlayerMovementData;
import org.valkyrienskies.mod.common.network.PlayerMovementData;
import org.valkyrienskies.mod.common.ships.QueryableShipData;
import org.valkyrienskies.mod.common.ships.ShipData;
import org.valkyrienskies.mod.common.ships.chunk_claims.ShipChunkAllocator;
import org.valkyrienskies.mod.common.ships.entity_interaction.IDraggable;
import org.valkyrienskies.mod.common.ships.ship_transform.ShipTransform;
import org.valkyrienskies.mod.common.ships.ship_world.PhysicsObject;
import org.valkyrienskies.mod.common.util.VSMath;
import org.valkyrienskies.mod.common.util.ValkyrienUtils;
import valkyrienwarfare.api.TransformType;

import java.util.*;

@Mixin(value = NetHandlerPlayServer.class)
public abstract class MixinNetHandlerPlayServer {

    private final NetHandlerPlayServer thisAsNetHandler = NetHandlerPlayServer.class.cast(this);
    private boolean redirectingSetPlayerLocation = false;

    @Shadow
    public EntityPlayerMP player;
    @Shadow
    public double firstGoodX;
    @Shadow
    public double firstGoodY;
    @Shadow
    public double firstGoodZ;
    @Shadow
    public double lastGoodX;
    @Shadow
    public double lastGoodY;
    @Shadow
    public double lastGoodZ;

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

    /**
     * @reason This mixin prevents the server from thinking a player is stuck in blocks; which fixes some player moved wrongly errors.
     */
    @Redirect(
            method = "processPlayer",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/world/WorldServer;getCollisionBoxes(Lnet/minecraft/entity/Entity;Lnet/minecraft/util/math/AxisAlignedBB;)Ljava/util/List;"
            ))
    private List<AxisAlignedBB> removeStuckInBlockMovementCheck(WorldServer worldServer, Entity entityIn, AxisAlignedBB aabb) {
        return ImmutableList.of();
    }

    /**
     * @reason This mixin forces the condition (d11 > 0.0625D) to always be false; which fixes some player moved wrongly errors.
     */
    @ModifyConstant(
            method = "processPlayer",
            constant = @Constant(
                    doubleValue = 0.0625D,
                    ordinal = 1
            ))
    private double allowMovementToBeVeryWrong(double originalConstant) {
        return 10;
    }

    /**
     * This mixin fixes "Player Moved Wrongly" errors.
     *
     * @param packetPlayer The packet the player sent us
     * @param info We can use this to cancel the invocation
     */
    @Inject(method = "processPlayer", at = @At("HEAD"))
    private void preProcessPlayer(final CPacketPlayer packetPlayer, final CallbackInfo info) {
        // Don't run any of this code on the network thread!
        if (this.player.getServerWorld().isCallingFromMinecraftThread()) {
            // This fixes players dying of fall damage when changing dimensions
            if (this.player.isInvulnerableDimensionChange()) {
                return;
            }
            final PlayerMovementData addedPlayerMovementData = IHasPlayerMovementData.class.cast(packetPlayer).getPlayerMovementData();
            final World world = player.world;

            final UUID lastTouchedShipId = addedPlayerMovementData.getLastTouchedShipId();
            final int ticksSinceTouchedLastShip = addedPlayerMovementData.getTicksSinceTouchedLastShip();
            final int ticksPartOfGround = addedPlayerMovementData.getTicksPartOfGround();
            final Vector3d playerPosInShip = new Vector3d(addedPlayerMovementData.getPlayerPosInShip());
            final Vector3d playerLookInShip = new Vector3d(addedPlayerMovementData.getPlayerLookInShip());

            ShipData lastTouchedShip = null;
            if (lastTouchedShipId != null) {
                final QueryableShipData queryableShipData = QueryableShipData.get(world);
                final Optional<ShipData> shipDataOptional = queryableShipData.getShip(lastTouchedShipId);
                if (shipDataOptional.isPresent()) {
                    lastTouchedShip = shipDataOptional.get();

                    final PhysicsObject shipObject = ValkyrienUtils.getServerShipManager(world).getPhysObjectFromUUID(lastTouchedShip.getUuid());

                    if (shipObject != null) {
                        if (shipObject.getTicksSinceShipTeleport() > PhysicsObject.TICKS_SINCE_TELEPORT_TO_START_DRAGGING) {
                            final ShipTransform shipTransform = lastTouchedShip.getShipTransform();
                            shipTransform.transformPosition(playerPosInShip, TransformType.SUBSPACE_TO_GLOBAL);
                            shipTransform.transformDirection(playerLookInShip, TransformType.SUBSPACE_TO_GLOBAL);
                        } else {
                            // Don't move the player relative to the ship until the TicksSinceShipTeleport timer expires.
                            playerPosInShip.set(player.posX, player.posY, player.posZ);
                        }
                    }
                } else {
                    // Rare case, just ignore this
                    // info.cancel();
                    return;
                }
            }

            // Get the player pitch/yaw from the look vector
            final Tuple<Double, Double> pitchYawTuple = VSMath.getPitchYawFromVector(playerLookInShip);
            final double playerPitchInGlobal = pitchYawTuple.getFirst();
            final double playerYawInGlobal = pitchYawTuple.getSecond();

            // Idk if this is needed, but I'm too bothered to change it
            packetPlayer.moving = true;

            // Then update the packet values to match the ones above.
            packetPlayer.x = playerPosInShip.x();
            packetPlayer.y = playerPosInShip.y();
            packetPlayer.z = playerPosInShip.z();
            packetPlayer.yaw = (float) playerYawInGlobal;
            packetPlayer.pitch = (float) playerPitchInGlobal;

            // Set the player motion values to tell the NetHandlerPlayServer that the player is allowed to move this fast.
            this.player.motionX = packetPlayer.x - this.firstGoodX;
            this.player.motionY = packetPlayer.y - this.firstGoodY;
            this.player.motionZ = packetPlayer.z - this.firstGoodZ;

            // Update the player draggable
            final IDraggable playerAsDraggable = IDraggable.class.cast(this.player);
            playerAsDraggable.setEntityShipMovementData(
                    playerAsDraggable.getEntityShipMovementData()
                            .withLastTouchedShip(lastTouchedShip)
                            .withAddedLinearVelocity(new Vector3d())
                            .withAddedYawVelocity(0)
                            .withTicksPartOfGround(ticksPartOfGround)
                            .withTicksSinceTouchedShip(ticksSinceTouchedLastShip)
            );
        }
    }

}
