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

package valkyrienwarfare.addon.combat.entity;

import io.netty.buffer.ByteBuf;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.DamageSource;
import net.minecraft.util.EnumActionResult;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;
import net.minecraftforge.fml.common.registry.IEntityAdditionalSpawnData;
import valkyrienwarfare.mod.common.math.RotationMatrices;
import valkyrienwarfare.mod.common.math.Vector;
import valkyrienwarfare.mod.common.physics.management.PhysicsWrapperEntity;

import javax.annotation.Nullable;

/**
 * Provides a base to easily add more weapons
 *
 * @author thebest108
 */
public abstract class EntityMountingWeaponBase extends Entity implements IEntityAdditionalSpawnData {

    public int currentTicksOperated = 0;
    public double damage = 0;
    // Default facing
    private EnumFacing facing = EnumFacing.NORTH;

    public EntityMountingWeaponBase(World worldIn) {
        super(worldIn);
        this.width = 1F;
//        this.height = 1F;
    }

    @Override
    public boolean processInitialInteract(EntityPlayer player, EnumHand hand) {
        if (player.getLowestRidingEntity() == super.getLowestRidingEntity()) {
            onRiderInteract(player, player.getHeldItem(hand), hand);
        } else {
            player.startRiding(this);

            PhysicsWrapperEntity wrapper = getParentShip();
            if (wrapper != null) {
                Vector posInLocal = new Vector(this);
                Vector passengerOffset = getRiderPositionOffset();

//				double[] rotationMatricesCompensation = RotationMatrices.getRotationMatrix(0, 45D, 0);

//				RotationMatrices.applyTransform(rotationMatricesCompensation, passengerOffset);


                wrapper.getPhysicsObject().getShipTransformationManager().fromGlobalToLocal(posInLocal);

                posInLocal.add(passengerOffset);

                wrapper.getPhysicsObject().fixEntity(player, posInLocal);
            }
        }
        return false;
    }

    @Override
    public EnumActionResult applyPlayerInteraction(EntityPlayer player, Vec3d vec, EnumHand stack) {
        return EnumActionResult.PASS;
    }

    public void setFacing(EnumFacing toSet) {
        facing = toSet;
        rotationYaw = -getBaseAngleOffset() + 90F;
    }

    public float getBaseAngleOffset() {
        switch (facing) {
            case WEST:
                return 0F;
            case SOUTH:
                return 90F;
            case EAST:
                return 180F;
            case NORTH:
                return 270F;
            default:
                return 0;
        }
    }

    public abstract void onRiderInteract(EntityPlayer player, @Nullable ItemStack stack, EnumHand hand);
    //
    // @Override
    // public boolean interactFirst(EntityPlayer playerIn){
    // if(riddenByEntity!=playerIn){
    // playerIn.mountEntity(this);
    // }else{
    // if(!worldObj.isRemote&&delay==2){
    // Vec3 look = getVectorForRotation(rotationPitch,rotationYaw);
    // if(riddenByEntity != null){
    // look = riddenByEntity.getLookVec();
    // }
    // ContactTNT tnt = new ContactTNT(worldObj);
    //
    // double speed = 2.5D;
    //
    // tnt.setPosition(posX+look.x, posY+.6+look.y, posZ+look.z);
    // tnt.motionX = look.x*speed;
    // tnt.motionY = look.y*speed;
    // tnt.motionZ = look.z*speed;
    // worldObj.spawnEntityInWorld(tnt);
    // worldObj.playSoundAtEntity(tnt, "game.tnt.primed", 1.0F, 1.0F);
    // delay=0;
    // }else{
    // delay++;
    // }
    // }
    // return true;
    // }

    @Override
    public void updateRidden() {
        // System.out.println("test");
    }

    // @Override
    // public void updateRiderPosition(){
    // if(riddenByEntity != null){
    // riddenByEntity.setPosition(posX, posY + getMountedYOffset() + riddenByEntity.getYOffset(), posZ);
    // if(!ValkyrianWarfareMod.entityFixingManager.isEntityFixed(this)){
    // rotationYaw = riddenByEntity.getRotationYawHead();
    // rotationPitch = riddenByEntity.rotationPitch;
    // }else{
    // Ship fixedOn = ValkyrianWarfareMod.entityFixingManager.getShipForFixedEntity(this);
    // if(fixedOn!=null){
    // Vec3 pointingDirection = riddenByEntity.getLook(1.0F);
    // pointingDirection = RotationMatrices.applyTransform(RotationMatrices.inverse(fixedOn.rotationTransform), pointingDirection);
    // double newPitch = math.asin(pointingDirection.y)* -180f/math.PI;
    // float f4 = (float) -math.cos(-newPitch * 0.017453292F);
    // double radianYaw = math.atan2((pointingDirection.x/f4), (pointingDirection.z/f4));
    // radianYaw+=math.PI;
    // radianYaw*= -180f/math.PI;
    // rotationYaw = (float) radianYaw;
    // rotationPitch = (float) newPitch;
    // }
    // }
    // }
    // }

    @Override
    public double getMountedYOffset() {
        return -.4D;
    }

    @Override
    public boolean canBeCollidedWith() {
        return !isDead;
    }

