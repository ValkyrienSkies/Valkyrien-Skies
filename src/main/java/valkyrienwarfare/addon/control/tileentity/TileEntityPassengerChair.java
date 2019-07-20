package valkyrienwarfare.addon.control.tileentity;

import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.WorldServer;
import valkyrienwarfare.api.TransformType;
import valkyrienwarfare.fixes.IPhysicsChunk;
import valkyrienwarfare.mod.common.coordinates.CoordinateSpaceType;
import valkyrienwarfare.mod.common.coordinates.ShipTransform;
import valkyrienwarfare.mod.common.entity.EntityMountableChair;
import valkyrienwarfare.mod.common.physics.management.PhysicsObject;
import valkyrienwarfare.mod.common.physmanagement.relocation.IRelocationAwareTile;

import java.util.Optional;
import java.util.UUID;

public class TileEntityPassengerChair extends TileEntity implements IRelocationAwareTile {

    private Optional<UUID> chairEntityUUID;

    public TileEntityPassengerChair() {
        this.chairEntityUUID = Optional.empty();
    }

    public void tryToMountPlayerToChair(EntityPlayer player, Vec3d mountPos) {
        if (getWorld().isRemote) {
            throw new IllegalStateException("tryToMountPlayerToChair is not designed to be called on client side!");
        }
        boolean isChairEmpty;
        if (chairEntityUUID.isPresent()) {
            Entity chairEntity = ((WorldServer) getWorld()).getEntityFromUuid(chairEntityUUID.get());
            if (chairEntity != null) {
                if (chairEntity.isDead || chairEntity.isBeingRidden()) {
                    // Dead entity, chair is empty.
                    this.chairEntityUUID = Optional.empty();
                    markDirty();
                    isChairEmpty = true;
                } else {
                    // Everything checks out, this chair is not empty.
                    isChairEmpty = false;
                }
            } else {
                // Either null or not a chair entity (somehow?). Just consider this chair as empty
                this.chairEntityUUID = Optional.empty();
                markDirty();
                isChairEmpty = true;
            }
        } else {
            // No UUID for a chair entity, so this chair must be empty.
            isChairEmpty = true;
        }
        if (isChairEmpty) {
            // Chair is guaranteed empty.
            Optional<PhysicsObject> physicsObject = ((IPhysicsChunk) getWorld().getChunk(getPos())).getPhysicsObjectOptional();
            CoordinateSpaceType mountCoordType = physicsObject.isPresent() ? CoordinateSpaceType.SUBSPACE_COORDINATES : CoordinateSpaceType.GLOBAL_COORDINATES;
            EntityMountableChair entityMountable = new EntityMountableChair(getWorld(), mountPos, mountCoordType, getPos());
            chairEntityUUID = Optional.of(entityMountable.getPersistentID());
            markDirty();
            getWorld().spawnEntity(entityMountable);
            player.startRiding(entityMountable);
        }
    }

    @Override
    public NBTTagCompound writeToNBT(NBTTagCompound compound) {
        compound.setBoolean("has_chair_entity", chairEntityUUID.isPresent());
        if (chairEntityUUID.isPresent()) {
            compound.setUniqueId("chair_entity_uuid", chairEntityUUID.get());
        }
        return super.writeToNBT(compound);
    }

    @Override
    public void readFromNBT(NBTTagCompound compound) {
        if (compound.getBoolean("has_chair_entity")) {
            chairEntityUUID = Optional.of(compound.getUniqueId("chair_entity_uuid"));
        } else {
            chairEntityUUID = Optional.empty();
        }
        super.readFromNBT(compound);
    }

    public void onBlockBroken(IBlockState state) {
        if (chairEntityUUID.isPresent()) {
            // Kill the chair entity.
            Entity chairEntity = ((WorldServer) getWorld()).getEntityFromUuid(chairEntityUUID.get());
            if (chairEntity != null) {
                chairEntity.setDead();
            }
        }
    }

    @Override
    public TileEntity createRelocatedTile(BlockPos newPos, ShipTransform transform, CoordinateSpaceType coordinateSpaceType) {
        TileEntityPassengerChair relocatedTile = new TileEntityPassengerChair();
        relocatedTile.setWorld(getWorld());
        relocatedTile.setPos(newPos);

        if (chairEntityUUID.isPresent()) {
            EntityMountableChair chairEntity = (EntityMountableChair) ((WorldServer) getWorld()).getEntityFromUuid(chairEntityUUID.get());
            if (chairEntity != null) {
                Vec3d newMountPos = transform.transform(chairEntity.getMountPos(), TransformType.SUBSPACE_TO_GLOBAL);
                chairEntity.setMountValues(newMountPos, coordinateSpaceType, Optional.of(newPos));
            } else {
                chairEntityUUID = Optional.empty();
            }
        }

        relocatedTile.chairEntityUUID = this.chairEntityUUID;
        // Move everything to the new tile.
        this.chairEntityUUID = Optional.empty();
        this.markDirty();
        return relocatedTile;
    }
}
