package valkyrienwarfare.fixes;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.network.Packet;
import net.minecraft.network.play.server.SPacketEffect;
import net.minecraft.network.play.server.SPacketSoundEffect;
import net.minecraft.network.play.server.SPacketUpdateTileEntity;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraft.world.WorldServer;
import net.minecraftforge.common.DimensionManager;
import valkyrienwarfare.ValkyrienWarfareMod;
import valkyrienwarfare.math.Vector;
import valkyrienwarfare.physics.management.PhysicsWrapperEntity;

import javax.annotation.Nullable;
import java.util.List;

/**
 * A few simple static implementations of functions that send packets, correctly handling for ships.
 */
public class VWNetwork {

    // The radius in which tile entity packets are sent
    public static final double TILE_PACKET_RADIUS = 128;

    public static void sendTileToAllNearby(TileEntity tileEntity) {
        SPacketUpdateTileEntity spacketupdatetileentity = tileEntity.getUpdatePacket();
        VWNetwork.sendToAllNearExcept(null, tileEntity.getPos()
                        .getX(), tileEntity.getPos()
                        .getY(), tileEntity.getPos()
                        .getZ(), TILE_PACKET_RADIUS,
                tileEntity.getWorld().provider.getDimension(), spacketupdatetileentity);
    }

    public static void sendToAllNearExcept(@Nullable EntityPlayer except, double x, double y, double z, double radius, int dimension, Packet<?> packetIn) {
        BlockPos pos = new BlockPos(x, y, z);
        World worldIn;
        if (except == null) {
            worldIn = DimensionManager.getWorld(dimension);
        } else {
            worldIn = except.world;
        }
        PhysicsWrapperEntity wrapper = ValkyrienWarfareMod.VW_PHYSICS_MANAGER.getObjectManagingPos(worldIn, pos);
        Vector packetPosition = new Vector(x, y, z);
        if (wrapper != null && wrapper.getPhysicsObject()
                .getShipTransformationManager() != null) {
            wrapper.getPhysicsObject()
                    .getShipTransformationManager()
                    .fromLocalToGlobal(packetPosition);
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

        List<EntityPlayer> playerEntityList = ((WorldServer) worldIn).playerEntities;

        // Original method here.
        for (int i = 0; i < playerEntityList.size(); ++i) {
            EntityPlayerMP entityplayermp = (EntityPlayerMP) playerEntityList.get(i);

            if (entityplayermp != except && entityplayermp.dimension == dimension) {
                double d0 = x - entityplayermp.posX;
                double d1 = y - entityplayermp.posY;
                double d2 = z - entityplayermp.posZ;

                double d3 = packetPosition.X - entityplayermp.posX;
                double d4 = packetPosition.Y - entityplayermp.posY;
                double d5 = packetPosition.Z - entityplayermp.posZ;

                // Cover both cases; if player is in ship space or if player is in world space.
                if ((d0 * d0 + d1 * d1 + d2 * d2 < radius * radius) || (d3 * d3 + d4 * d4 + d5 * d5 < radius * radius)) {
                    entityplayermp.connection.sendPacket(packetIn);
                }
            }
        }
    }
}
