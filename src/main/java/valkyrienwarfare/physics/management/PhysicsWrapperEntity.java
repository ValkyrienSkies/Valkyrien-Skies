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

package valkyrienwarfare.physics.management;

import io.netty.buffer.ByteBuf;
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
import valkyrienwarfare.ValkyrienWarfareMod;
import valkyrienwarfare.addon.combat.entity.EntityMountingWeaponBase;
import valkyrienwarfare.api.TransformType;
import valkyrienwarfare.math.Vector;
import valkyrienwarfare.mod.capability.IAirshipCounterCapability;
import valkyrienwarfare.mod.physmanagement.interaction.ShipNameUUIDData;
import valkyrienwarfare.mod.schematics.SchematicReader.Schematic;
import valkyrienwarfare.physics.collision.polygons.Polygon;

import javax.annotation.Nullable;

/**
 * This entity's only purpose is to use the functionality of sending itself to
 * nearby players, as well as the functionality of automatically loading with
 * the world; all other operations are handled by the PhysicsObject class.
 */
public class PhysicsWrapperEntity extends Entity implements IEntityAdditionalSpawnData {

    public static final DataParameter<Boolean> IS_NAME_CUSTOM = EntityDataManager.<Boolean>createKey(Entity.class,
            DataSerializers.BOOLEAN);
    private final PhysicsObject physicsObject;
    // TODO: Replace these raw types with something safer.
    private double pitch;
    private double yaw;
    private double roll;

    public PhysicsWrapperEntity(World worldIn) {
        super(worldIn);
        this.physicsObject = new PhysicsObject(this);
        dataManager.register(IS_NAME_CUSTOM, Boolean.valueOf(false));
    }

    public PhysicsWrapperEntity(World worldIn, double x, double y, double z, @Nullable EntityPlayer creator,
                                int detectorID, ShipType shipType) {
        this(worldIn);
        posX = x;
        posY = y;
        posZ = z;

        getPhysicsObject().setCreator(creator.entityUniqueID.toString());
        getPhysicsObject().setDetectorID(detectorID);
        getPhysicsObject().setShipType(shipType);
        getPhysicsObject().processChunkClaims(creator);

        IAirshipCounterCapability counter = creator.getCapability(ValkyrienWarfareMod.airshipCounter, null);
        counter.onCreate();

        setCustomNameTagInitial(creator.getName() + ":" + counter.getAirshipCountEver());
        ShipNameUUIDData.get(worldIn).placeShipInRegistry(this, getCustomNameTag());
    }

    // TODO: Redesign this constructor
    public PhysicsWrapperEntity(World worldIn, double x, double y, double z, ShipType shipType, Schematic schematic) {
        this(worldIn);
        posX = x;
        posY = y;
        posZ = z;

        getPhysicsObject().setCreator("thebest108");
        getPhysicsObject().setDetectorID(0);
        getPhysicsObject().setShipType(shipType);

        getPhysicsObject().processChunkClaims(schematic);

        setCustomNameTagInitial("ShipRandom" + ":" + Math.random() * 10000000);
        ShipNameUUIDData.get(worldIn).placeShipInRegistry(this, getCustomNameTag());
    }

    @Override
    public void onUpdate() {
        if (isDead) {
            return;
        }
        if (world.isRemote) {
            getPhysicsObject().setNameCustom(dataManager.get(IS_NAME_CUSTOM));
        }
        // super.onUpdate();
        getPhysicsObject().onTick();

        firstUpdate = false;
    }

    @Override
    public void updatePassenger(Entity passenger) {
        Vector inLocal = getPhysicsObject().getLocalPositionForEntity(passenger);

        if (inLocal != null) {
            Vector newEntityPosition = new Vector(inLocal);
            float f = passenger.width / 2.0F;
            float f1 = passenger.height;
            AxisAlignedBB inLocalAABB = new AxisAlignedBB(newEntityPosition.X - f, newEntityPosition.Y,
                    newEntityPosition.Z - f, newEntityPosition.X + f, newEntityPosition.Y + f1,
                    newEntityPosition.Z + f);
            getPhysicsObject().getShipTransformationManager().fromLocalToGlobal(newEntityPosition);
            passenger.setPosition(newEntityPosition.X, newEntityPosition.Y, newEntityPosition.Z);
            Polygon entityBBPoly = new Polygon(inLocalAABB, getPhysicsObject().getShipTransformationManager().getCurrentTickTransform(), TransformType.SUBSPACE_TO_GLOBAL);

            AxisAlignedBB newEntityBB = entityBBPoly.getEnclosedAABB();
            passenger.setEntityBoundingBox(newEntityBB);
            if (passenger instanceof EntityMountingWeaponBase) {
                passenger.onUpdate();

                for (Entity e : passenger.riddenByEntities) {
                    if (getPhysicsObject().isEntityFixed(e)) {
                        Vector inLocalAgain = getPhysicsObject().getLocalPositionForEntity(e);
                        if (inLocalAgain != null) {
                            Vector newEntityPositionAgain = new Vector(inLocalAgain);
                            getPhysicsObject().getShipTransformationManager().fromLocalToGlobal(newEntityPositionAgain);

                            e.setPosition(newEntityPositionAgain.X, newEntityPositionAgain.Y, newEntityPositionAgain.Z);
                        }
                    }
                }
            }
        }
    }

    @Override
    public void setCustomNameTag(String name) {
        if (!world.isRemote) {
            if (getCustomNameTag() != null && !getCustomNameTag().equals("")) {
                // Update the name registry
                boolean didRenameSuccessful = ShipNameUUIDData.get(world).renameShipInRegsitry(this, name,
                        getCustomNameTag());
                if (didRenameSuccessful) {
                    super.setCustomNameTag(name);
                    getPhysicsObject().setNameCustom(true);
                    dataManager.set(IS_NAME_CUSTOM, true);
                }
            } else {
                super.setCustomNameTag(name);
            }
        } else {
            super.setCustomNameTag(name);
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

    public void setCustomNameTagInitial(String name) {
        super.setCustomNameTag(name);
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
     * Sets the position and rotation of the PhysicsWrapperEntity, and updates the pseudo
     * ship AABB (not the same as the actual collision one).
     *
     * @param posX
     * @param posY
     * @param posZ
     * @param pitch in degrees
     * @param yaw   in degrees
     * @param roll  in degrees
     */
    public void setPhysicsEntityPositionAndRotation(double posX, double posY, double posZ, double pitch, double yaw, double roll) {
        this.posX = posX;
        this.posY = posY;
        this.posZ = posZ;
        setPhysicsEntityRotation(pitch, yaw, roll);
        this.setEntityBoundingBox(new AxisAlignedBB(this.posX, this.posY, this.posZ, this.posX, this.posY, this.posZ).grow(.1D));
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
