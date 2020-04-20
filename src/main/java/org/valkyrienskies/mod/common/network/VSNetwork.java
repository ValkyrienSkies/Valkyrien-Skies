package org.valkyrienskies.mod.common.network;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.network.Packet;
import net.minecraft.network.play.server.SPacketEffect;
import net.minecraft.network.play.server.SPacketSoundEffect;
import net.minecraft.server.management.PlayerChunkMap;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraft.world.WorldServer;
import net.minecraftforge.common.DimensionManager;
import org.joml.Vector3d;
import org.valkyrienskies.mod.common.ships.ship_world.PhysicsObject;
import org.valkyrienskies.mod.common.util.ValkyrienUtils;
import valkyrienwarfare.api.TransformType;

import javax.annotation.Nullable;
import java.util.List;
import java.util.Optional;

/**
 * A few simple static implementations of functions that send packets, correctly handling for
 * ships.
 */
public class VSNetwork {

    /**
     * Don't use this! Use world.notifyBlockUpdate() instead!
     */
    @Deprecated
    public static void sendTileToAllNearby(TileEntity tileEntity) {
        PlayerChunkMap playerChunkMap = ((WorldServer) tileEntity.getWorld()).playerChunkMap;
        playerChunkMap.markBlockForUpdate(tileEntity.getPos());
    }

    public static void sendToAllNearExcept(@Nullable EntityPlayer except, double x, double y,
        double z, double radius, int dimension, Packet<?> packetIn) {
        BlockPos pos = new BlockPos(x, y, z);
        World worldIn;
        if (except == null) {
            worldIn = DimensionManager.getWorld(dimension);
        } else {
            worldIn = except.world;
        }
        Optional<PhysicsObject> physicsObject = ValkyrienUtils.getPhysoManagingBlock(worldIn, pos);
        Vector3d packetPosition = new Vector3d(x, y, z);
        if (physicsObject.isPresent()) {
            physicsObject.get()
                .getShipTransformationManager()
                .getCurrentTickTransform()
                .transformPosition(packetPosition, TransformType.SUBSPACE_TO_GLOBAL);
            // Special treatment for certain packets.
            if (packetIn instanceof SPacketSoundEffect) {
                SPacketSoundEffect soundEffect = (SPacketSoundEffect) packetIn;
                packetIn = new SPacketSoundEffect(soundEffect.sound, soundEffect.category,
                    packetPosition.x, packetPosition.y, packetPosition.z, soundEffect.soundVolume,
                    soundEffect.soundPitch);
            }

            if (packetIn instanceof SPacketEffect) {
                SPacketEffect effect = (SPacketEffect) packetIn;
                BlockPos blockpos = new BlockPos(packetPosition.x, packetPosition.y,
                    packetPosition.z);
                packetIn = new SPacketEffect(effect.soundType, blockpos, effect.soundData,
                    effect.serverWide);
            }
        }

        List<EntityPlayer> playerEntityList = ((WorldServer) worldIn).playerEntities;

        // Original method here.
        for (int i = 0; i < playerEntityList.size(); ++i) {
            EntityPlayerMP entityplayermp = (EntityPlayerMP) playerEntityList.get(i);

            if (entityplayermp != except && entityplayermp.dimension == dimension) {
                double d0 = x - entityplayermp.posX;
                double d1 = y - entityplayermp.posY;
                double d2 = z - entityplayermp.posZ;

                double d3 = packetPosition.x - entityplayermp.posX;
                double d4 = packetPosition.y - entityplayermp.posY;
                double d5 = packetPosition.z - entityplayermp.posZ;

                // Cover both cases; if player is in ship space or if player is in world space.
                if ((d0 * d0 + d1 * d1 + d2 * d2 < radius * radius) || (d3 * d3 + d4 * d4 + d5 * d5
                    < radius * radius)) {
                    entityplayermp.connection.sendPacket(packetIn);
                }
            }
        }
    }
}
