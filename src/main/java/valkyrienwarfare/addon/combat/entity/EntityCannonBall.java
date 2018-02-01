/*
 * Adapted from the Wizardry License
 *
 * Copyright (c) 2015-2017 the Valkyrien Warfare team
 *
 * Permission is hereby granted to any persons and/or organizations using this software to copy, modify, merge, publish, and distribute it.
 * Said persons and/or organizations are not allowed to use the software or any derivatives of the work for commercial use or any other means to generate income unless it is to be used as a part of a larger project (IE: "modpacks"), nor are they allowed to claim this software as their own.
 *
 * The persons and/or organizations are also disallowed from sub-licensing and/or trademarking this software without explicit permission from the Valkyrien Warfare team.
 *
 * Any persons and/or organizations using this software must disclose their source code and have it publicly available, include this license, provide sufficient credit to the original authors of the project (IE: The Valkyrien Warfare team), as well as provide a link to the original project.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND NON INFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
 */

package valkyrienwarfare.addon.combat.entity;

import net.minecraft.entity.Entity;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.RayTraceResult;
import net.minecraft.util.math.RayTraceResult.Type;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import valkyrienwarfare.api.Vector;

public class EntityCannonBall extends Entity {

    public float explosionPower;
    private int lerpSteps;
    private double boatPitch, lerpY, lerpZ, lerpXRot, boatYaw;

    public EntityCannonBall(World worldIn) {
        super(worldIn);
        setSize(.4F, .4F);
        explosionPower = 2f;
    }

    public EntityCannonBall(World worldIn, Vector velocityVector, Entity parent) {
        this(worldIn);
        motionX = velocityVector.X;
        motionY = velocityVector.Y;
        motionZ = velocityVector.Z;
        prevRotationYaw = rotationYaw = parent.rotationYaw;
        prevRotationPitch = rotationPitch = parent.rotationPitch;
        prevPosX = lastTickPosX = posX = parent.posX;
        prevPosY = lastTickPosY = posY = parent.posY;
        prevPosZ = lastTickPosZ = posZ = parent.posZ;
    }

    @Override
    protected void entityInit() {

    }

    @Override
    public void onUpdate() {
        super.onUpdate();
        tickLerp();
        // if(!worldObj.isRemote){
        processMovementForTick();
        // }

    }

    private void processMovementForTick() {
        Vec3d origin = new Vec3d(posX, posY, posZ);
        Vec3d traceEnd = origin.addVector(motionX, motionY, motionZ);

        RayTraceResult traceResult = world.rayTraceBlocks(origin, traceEnd, false, true, false);

        if (traceResult == null || traceResult.typeOfHit == Type.MISS) {
            posX += motionX;
            posY += motionY;
            posZ += motionZ;

            double drag = Math.pow(.995D, 1D / 20D);
            motionX *= drag;
            motionY *= drag;
            motionZ *= drag;
            motionY -= .05;
        } else {
            if (traceResult.hitVec != null && !world.isRemote) {
                processCollision(traceResult);
                this.setDead();
            }
        }
    }

    private void processCollision(RayTraceResult collisionTrace) {
        world.createExplosion(this, collisionTrace.hitVec.x, collisionTrace.hitVec.y, collisionTrace.hitVec.z, explosionPower, true);
    }

    private void tickLerp() {
        if (this.lerpSteps > 0 && !this.canPassengerSteer()) {
            double d0 = this.posX + (this.boatPitch - this.posX) / (double) this.lerpSteps;
            double d1 = this.posY + (this.lerpY - this.posY) / (double) this.lerpSteps;
            double d2 = this.posZ + (this.lerpZ - this.posZ) / (double) this.lerpSteps;
            double d3 = MathHelper.wrapDegrees(this.boatYaw - (double) this.rotationYaw);
            this.rotationYaw = (float) ((double) this.rotationYaw + d3 / (double) this.lerpSteps);
            this.rotationPitch = (float) ((double) this.rotationPitch + (this.lerpXRot - (double) this.rotationPitch) / (double) this.lerpSteps);
            --this.lerpSteps;
            this.setPosition(d0, d1, d2);
            this.setRotation(this.rotationYaw, this.rotationPitch);
        }
    }

    /**
     * Set the position and rotation values directly without any clamping.
     */
    @SideOnly(Side.CLIENT)
    public void setPositionAndRotationDirect(double x, double y, double z, float yaw, float pitch, int posRotationIncrements, boolean teleport) {
        posX = x;
        posY = y;
        posZ = z;
        // this.boatPitch = x;
        // this.lerpY = y;
        // this.lerpZ = z;
        // this.boatYaw = (double)yaw;
        // this.lerpXRot = (double)pitch;
        // this.lerpSteps = 0;
    }

    @Override
    protected void readEntityFromNBT(NBTTagCompound compound) {
        // TODO Auto-generated method stub

    }

    @Override
    protected void writeEntityToNBT(NBTTagCompound compound) {
        // TODO Auto-generated method stub

    }

}
