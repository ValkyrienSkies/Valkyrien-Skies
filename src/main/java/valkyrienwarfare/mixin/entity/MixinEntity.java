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

package valkyrienwarfare.mixin.entity;

import net.minecraft.entity.Entity;
import net.minecraft.entity.MoverType;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;
import valkyrienwarfare.api.TransformType;
import valkyrienwarfare.mod.common.ValkyrienWarfareMod;
import valkyrienwarfare.mod.common.coordinates.CoordinateSpaceType;
import valkyrienwarfare.mod.common.coordinates.ISubspacedEntity;
import valkyrienwarfare.mod.common.coordinates.ISubspacedEntityRecord;
import valkyrienwarfare.mod.common.coordinates.VectorImmutable;
import valkyrienwarfare.mod.common.math.VWMath;
import valkyrienwarfare.mod.common.math.Vector;
import valkyrienwarfare.mod.common.physics.management.PhysicsObject;
import valkyrienwarfare.mod.common.physics.management.PhysicsWrapperEntity;
import valkyrienwarfare.mod.common.physmanagement.chunk.PhysicsChunkManager;
import valkyrienwarfare.mod.common.physmanagement.interaction.IDraggable;
import valkyrienwarfare.mod.common.util.ValkyrienUtils;

import java.util.Optional;

@Mixin(Entity.class)
public abstract class MixinEntity implements IDraggable, ISubspacedEntity {

    private final IDraggable thisAsDraggable = this;
    private final Entity thisAsEntity = Entity.class.cast(this);
    @Shadow
    public float rotationYaw;
    @Shadow
    public float rotationPitch;
    @Shadow
    public float prevRotationYaw;
    @Shadow
    public float prevRotationPitch;
    @Shadow
    public World world;
    @Shadow
    public double posX;
    @Shadow
    public double posY;
    @Shadow
    public double posZ;
    private PhysicsWrapperEntity worldBelowFeet;
    private PhysicsWrapperEntity forcedRelativeWorldBelowFeet;
    private Vector velocityAddedToPlayer = new Vector();
    private double yawDifVelocity;
    private boolean cancelNextMove = false;
    private Vector positionInShipSpace;
    private Vector velocityInShipSpace;
    private Vector searchVector = null;

    @Override
    public CoordinateSpaceType currentSubspaceType() {
        int entityChunkXPosition = ((int) posX) >> 4;
        int entityChunkZPosition = ((int) posZ) >> 4;
        boolean isInShipChunks = PhysicsChunkManager.isLikelyShipChunk(entityChunkXPosition, entityChunkZPosition);
        if (isInShipChunks) {
            return CoordinateSpaceType.SUBSPACE_COORDINATES;
        } else {
            return CoordinateSpaceType.GLOBAL_COORDINATES;
        }
    }

    @Override
    public Vector createCurrentPositionVector() {
        return new Vector(posX, posY, posZ);
    }

    @Override
    public Vector createLastTickPositionVector() {
        return new Vector(thisAsEntity.lastTickPosX, thisAsEntity.lastTickPosY, thisAsEntity.lastTickPosZ);
    }

    @Override
    public Vector createCurrentLookVector() {
        return new Vector(thisAsEntity.getLookVec());
    }

    @Override
    public Vector createCurrentVelocityVector() {
        return new Vector(thisAsEntity.motionX, thisAsEntity.motionY, thisAsEntity.motionZ);
    }

