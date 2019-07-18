package valkyrienwarfare.mod.common.entity;

import io.netty.buffer.ByteBuf;
import net.minecraft.entity.Entity;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.network.PacketBuffer;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;
import net.minecraftforge.fml.common.registry.IEntityAdditionalSpawnData;
import valkyrienwarfare.api.TransformType;
import valkyrienwarfare.fixes.IPhysicsChunk;
import valkyrienwarfare.mod.common.physics.management.PhysicsObject;

import java.util.Optional;

/**
 * A simple entity whose only purpose is to allow mounting onto chairs, as well as fixing entities onto ships.
 */
public class EntityMountable extends Entity implements IEntityAdditionalSpawnData {

    // An optional in case we try accessing it before nbt load has occurred.
    private Optional<Vec3d> mountPos;
    // A blockpos that is a means of getting the parent ship of this entity mountable. Can be any position in the ship, and in the case of chairs is the same as the chair block position.
    private Optional<BlockPos> referencePos;

    public EntityMountable(World worldIn) {
        super(worldIn);
        // default value
        this.mountPos = Optional.empty();
        this.referencePos = Optional.empty();
        this.width = 0.01f;
        this.height = 0.01f;
    }

    public EntityMountable(World worldIn, Vec3d chairPos) {
        this(worldIn);
        this.mountPos = Optional.of(chairPos);
        this.setPosition(chairPos.x, chairPos.y, chairPos.z);
    }

    public EntityMountable(World worldIn, Vec3d chairPos, BlockPos shipReferencePos) {
        this(worldIn, chairPos);
        this.referencePos = Optional.of(shipReferencePos);
    }

    @Override
    public void onUpdate() {
        if (!mountPos.isPresent() || !this.isBeingRidden()) {
            this.setDead();
        }
        if (!mountPos.isPresent()) {
            throw new IllegalStateException("Mounting position not present!");
        }
        Vec3d entityPos = mountPos.get();
        if (referencePos.isPresent()) {
            Optional<PhysicsObject> mountedOnto = ((IPhysicsChunk) world.getChunk(referencePos.get())).getPhysicsObjectOptional();
            if (mountedOnto.isPresent()) {
                entityPos = mountedOnto.get()
                        .transformVector(entityPos, TransformType.SUBSPACE_TO_GLOBAL);
            }
        }
        setPosition(entityPos.x, entityPos.y, entityPos.z);
    }

    @Override
    protected void entityInit() {

    }

    @Override
    protected void readEntityFromNBT(NBTTagCompound compound) {
        if (compound.hasKey("vw_mount_pos_x")) {
            mountPos = Optional.of(new Vec3d(compound.getDouble("vw_mount_pos_x"), compound.getDouble("vw_mount_pos_y"), compound.getDouble("vw_mount_pos_z")));
        }
        if (compound.hasKey("vw_ref_pos_x")) {
            referencePos = Optional.of(new BlockPos(compound.getInteger("vw_ship_pos_x"), compound.getInteger("vw_ship_pos_y"), compound.getInteger("vw_ship_pos_z")));
        }
    }

    @Override
    protected void writeEntityToNBT(NBTTagCompound compound) {
        if (mountPos.isPresent()) {
            // Try to prevent data race
            Vec3d mountPosLocal = mountPos.get();
            compound.setDouble("vw_mount_pos_x", mountPosLocal.x);
            compound.setDouble("vw_mount_pos_y", mountPosLocal.y);
            compound.setDouble("vw_mount_pos_z", mountPosLocal.z);
        }
        if (referencePos.isPresent()) {
            // Try to prevent data race
            BlockPos shipPosLocal = referencePos.get();
            compound.setInteger("vw_ref_pos_x", shipPosLocal.getX());
            compound.setInteger("vw_ref_pos_y", shipPosLocal.getY());
            compound.setInteger("vw_ref_pos_z", shipPosLocal.getZ());
        }
    }

    @Override
    public void writeSpawnData(ByteBuf buffer) {
        PacketBuffer packetBuffer = new PacketBuffer(buffer);
        packetBuffer.writeBoolean(mountPos.isPresent());
        if (mountPos.isPresent()) {
            Vec3d mountPosLocal = mountPos.get();
            packetBuffer.writeDouble(mountPosLocal.x);
            packetBuffer.writeDouble(mountPosLocal.y);
            packetBuffer.writeDouble(mountPosLocal.z);
        }
        packetBuffer.writeBoolean(referencePos.isPresent());
        if (referencePos.isPresent()) {
            packetBuffer.writeBlockPos(referencePos.get());
        }
    }

    @Override
    public void readSpawnData(ByteBuf additionalData) {
        PacketBuffer packetBuffer = new PacketBuffer(additionalData);
        if (packetBuffer.readBoolean()) {
            mountPos = Optional.of(new Vec3d(packetBuffer.readDouble(), packetBuffer.readDouble(), packetBuffer.readDouble()));
        }
        if (packetBuffer.readBoolean()) {
            referencePos = Optional.of(packetBuffer.readBlockPos());
        }
    }

    public Optional<BlockPos> getReferencePos() {
        return referencePos;
    }
}
