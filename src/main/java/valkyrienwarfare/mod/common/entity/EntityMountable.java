package valkyrienwarfare.mod.common.entity;

import io.netty.buffer.ByteBuf;
import net.minecraft.entity.Entity;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.network.PacketBuffer;
import net.minecraft.network.datasync.DataParameter;
import net.minecraft.network.datasync.DataSerializers;
import net.minecraft.network.datasync.EntityDataManager;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;
import net.minecraftforge.fml.common.registry.IEntityAdditionalSpawnData;
import valkyrienwarfare.api.TransformType;
import valkyrienwarfare.fixes.IPhysicsChunk;
import valkyrienwarfare.mod.common.coordinates.CoordinateSpaceType;
import valkyrienwarfare.mod.common.physics.management.PhysicsObject;

import java.util.Optional;

/**
 * A simple entity whose only purpose is to allow mounting onto chairs, as well as fixing entities onto ships.
 */
public class EntityMountable extends Entity implements IEntityAdditionalSpawnData {

    public static final DataParameter<NBTTagCompound> SHARED_NBT = EntityDataManager.createKey(EntityMountable.class,
            DataSerializers.COMPOUND_TAG);

    // An optional in case we try accessing it before nbt load has occurred.
    private Vec3d mountPos;
    private CoordinateSpaceType mountPosSpace;
    // A blockpos that is a means of getting the parent ship of this entity mountable. Can be any position in the ship, and in the case of chairs is the same as the chair block position.
    private Optional<BlockPos> referencePos;

    public EntityMountable(World worldIn) {
        super(worldIn);
        // default value
        this.mountPos = null;
        this.referencePos = Optional.empty();
        this.mountPosSpace = null;
        this.width = 0.01f;
        this.height = 0.01f;
        dataManager.register(SHARED_NBT, new NBTTagCompound());
    }

    public EntityMountable(World worldIn, Vec3d chairPos, CoordinateSpaceType coordinateSpaceType) {
        this(worldIn);
        this.mountPos = chairPos;
        this.setPosition(chairPos.x, chairPos.y, chairPos.z);
        this.mountPosSpace = coordinateSpaceType;
    }

    public EntityMountable(World worldIn, Vec3d chairPos, CoordinateSpaceType coordinateSpaceType, BlockPos shipReferencePos) {
        this(worldIn, chairPos, coordinateSpaceType);
        this.referencePos = Optional.of(shipReferencePos);
    }

    public void setMountValues(Vec3d mountPos, CoordinateSpaceType mountPosSpace, Optional<BlockPos> referencePos) {
        this.mountPos = mountPos;
        this.mountPosSpace = mountPosSpace;
        this.referencePos = referencePos;
        updateSharedNBT();
    }

    public void updateSharedNBT() {
        NBTTagCompound tagCompound = new NBTTagCompound();
        writeEntityToNBT(tagCompound);
        dataManager.set(SHARED_NBT, tagCompound);
    }

    public Vec3d getMountPos() {
        return mountPos;
    }

    @Override
    public void notifyDataManagerChange(DataParameter<?> key) {
        if (key == SHARED_NBT) {
            readEntityFromNBT(dataManager.get(SHARED_NBT));
        }
    }

    @Override
    public void onUpdate() {
        if (this.firstUpdate) {
            updateSharedNBT();
        }
        super.onUpdate();
        // Check that this entity isn't broken.
        if (mountPos == null) {
            throw new IllegalStateException("Mounting position not present!");
        }
        if (mountPosSpace == null) {
            throw new IllegalStateException("Mounting space type not present!");
        }
        if (!this.isBeingRidden()) {
            this.setDead();
        }
        // Now update the position of this mounting entity.
        Vec3d entityPos = mountPos;
        if (mountPosSpace == CoordinateSpaceType.SUBSPACE_COORDINATES) {
            if (!referencePos.isPresent()) {
                throw new IllegalStateException("Mounting reference position for ship not present!");
            }
            Optional<PhysicsObject> mountedOnto = ((IPhysicsChunk) world.getChunk(referencePos.get())).getPhysicsObjectOptional();
            if (mountedOnto.isPresent()) {
                entityPos = mountedOnto.get()
                        .transformVector(entityPos, TransformType.SUBSPACE_TO_GLOBAL);
            } else {
                new IllegalStateException("Couldn't access ship with reference coordinates " + referencePos).printStackTrace();
                return;
            }

        }

        setPosition(entityPos.x, entityPos.y, entityPos.z);
    }

    @Override
    protected void entityInit() {

    }

    @Override
    protected void readEntityFromNBT(NBTTagCompound compound) {
        mountPos = new Vec3d(compound.getDouble("vw_mount_pos_x"), compound.getDouble("vw_mount_pos_y"), compound.getDouble("vw_mount_pos_z"));
        mountPosSpace = CoordinateSpaceType.values()[compound.getInteger("vw_coord_type")];

        if (compound.getBoolean("vw_ref_pos_present")) {
            referencePos = Optional.of(new BlockPos(compound.getInteger("vw_ref_pos_x"), compound.getInteger("vw_ref_pos_y"), compound.getInteger("vw_ref_pos_z")));
        } else {
            referencePos = Optional.empty();
        }
    }

    @Override
    protected void writeEntityToNBT(NBTTagCompound compound) {
        // Try to prevent data race
        Vec3d mountPosLocal = mountPos;
        compound.setDouble("vw_mount_pos_x", mountPosLocal.x);
        compound.setDouble("vw_mount_pos_y", mountPosLocal.y);
        compound.setDouble("vw_mount_pos_z", mountPosLocal.z);

        compound.setInteger("vw_coord_type", mountPosSpace.ordinal());

        compound.setBoolean("vw_ref_pos_present", referencePos.isPresent());
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
        Vec3d mountPosLocal = mountPos;
        packetBuffer.writeDouble(mountPosLocal.x);
        packetBuffer.writeDouble(mountPosLocal.y);
        packetBuffer.writeDouble(mountPosLocal.z);
        packetBuffer.writeInt(mountPosSpace.ordinal());
        packetBuffer.writeBoolean(referencePos.isPresent());
        if (referencePos.isPresent()) {
            packetBuffer.writeBlockPos(referencePos.get());
        }
    }

    @Override
    public void readSpawnData(ByteBuf additionalData) {
        PacketBuffer packetBuffer = new PacketBuffer(additionalData);
        mountPos = new Vec3d(packetBuffer.readDouble(), packetBuffer.readDouble(), packetBuffer.readDouble());
        mountPosSpace = CoordinateSpaceType.values()[packetBuffer.readInt()];
        if (packetBuffer.readBoolean()) {
            referencePos = Optional.of(packetBuffer.readBlockPos());
        } else {
            referencePos = Optional.empty();
        }
    }

    public Optional<BlockPos> getReferencePos() {
        return referencePos;
    }
}
