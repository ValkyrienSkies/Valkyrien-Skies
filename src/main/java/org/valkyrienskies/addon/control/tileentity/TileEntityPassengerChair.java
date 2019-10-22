package org.valkyrienskies.addon.control.tileentity;

import java.util.Optional;
import java.util.UUID;
import mcp.MethodsReturnNonnullByDefault;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.WorldServer;
import org.valkyrienskies.mod.common.coordinates.CoordinateSpaceType;
import org.valkyrienskies.mod.common.coordinates.ShipTransform;
import org.valkyrienskies.mod.common.entity.EntityMountableChair;
import org.valkyrienskies.mod.common.physics.management.physo.PhysicsObject;
import org.valkyrienskies.mod.common.util.ValkyrienUtils;
import valkyrienwarfare.api.TransformType;

@MethodsReturnNonnullByDefault
// TODO: FIX THIS CLASS
public class TileEntityPassengerChair extends TileEntity /*implements IRelocationAwareTile*/ {

    // UUID of the mounting entity this chair is using to hold its passenger.
    private UUID chairEntityUUID;

    public TileEntityPassengerChair() {
        this.chairEntityUUID = null;
    }

    public void tryToMountPlayerToChair(EntityPlayer player, Vec3d mountPos) {
        if (getWorld().isRemote) {
            throw new IllegalStateException(
                "tryToMountPlayerToChair is not designed to be called on client side!");
        }
        boolean isChairEmpty;
        if (chairEntityUUID != null) {
            Entity chairEntity = ((WorldServer) getWorld()).getEntityFromUuid(chairEntityUUID);
            if (chairEntity != null) {
                if (chairEntity.isDead || chairEntity.isBeingRidden()) {
                    // Dead entity, chair is empty.
                    this.chairEntityUUID = null;
                    markDirty();
                    isChairEmpty = true;
                } else {
                    // Everything checks out, this chair is not empty.
                    isChairEmpty = false;
                }
            } else {
                // Either null or not a chair entity (somehow?). Just consider this chair as empty
                this.chairEntityUUID = null;
                markDirty();
                isChairEmpty = true;
            }
        } else {
            // No UUID for a chair entity, so this chair must be empty.
            isChairEmpty = true;
        }
        if (isChairEmpty) {
            // Chair is guaranteed empty.
            Optional<PhysicsObject> physicsObject = ValkyrienUtils
                .getPhysicsObject(getWorld(), getPos());
            CoordinateSpaceType mountCoordType =
                physicsObject.isPresent() ? CoordinateSpaceType.SUBSPACE_COORDINATES
                    : CoordinateSpaceType.GLOBAL_COORDINATES;
            EntityMountableChair entityMountable = new EntityMountableChair(getWorld(), mountPos,
                mountCoordType, getPos());
            chairEntityUUID = entityMountable.getPersistentID();
            markDirty();
            getWorld().spawnEntity(entityMountable);
            player.startRiding(entityMountable);
        }
    }

    @Override
    public NBTTagCompound writeToNBT(NBTTagCompound compound) {
        compound.setBoolean("has_chair_entity", chairEntityUUID != null);
        if (chairEntityUUID != null) {
            compound.setUniqueId("chair_entity_uuid", chairEntityUUID);
        }
        return super.writeToNBT(compound);
    }

    @Override
    public void readFromNBT(NBTTagCompound compound) {
        if (compound.getBoolean("has_chair_entity")) {
            chairEntityUUID = compound.getUniqueId("chair_entity_uuid");
        } else {
            chairEntityUUID = null;
        }
        super.readFromNBT(compound);
    }

    public void onBlockBroken(IBlockState state) {
        if (chairEntityUUID != null) {
            // Kill the chair entity.
            Entity chairEntity = ((WorldServer) getWorld()).getEntityFromUuid(chairEntityUUID);
            if (chairEntity != null) {
                chairEntity.setDead();
            }
        }
    }

    public TileEntity createRelocatedTile(BlockPos newPos, ShipTransform transform,
        CoordinateSpaceType coordinateSpaceType) {
        TileEntityPassengerChair relocatedTile = new TileEntityPassengerChair();
        relocatedTile.setWorld(getWorld());
        relocatedTile.setPos(newPos);

        if (chairEntityUUID != null) {
            EntityMountableChair chairEntity = (EntityMountableChair) ((WorldServer) getWorld())
                .getEntityFromUuid(chairEntityUUID);
            if (chairEntity != null) {
                Vec3d newMountPos = transform
                    .transform(chairEntity.getMountPos(), TransformType.SUBSPACE_TO_GLOBAL);
                chairEntity.setMountValues(newMountPos, coordinateSpaceType, newPos);
            } else {
                chairEntityUUID = null;
            }
        }

        relocatedTile.chairEntityUUID = this.chairEntityUUID;
        // Move everything to the new tile.
        this.chairEntityUUID = null;
        this.markDirty();
        return relocatedTile;
    }
}