    @Override
    public void restoreSubspacedEntityStateToRecord(ISubspacedEntityRecord record) {
        VectorImmutable coordinates = record.getPosition();
        VectorImmutable coordinatesLastTick = record.getPositionLastTick();
        VectorImmutable lookVector = record.getLookDirection();
        VectorImmutable velocityVector = record.getVelocity();

        thisAsEntity.lastTickPosX = coordinatesLastTick.getX();
        thisAsEntity.lastTickPosY = coordinatesLastTick.getY();
        thisAsEntity.lastTickPosZ = coordinatesLastTick.getZ();

        double pitch = VWMath.getPitchFromVectorImmutable(lookVector);
        double yaw = VWMath.getYawFromVectorImmutable(lookVector, pitch);

        this.rotationPitch = (float) pitch;
        this.rotationYaw = (float) yaw;

        thisAsEntity.setPosition(coordinates.getX(), coordinates.getY(), coordinates.getZ());
    }

    @Override
    public int getSubspacedEntityID() {
        return thisAsEntity.getEntityId();
    }

    @Override
    public PhysicsWrapperEntity getWorldBelowFeet() {
        return worldBelowFeet;
    }

    @Override
    public void setWorldBelowFeet(PhysicsWrapperEntity toSet) {
        worldBelowFeet = toSet;
    }

    @Override
    public Vector getVelocityAddedToPlayer() {
        return velocityAddedToPlayer;
    }

    @Override
    public void setVelocityAddedToPlayer(Vector toSet) {
        velocityAddedToPlayer = toSet;
    }

    @Override
    public double getYawDifVelocity() {
        return yawDifVelocity;
    }

    @Override
    public void setYawDifVelocity(double toSet) {
        yawDifVelocity = toSet;
    }

    @Override
    public void setCancelNextMove(boolean toSet) {
        cancelNextMove = toSet;
    }

    @Override
    public void setForcedRelativeSubspace(PhysicsWrapperEntity toSet) {
        forcedRelativeWorldBelowFeet = toSet;
    }

    @Override
    public PhysicsWrapperEntity getForcedSubspaceBelowFeet() {
        return forcedRelativeWorldBelowFeet;
    }

    /**
     * This is easier to have as an overwrite because there's less laggy hackery to
     * be done then :P
     *
     * @author DaPorkchop_
     */
    @Overwrite
    public Vec3d getLook(float partialTicks) {
        // BEGIN VANILLA CODE
        Vec3d original;
        if (partialTicks == 1.0F) {
            original = this.getVectorForRotation(this.rotationPitch, this.rotationYaw);
        } else {
            float f = this.prevRotationPitch + (this.rotationPitch - this.prevRotationPitch) * partialTicks;
            float f1 = this.prevRotationYaw + (this.rotationYaw - this.prevRotationYaw) * partialTicks;
            original = this.getVectorForRotation(f, f1);
        }
        // END VANILLA CODE

        PhysicsWrapperEntity wrapper = ValkyrienWarfareMod.VW_PHYSICS_MANAGER.getShipFixedOnto(Entity.class.cast(this));
        if (wrapper != null) {
            return wrapper.getPhysicsObject().getShipTransformationManager().getRenderTransform().rotate(original, TransformType.SUBSPACE_TO_GLOBAL);
        } else {
            return original;
        }
    }

    /**
     * fix a warning
     *
     * @author asdf
     */
    @Overwrite
    protected final Vec3d getVectorForRotation(float pitch, float yaw) {
        // BEGIN VANILLA CODE
        float f = MathHelper.cos(-yaw * 0.017453292F - (float) Math.PI);
        float f1 = MathHelper.sin(-yaw * 0.017453292F - (float) Math.PI);
        float f2 = -MathHelper.cos(-pitch * 0.017453292F);
        float f3 = MathHelper.sin(-pitch * 0.017453292F);
        Vec3d vanilla = new Vec3d(f1 * f2, f3, f * f2);
        // END VANILLA CODE

        PhysicsWrapperEntity wrapper = ValkyrienWarfareMod.VW_PHYSICS_MANAGER.getShipFixedOnto(Entity.class.cast(this));
        if (wrapper != null) {
            return wrapper.getPhysicsObject().getShipTransformationManager().getRenderTransform().rotate(vanilla, TransformType.SUBSPACE_TO_GLOBAL);
        }

        return vanilla;
    }