    @Override
    public Entity getLowestRidingEntity() {
        Entity entity;

        for (entity = this; entity.isRiding(); entity = entity.getRidingEntity()) {
        }

        return null;
    }

    @Override
    public void setPositionAndRotationDirect(double x, double y, double z, float yaw, float pitch, int rotationSteps, boolean p_180426_10_) {
        if (p_180426_10_) {
            posX = x;
            posY = y;
            posZ = z;
            rotationYaw = yaw;
            rotationPitch = pitch;
        }
    }

    @Override
    public void onUpdate() {
        super.onUpdate();
//		System.out.println("test");
        Entity rider = getRider();
        if (rider != null) {
            rotationYaw = rider.getRotationYawHead();
            rotationPitch = rider.rotationPitch;
        }
    }

    @Override
    public void updatePassenger(Entity passenger) {
        if (this.isPassenger(passenger)) {
            if (getParentShip() == null) {
                //We're in the real world
                Vector passengerOffset = getRiderPositionOffset();
                passengerOffset.add(posX, posY, posZ);
//				passenger.posX = passengerOffset.X;
//				passenger.posY = passengerOffset.Y;
//				passenger.posZ = passengerOffset.Z;
//				System.out.println("oi");
                passenger.setPosition(passengerOffset.X, passengerOffset.Y, passengerOffset.Z);
            }

//			passenger.setPosition(passengerOffset.X, passengerOffset.Y, passengerOffset.Z);
        }
    }

    public Vector getRiderPositionOffset() {
        Vector riderOffset = new Vector(.55D, 0, 0);

        double[] rotMatrix = RotationMatrices.getDoubleIdentity();
        rotMatrix = RotationMatrices.rotateOnly(rotMatrix, 0, getBaseAngleOffset(), 0);
        RotationMatrices.applyTransform(rotMatrix, riderOffset);

        riderOffset.Y += getMountedYOffset();

        return riderOffset;
    }

    // @thebest108 why??
    //     -- DaPorkchop_

	/*@Override
    public void move(MoverType type, double x, double y, double z) {
		super.move(type, x, y, z);
	}*/

    @Override
    public AxisAlignedBB getEntityBoundingBox() {
        return boundingBox;
    }

    @Override
    public AxisAlignedBB getCollisionBox(Entity entityIn) {
        return boundingBox;
    }

    @Nullable
    public Entity getRider() {
        Entity entity = this.getPassengers().isEmpty() ? null : this.getPassengers().get(0);
        return entity;
    }

    @Override
    public boolean canRiderInteract() {
        System.out.println("test");
        return true;
    }

    @Override
    @Nullable
    public Entity getRidingEntity() {
        return this.ridingEntity;
    }

    @Override
    protected void entityInit() {

    }

    @Override
    public boolean attackEntityFrom(DamageSource source, float amount) {
        if (!this.world.isRemote && !this.isDead) {
            if (this.isEntityInvulnerable(source)) {
                return false;
            } else {
                this.markVelocityChanged();
                this.setDamage(this.getDamage() + amount * 10.0F);
                boolean flag = source.getImmediateSource() instanceof EntityPlayer && ((EntityPlayer) source.getImmediateSource()).capabilities.isCreativeMode;

                if (flag || this.getDamage() > getMaxDamage()) {
                    this.removePassengers();

                    if (flag && !this.hasCustomName()) {
                        this.setDead();
                    } else {
                        this.killWeapon(source);
                    }
                }

                return true;
            }
        } else {
            return true;
        }
    }

    @Override
    public void setPosition(double x, double y, double z) {
        PhysicsWrapperEntity wrapper = getParentShip();
        float f = this.width / 2.0F;
        float f1 = this.height;

        this.posX = x;
        this.posY = y;
        this.posZ = z;

        this.setEntityBoundingBox(new AxisAlignedBB(x - (double) f, y, z - (double) f, x + (double) f, y + (double) f1, z + (double) f));
    }

    public double getDamage() {
        return damage;
    }

    public void setDamage(double toSet) {
        damage = toSet;
    }

    public double getMaxDamage() {
        return 50D;
    }

    public void killWeapon(DamageSource source) {
        this.setDead();
        if (this.world.getGameRules().getBoolean("doEntityDrops")) {
            doItemDrops();
        }
    }

    public abstract void doItemDrops();

    public PhysicsWrapperEntity getParentShip() {
        if (ridingEntity instanceof PhysicsWrapperEntity) {
            PhysicsWrapperEntity wrapper = (PhysicsWrapperEntity) ridingEntity;
            return wrapper;
        }
        return null;
    }

    @Override
    protected void readEntityFromNBT(NBTTagCompound tagCompund) {
        facing = EnumFacing.byHorizontalIndex(tagCompund.getInteger("facingOrdinal"));
    }

    @Override
    protected void writeEntityToNBT(NBTTagCompound tagCompound) {
        tagCompound.setInteger("facingOrdinal", facing.getHorizontalIndex());
    }

    @Override
    public void writeSpawnData(ByteBuf buffer) {
        buffer.writeInt(facing.getHorizontalIndex());

    }

    @Override
    public void readSpawnData(ByteBuf additionalData) {
        facing = EnumFacing.byHorizontalIndex(additionalData.readInt());
    }

}