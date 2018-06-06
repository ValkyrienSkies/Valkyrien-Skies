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
import valkyrienwarfare.api.Vector;
import valkyrienwarfare.mod.capability.IAirshipCounterCapability;
import valkyrienwarfare.mod.physmanagement.interaction.ShipNameUUIDData;
import valkyrienwarfare.mod.schematics.SchematicReader.Schematic;
import valkyrienwarfare.physics.TransformType;
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

        getPhysicsObject().creator = creator.entityUniqueID.toString();
        getPhysicsObject().detectorID = detectorID;
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

        getPhysicsObject().creator = "thebest108";
        getPhysicsObject().detectorID = 0;
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
            getPhysicsObject().isNameCustom = dataManager.get(IS_NAME_CUSTOM);
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
            getPhysicsObject().coordTransform.fromLocalToGlobal(newEntityPosition);
            passenger.setPosition(newEntityPosition.X, newEntityPosition.Y, newEntityPosition.Z);
            Polygon entityBBPoly = new Polygon(inLocalAABB, getPhysicsObject().coordTransform.getCurrentTickTransform(), TransformType.LOCAL_TO_GLOBAL);

            AxisAlignedBB newEntityBB = entityBBPoly.getEnclosedAABB();
            passenger.setEntityBoundingBox(newEntityBB);
            if (passenger instanceof EntityMountingWeaponBase) {
                passenger.onUpdate();

                for (Entity e : passenger.riddenByEntities) {
                    if (getPhysicsObject().isEntityFixed(e)) {
                        Vector inLocalAgain = getPhysicsObject().getLocalPositionForEntity(e);
                        if (inLocalAgain != null) {
                            Vector newEntityPositionAgain = new Vector(inLocalAgain);
                            getPhysicsObject().coordTransform.fromLocalToGlobal(newEntityPositionAgain);

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
                    getPhysicsObject().isNameCustom = true;
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
    public AxisAlignedBB getEntityBoundingBox() {
        return Entity.ZERO_AABB;
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
        return getPhysicsObject().isNameCustom;
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
     * @param roll the roll to set
     */
    @Deprecated
    public void setRoll(double roll) {
        this.roll = roll;
    }

    /**
     * @return the yaw value being currently used by the game tick
     */
    public double getYaw() {
        return yaw;
    }

    /**
     * @param yaw the yaw to set
     */
    @Deprecated
    public void setYaw(double yaw) {
        this.yaw = yaw;
    }

    /**
     * @return the pitch value being currently used by the game tick
     */
    public double getPitch() {
        return pitch;
    }

    /**
     * @param pitch the pitch to set
     */
    @Deprecated
    public void setPitch(double pitch) {
        this.pitch = pitch;
    }

	/**
	 * @return The PhysicsObject this entity is wrapping around.
	 */
	public PhysicsObject getPhysicsObject() {
		return physicsObject;
	}
}