    @Shadow
    public abstract void move(MoverType type, double x, double y, double z);

    @Shadow
    protected abstract void copyDataFromOld(Entity entityIn);

    /**
     * This is easier to have as an overwrite because there's less laggy hackery to
     * be done then :P
     *
     * @author DaPorkchop_
     */
    @Overwrite
    public double getDistanceSq(double x, double y, double z) {
        double d0 = this.posX - x;
        double d1 = this.posY - y;
        double d2 = this.posZ - z;
        double vanilla = d0 * d0 + d1 * d1 + d2 * d2;
        if (vanilla < 64.0D) {
            return vanilla;
        } else {
            Optional<PhysicsObject> physicsObject = ValkyrienUtils.getPhysicsObject(world, new BlockPos(x, y, z));
            if (physicsObject.isPresent()) {
                Vector posVec = new Vector(x, y, z);
                physicsObject.get()
                        .getShipTransformationManager()
                        .fromLocalToGlobal(posVec);
                posVec.X -= this.posX;
                posVec.Y -= this.posY;
                posVec.Z -= this.posZ;
                if (vanilla > posVec.lengthSq()) {
                    return posVec.lengthSq();
                }
            }
        }
        return vanilla;
    }

    /**
     * This is easier to have as an overwrite because there's less laggy hackery to
     * be done then :P
     *
     * @author DaPorkchop_
     */
    @Overwrite
    public double getDistanceSq(BlockPos pos) {
        double vanilla = pos.getDistance((int) posX, (int) posY, (int) posZ);
        if (vanilla < 64.0D) {
            return vanilla;
        } else {
            Optional<PhysicsObject> physicsObject = ValkyrienUtils.getPhysicsObject(world, pos);
            if (physicsObject.isPresent()) {
                Vector posVec = new Vector(pos.getX() + .5D, pos.getY() + .5D, pos.getZ() + .5D);
                physicsObject.get()
                        .getShipTransformationManager()
                        .fromLocalToGlobal(posVec);
                posVec.X -= this.posX;
                posVec.Y -= this.posY;
                posVec.Z -= this.posZ;
                if (vanilla > posVec.lengthSq()) {
                    return posVec.lengthSq();
                }
            }
        }
        return vanilla;
    }

    @Redirect(method = "createRunningParticles", at = @At(value = "INVOKE", target = "Lnet/minecraft/util/math/MathHelper;floor(D)I", ordinal = 0))
    public int runningParticlesFirstFloor(double d) {
        PhysicsWrapperEntity worldBelow = thisAsDraggable.getWorldBelowFeet();

        if (worldBelow == null) {
            searchVector = null;
            return MathHelper.floor(d);
        } else {
            searchVector = new Vector(this.posX, this.posY - 0.20000000298023224D, this.posZ);
//            searchVector.transform(worldBelow.wrapping.coordTransform.wToLTransform);
            worldBelow.getPhysicsObject().getShipTransformationManager().getCurrentTickTransform().transform(searchVector, TransformType.GLOBAL_TO_SUBSPACE);
            return MathHelper.floor(searchVector.X);
        }
    }

    @Redirect(method = "createRunningParticles", at = @At(value = "INVOKE", target = "Lnet/minecraft/util/math/MathHelper;floor(D)I", ordinal = 1))
    public int runningParticlesSecondFloor(double d) {
        if (searchVector == null) {
            return MathHelper.floor(d);
        } else {
            return MathHelper.floor(searchVector.Y);
        }
    }

    @Redirect(method = "createRunningParticles", at = @At(value = "INVOKE", target = "Lnet/minecraft/util/math/MathHelper;floor(D)I", ordinal = 2))
    public int runningParticlesThirdFloor(double d) {
        if (searchVector == null) {
            return MathHelper.floor(d);
        } else {
            return MathHelper.floor(searchVector.Z);
        }
    }
}
