package org.valkyrienskies.mod.common.physmanagement.interaction;

import java.util.Optional;
import javax.annotation.Nullable;
import javax.annotation.ParametersAreNonnullByDefault;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.Entity;
import net.minecraft.entity.item.EntityArmorStand;
import net.minecraft.entity.item.EntityBoat;
import net.minecraft.entity.item.EntityFallingBlock;
import net.minecraft.entity.passive.EntityPig;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.network.play.server.SPacketBlockBreakAnim;
import net.minecraft.util.SoundCategory;
import net.minecraft.util.SoundEvent;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IWorldEventListener;
import net.minecraft.world.World;
import net.minecraft.world.chunk.Chunk;
import org.valkyrienskies.mod.common.coordinates.CoordinateSpaceType;
import org.valkyrienskies.mod.common.entity.EntityMountable;
import org.valkyrienskies.mod.common.math.Vector;
import org.valkyrienskies.mod.common.physics.management.physo.PhysicsObject;
import org.valkyrienskies.mod.common.util.ValkyrienUtils;
import valkyrienwarfare.api.TransformType;

@ParametersAreNonnullByDefault
public class VSWorldEventListener implements IWorldEventListener {

    private final World worldObj;

    public VSWorldEventListener(World world) {
        worldObj = world;
    }

    @Override
    public void notifyBlockUpdate(World worldIn, BlockPos pos, IBlockState oldState,
        IBlockState newState, int flags) {
        Chunk chunk = worldIn.getChunk(pos);
        ValkyrienUtils.getPhysoManagingChunk(chunk)
            .ifPresent(physicsObject -> physicsObject.onSetBlockState(oldState, newState, pos));
    }

    @Override
    public void notifyLightSet(BlockPos pos) {
    }

    @Override
    public void markBlockRangeForRenderUpdate(int minX, int minY, int minZ, int maxX, int maxY,
        int maxZ) {
        // this.markBlocksForUpdate(minX - 1, minY - 1, minZ - 1, maxX + 1, maxY + 1, maxZ + 1, false);
    }

    @Override
    public void playSoundToAllNearExcept(@Nullable EntityPlayer player, SoundEvent soundIn,
        SoundCategory category, double x,
        double y, double z, float volume, float pitch) {

    }

    @Override
    public void playRecord(SoundEvent soundIn, BlockPos pos) {
    }

    @Override
    public void spawnParticle(int particleID, boolean ignoreRange, double x, double y, double z,
        double xSpeed,
        double ySpeed, double zSpeed, int... parameters) {
    }

    // TODO: Fix conflicts with EventsCommon.onEntityJoinWorldEvent()
    @Override
    public void onEntityAdded(Entity entity) {
        // This is really only here because Sponge doesn't call the entity join event for some reason :/
        // So I basically just copied the event code here as well.
        World world = worldObj;
        BlockPos posAt = new BlockPos(entity);
        Optional<PhysicsObject> physicsObject = ValkyrienUtils.getPhysoManagingBlock(world, posAt);

        if (!worldObj.isRemote && physicsObject.isPresent()
            && !(entity instanceof EntityFallingBlock)) {
            if (entity instanceof EntityArmorStand
                || entity instanceof EntityPig || entity instanceof EntityBoat) {
                EntityMountable entityMountable = new EntityMountable(world,
                    entity.getPositionVector(), CoordinateSpaceType.SUBSPACE_COORDINATES,
                    posAt);
                world.spawnEntity(entityMountable);
                entity.startRiding(entityMountable);
            }
            world.getChunk(entity.getPosition().getX() >> 4, entity.getPosition().getZ() >> 4)
                .removeEntity(entity);
            physicsObject.get()
                .getShipTransformationManager()
                .getCurrentTickTransform().transform(entity,
                TransformType.SUBSPACE_TO_GLOBAL);
            world.getChunk(entity.getPosition().getX() >> 4, entity.getPosition().getZ() >> 4)
                .addEntity(entity);
        }
    }

    @Override
    public void onEntityRemoved(Entity entityIn) {

    }

    @Override
    public void broadcastSound(int soundID, BlockPos pos, int data) {
    }

    @Override
    public void playEvent(EntityPlayer player, int type, BlockPos blockPosIn, int data) {
    }

    @Override
    public void sendBlockBreakProgress(int breakerId, BlockPos pos, int progress) {
        if (!worldObj.isRemote) {
            for (EntityPlayer entityplayermp : worldObj.playerEntities) {
                if (entityplayermp != null && entityplayermp.getEntityId() != breakerId) {
                    Vector posVector = new Vector(pos.getX(), pos.getY(), pos.getZ());

                    Optional<PhysicsObject> physicsObject = ValkyrienUtils
                        .getPhysoManagingBlock(worldObj, pos);

                    physicsObject.ifPresent(object -> object
                        .getShipTransformationManager()
                        .getCurrentTickTransform()
                        .transform(posVector,
                            TransformType.SUBSPACE_TO_GLOBAL));

                    double d0 = posVector.x - entityplayermp.posX;
                    double d1 = posVector.y - entityplayermp.posY;
                    double d2 = posVector.z - entityplayermp.posZ;

                    if (d0 * d0 + d1 * d1 + d2 * d2 < 1024.0D) {
                        ((EntityPlayerMP) entityplayermp).connection
                            .sendPacket(new SPacketBlockBreakAnim(breakerId, pos, progress));
                    }
                }
            }
        }
    }

    @Override
    public void spawnParticle(int p_190570_1_, boolean p_190570_2_, boolean p_190570_3_,
        double p_190570_4_,
        double p_190570_6_, double p_190570_8_, double p_190570_10_, double p_190570_12_,
        double p_190570_14_,
        int... p_190570_16_) {
    }

}
