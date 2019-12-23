package org.valkyrienskies.mod.common.entity;

import io.netty.buffer.ByteBuf;
import javax.annotation.ParametersAreNonnullByDefault;
import net.minecraft.entity.Entity;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.world.World;
import net.minecraftforge.fml.common.registry.IEntityAdditionalSpawnData;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import org.valkyrienskies.mod.common.multithreaded.TickSyncCompletableFuture;
import org.valkyrienskies.mod.common.physics.management.PhysicsObject;
import org.valkyrienskies.mod.common.physmanagement.relocation.DetectorManager.DetectorIDs;
import org.valkyrienskies.mod.common.physmanagement.shipdata.QueryableShipData;
import org.valkyrienskies.mod.common.tileentity.TileEntityPhysicsInfuser;
import org.valkyrienskies.mod.common.util.ValkyrienUtils;
import org.valkyrienskies.mod.common.util.names.NounListNameGenerator;

/**
 * This entity's only purpose is to use the functionality of sending itself to nearby players, as
 * well as the functionality of automatically loading with the world; all other operations are
 * handled by the PhysicsObject class.
 *
 * This is scheduled for deletion
 * @deprecated
 */
@ParametersAreNonnullByDefault
@Deprecated
public class PhysicsWrapperEntity extends Entity implements IEntityAdditionalSpawnData {

    private final PhysicsObject physicsObject;
    // TODO: Replace these raw types with something safer.
    private double pitch;
    private double yaw;
    private double roll;

    public PhysicsWrapperEntity(World worldIn) {
        super(worldIn);
        this.physicsObject = new PhysicsObject(this);
    }

    public static TickSyncCompletableFuture<PhysicsWrapperEntity> createWrapperEntity(
        TileEntityPhysicsInfuser te, DetectorIDs detectorID) {
        PhysicsWrapperEntity w = new PhysicsWrapperEntity(te.getWorld());

        w.posX = te.getPos()
            .getX();
        w.posY = te.getPos()
            .getY();
        w.posZ = te.getPos()
            .getZ();

        w.getPhysicsObject().setCreator("unknown");
        w.superSetCustomName(NounListNameGenerator.getInstance().generateName());

        w.getPhysicsObject().setDetectorID(detectorID);
        w.physicsObject.setPhysicsInfuserPos(te.getPos());
        return w.getPhysicsObject().assembleShipAsOrderedByPlayer(null).thenRun(() -> {
            System.out.println("Adding ship in thread " + Thread.currentThread().getName());
            ValkyrienUtils.getQueryableData(te.getWorld()).addShip(w);
        }).thenApply(any -> w);
    }

    private void superSetCustomName(String name) {
        super.setCustomNameTag(name);
    }

    @Override
    public void onUpdate() {
        // super.onUpdate();
        getPhysicsObject().onTick();

        firstUpdate = false;
    }

    @Override
    public void updatePassenger(Entity passenger) { }

    @Override
    public void setCustomNameTag(String name) {
        if (world.isRemote) {
            super.setCustomNameTag(name);
        } else if (this.getCustomNameTag().equals("")) {
            //throw new IllegalStateException("Why is this object's custom name tag empty???");
            return;
        } else {
            // Update the name registry
            QueryableShipData data = ValkyrienUtils.getQueryableData(world);

            // bad practice: fix?
            boolean didRenameSuccessfully = data.renameShip(data.getShip(this).get(), name);

            if (didRenameSuccessfully) {
                super.setCustomNameTag(name);
            }
        }
    }

    @Override
    public void setPositionAndUpdate(double x, double y, double z) {

    }

    @Override
    protected void entityInit() {

    }

    @Override
    @SideOnly(Side.CLIENT)
    public AxisAlignedBB getRenderBoundingBox() {
        return getPhysicsObject().getShipBoundingBox();
    }

    @Override
    public void setPosition(double x, double y, double z) {

    }

    @Override
    public void setLocationAndAngles(double x, double y, double z, float yaw, float pitch) {

    }

    @Override
    protected boolean canFitPassenger(Entity passenger) {
        return true;
    }

    @Override
    @SideOnly(Side.CLIENT)
    public void setPositionAndRotationDirect(double x, double y, double z, float yaw, float pitch,
        int posRotationIncrements, boolean teleport) {
    }

    @Override
    @SideOnly(Side.CLIENT)
    public boolean getAlwaysRenderNameTagForRender() {
        return false;
    }

    @Override
    protected void readEntityFromNBT(NBTTagCompound tagCompound) {
        getPhysicsObject().readFromNBTTag(tagCompound);
    }

    @Override
    protected void writeEntityToNBT(NBTTagCompound tagCompound) {
        getPhysicsObject().writeToNBTTag(tagCompound);
    }

    @Override
    public void writeSpawnData(ByteBuf buffer) {
        getPhysicsObject().preloadNewPlayers();
        getPhysicsObject().writeSpawnData(buffer);
    }

    @Override
    public void readSpawnData(ByteBuf additionalData) {
        getPhysicsObject().readSpawnData(additionalData);
    }

    /**
     * @return the roll value being currently used by the game tick
     */
    public double getRoll() {
        return roll;
    }

    /**
     * @return the yaw value being currently used by the game tick
     */
    public double getYaw() {
        return yaw;
    }

    /**
     * @return the pitch value being currently used by the game tick
     */
    public double getPitch() {
        return pitch;
    }

    /**
     * @return The PhysicsObject this entity is wrapping around.
     */
    public PhysicsObject getPhysicsObject() {
        return physicsObject;
    }

    /**
     * Sets the position and rotation of the PhysicsWrapperEntity, and updates the pseudo ship AABB
     * (not the same as the actual collision one).
     *
     * @param posX
     * @param posY
     * @param posZ
     * @param pitch in degrees
     * @param yaw   in degrees
     * @param roll  in degrees
     */
    public void setPhysicsEntityPositionAndRotation(double posX, double posY, double posZ,
        double pitch, double yaw, double roll) {
        this.posX = posX;
        this.posY = posY;
        this.posZ = posZ;
        setPhysicsEntityRotation(pitch, yaw, roll);
    }

    @Override
    public AxisAlignedBB getEntityBoundingBox() {
        return physicsObject.getShipBoundingBox();
    }


    /**
     * Sets the lastTickPos fields to be the current position. Only should be used by the client.
     */
    public void physicsUpdateLastTickPositions() {
        this.lastTickPosX = this.posX;
        this.lastTickPosY = this.posY;
        this.lastTickPosZ = this.posZ;
    }

    /**
     * Sets the rotation of this PhysicsWrapperEntity to the given values.
     *
     * @param pitch in degrees
     * @param yaw   in degrees
     * @param roll  in degrees
     */
    public void setPhysicsEntityRotation(double pitch, double yaw, double roll) {
        this.pitch = pitch;
        this.yaw = yaw;
        this.roll = roll;
    }
}
