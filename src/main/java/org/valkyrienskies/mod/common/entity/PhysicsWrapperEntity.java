/*
 * Adapted from the Wizardry License
 *
 * Copyright (c) 2015-2018 the Valkyrien Warfare team
 *
 * Permission is hereby granted to any persons and/or organizations using this software to copy, modify, merge, publish, and distribute it.
 * Said persons and/or organizations are not allowed to use the software or any derivatives of the work for commercial use or any other means to generate income unless it is to be used as a part of a larger project (IE: "modpacks"), nor are they allowed to claim this software as their own.
 *
 * The persons and/or organizations are also disallowed from sub-licensing and/or trademarking this software without explicit permission from the Valkyrien Warfare team.
 *
 * Any persons and/or organizations using this software must disclose their source code and have it publicly available, include this license, provide sufficient credit to the original authors of the project (IE: The Valkyrien Warfare team), as well as provide a link to the original project.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND NON INFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
 *
 */

package org.valkyrienskies.mod.common.entity;

import io.netty.buffer.ByteBuf;
import java.util.UUID;
import javax.annotation.Nullable;
import javax.annotation.ParametersAreNonnullByDefault;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.network.datasync.DataParameter;
import net.minecraft.network.datasync.DataSerializers;
import net.minecraft.network.datasync.EntityDataManager;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.world.World;
import net.minecraftforge.fml.common.registry.IEntityAdditionalSpawnData;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import org.valkyrienskies.mod.common.physics.management.PhysicsObject;
import org.valkyrienskies.mod.common.physics.management.ShipType;
import org.valkyrienskies.mod.common.physmanagement.interaction.QueryableShipData;
import org.valkyrienskies.mod.common.tileentity.TileEntityPhysicsInfuser;

/**
 * This entity's only purpose is to use the functionality of sending itself to nearby players, as
 * well as the functionality of automatically loading with the world; all other operations are
 * handled by the PhysicsObject class.
 */
@ParametersAreNonnullByDefault
public class PhysicsWrapperEntity extends Entity implements IEntityAdditionalSpawnData {

    public static final DataParameter<Boolean> IS_NAME_CUSTOM = EntityDataManager
        .createKey(PhysicsWrapperEntity.class,
            DataSerializers.BOOLEAN);
    private final PhysicsObject physicsObject;
    // TODO: Replace these raw types with something safer.
    private double pitch;
    private double yaw;
    private double roll;

    public PhysicsWrapperEntity(World worldIn) {
        super(worldIn);
        this.physicsObject = new PhysicsObject(this);
        dataManager.register(IS_NAME_CUSTOM, false);
    }

    public PhysicsWrapperEntity(World worldIn, double x, double y, double z,
        @Nullable EntityPlayer creator,
        int detectorID, ShipType shipType) {
        this(worldIn);
        posX = x;
        posY = y;
        posZ = z;

        if (creator != null) {
            getPhysicsObject().creator(creator.entityUniqueID.toString());
        } else {
            getPhysicsObject().creator("unknown");
            super.setCustomNameTag(UUID.randomUUID()
                .toString());
        }
        getPhysicsObject().setDetectorID(detectorID);
        getPhysicsObject().setShipType(shipType);
        getPhysicsObject().assembleShipAsOrderedByPlayer(creator);

        QueryableShipData.get(world).addShip(this);
    }

    public PhysicsWrapperEntity(TileEntityPhysicsInfuser te) {
        this(te.getWorld());

        posX = te.getPos()
            .getX();
        posY = te.getPos()
            .getY();
        posZ = te.getPos()
            .getZ();

        getPhysicsObject().creator("unknown");
        super.setCustomNameTag(UUID.randomUUID()
            .toString());

        getPhysicsObject().setDetectorID(0);
        getPhysicsObject().setShipType(ShipType.PHYSICS_CORE_INFUSED);
        this.physicsObject.setPhysicsInfuserPos(te.getPos());
        getPhysicsObject().assembleShipAsOrderedByPlayer(null);

        QueryableShipData.get(world).addShip(this);
    }

    @Override
    public void onUpdate() {
        if (world.isRemote) {
            getPhysicsObject().setNameCustom(dataManager.get(IS_NAME_CUSTOM));
        }
        // super.onUpdate();
        getPhysicsObject().onTick();

        firstUpdate = false;
    }

    @Override
    public void updatePassenger(Entity passenger) {

    }

    @Override
    public void setCustomNameTag(String name) {
        if (world.isRemote) {
            super.setCustomNameTag(name);
        } else if (this.getCustomNameTag().equals("")) {
            //throw new IllegalStateException("Why is this object's custom name tag empty???");
            return;
        } else {
            // Update the name registry
            QueryableShipData data = QueryableShipData.get(world);

            // bad practice: fix?
            boolean didRenameSuccessfully = data.renameShip(data.getShip(this).get(), name);

            if (didRenameSuccessfully) {
                super.setCustomNameTag(name);
                getPhysicsObject().setNameCustom(true);
                dataManager.set(IS_NAME_CUSTOM, true);
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
        return getPhysicsObject().isNameCustom();
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
