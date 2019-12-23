package org.valkyrienskies.mixin.entity;

import java.util.Optional;
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
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import org.valkyrienskies.mod.common.math.Vector;
import org.valkyrienskies.mod.common.physics.management.physo.PhysicsObject;
import org.valkyrienskies.mod.common.physmanagement.interaction.IDraggable;
import org.valkyrienskies.mod.common.util.EntityShipMountData;
import org.valkyrienskies.mod.common.util.ValkyrienUtils;
import valkyrienwarfare.api.TransformType;

@Mixin(Entity.class)
public abstract class MixinEntity implements IDraggable {

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
    private PhysicsObject worldBelowFeet;
    private PhysicsObject forcedRelativeWorldBelowFeet;
    private Vector velocityAddedToPlayer = new Vector();
    private double yawDifVelocity;
    private boolean cancelNextMove = false;
    private Vector positionInShipSpace;
    private Vector velocityInShipSpace;
    private Vector searchVector = null;

    @Override
    public PhysicsObject getWorldBelowFeet() {
        return worldBelowFeet;
    }

    @Override
    public void setWorldBelowFeet(PhysicsObject toSet) {
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
    public void setForcedRelativeSubspace(PhysicsObject toSet) {
        forcedRelativeWorldBelowFeet = toSet;
    }

    @Override
    public PhysicsObject getForcedSubspaceBelowFeet() {
        return forcedRelativeWorldBelowFeet;
    }

    /**
     * This is easier to have as an overwrite because there's less laggy hackery to be done then :P
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
            float f = this.prevRotationPitch
                + (this.rotationPitch - this.prevRotationPitch) * partialTicks;
            float f1 =
                this.prevRotationYaw + (this.rotationYaw - this.prevRotationYaw) * partialTicks;
            original = this.getVectorForRotation(f, f1);
        }
        // END VANILLA CODE

        EntityShipMountData mountData = ValkyrienUtils
            .getMountedShipAndPos(Entity.class.cast(this));
        if (mountData.isMounted()) {
            return mountData.getMountedShip()
                .getShipTransformationManager()
                .getRenderTransform()
                .rotate(original, TransformType.SUBSPACE_TO_GLOBAL);
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

        EntityShipMountData mountData = ValkyrienUtils
            .getMountedShipAndPos(Entity.class.cast(this));
        if (mountData.isMounted()) {
            return mountData.getMountedShip()
                .getShipTransformationManager()
                .getRenderTransform()
                .rotate(vanilla, TransformType.SUBSPACE_TO_GLOBAL);
        } else {
            return vanilla;
        }
    }

    @Shadow
    public abstract void move(MoverType type, double x, double y, double z);

    @Shadow
    protected abstract void copyDataFromOld(Entity entityIn);

    /**
     * This is easier to have as an overwrite because there's less laggy hackery to be done then :P
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
            Optional<PhysicsObject> physicsObject = ValkyrienUtils
                .getPhysoManagingBlock(world, new BlockPos(x, y, z));
            if (physicsObject.isPresent()) {
                Vector posVec = new Vector(x, y, z);
                physicsObject.get()
                    .getShipTransformationManager()
                    .fromLocalToGlobal(posVec);
                posVec.x -= this.posX;
                posVec.y -= this.posY;
                posVec.z -= this.posZ;
                if (vanilla > posVec.lengthSq()) {
                    return posVec.lengthSq();
                }
            }
        }
        return vanilla;
    }

    /**
     * This is easier to have as an overwrite because there's less laggy hackery to be done then :P
     *
     * @author DaPorkchop_
     */
    @Overwrite
    public double getDistanceSq(BlockPos pos) {
        double vanilla = pos.getDistance((int) posX, (int) posY, (int) posZ);
        if (vanilla < 64.0D) {
            return vanilla;
        } else {
            Optional<PhysicsObject> physicsObject = ValkyrienUtils.getPhysoManagingBlock(world, pos);
            if (physicsObject.isPresent()) {
                Vector posVec = new Vector(pos.getX() + .5D, pos.getY() + .5D, pos.getZ() + .5D);
                physicsObject.get()
                    .getShipTransformationManager()
                    .fromLocalToGlobal(posVec);
                posVec.x -= this.posX;
                posVec.y -= this.posY;
                posVec.z -= this.posZ;
                if (vanilla > posVec.lengthSq()) {
                    return posVec.lengthSq();
                }
            }
        }
        return vanilla;
    }

    @Redirect(method = "createRunningParticles", at = @At(value = "INVOKE", target = "Lnet/minecraft/util/math/MathHelper;floor(D)I", ordinal = 0))
    public int runningParticlesFirstFloor(double d) {
        PhysicsObject worldBelow = thisAsDraggable.getWorldBelowFeet();

        if (worldBelow == null) {
            searchVector = null;
            return MathHelper.floor(d);
        } else {
            searchVector = new Vector(this.posX, this.posY - 0.20000000298023224D, this.posZ);
//            searchVector.transform(worldBelow.wrapping.coordTransform.wToLTransform);
            worldBelow.getShipTransformationManager().getCurrentTickTransform()
                .transform(searchVector, TransformType.GLOBAL_TO_SUBSPACE);
            return MathHelper.floor(searchVector.x);
        }
    }

    @Redirect(method = "createRunningParticles", at = @At(value = "INVOKE", target = "Lnet/minecraft/util/math/MathHelper;floor(D)I", ordinal = 1))
    public int runningParticlesSecondFloor(double d) {
        if (searchVector == null) {
            return MathHelper.floor(d);
        } else {
            return MathHelper.floor(searchVector.y);
        }
    }

    @Redirect(method = "createRunningParticles", at = @At(value = "INVOKE", target = "Lnet/minecraft/util/math/MathHelper;floor(D)I", ordinal = 2))
    public int runningParticlesThirdFloor(double d) {
        if (searchVector == null) {
            return MathHelper.floor(d);
        } else {
            return MathHelper.floor(searchVector.z);
        }
    }

    @Shadow
    public float getEyeHeight() {
        return 0.0f;
    }

    @Inject(method = "getPositionEyes(F)Lnet/minecraft/util/math/Vec3d;", at = @At("HEAD"), cancellable = true)
    public void getPositionEyesInject(float partialTicks,
        CallbackInfoReturnable<Vec3d> callbackInfo) {
        EntityShipMountData mountData = ValkyrienUtils
            .getMountedShipAndPos(Entity.class.cast(this));

        if (mountData.isMounted()) {
            Vector playerPosition = new Vector(mountData.getMountPos());
            mountData.getMountedShip()
                .getShipTransformationManager()
                .getRenderTransform()
                .transform(playerPosition,
                    TransformType.SUBSPACE_TO_GLOBAL);

            Vector playerEyes = new Vector(0, this.getEyeHeight(), 0);
            // Remove the original position added for the player's eyes
            // RotationMatrices.doRotationOnly(wrapper.wrapping.coordTransform.lToWTransform,
            // playerEyes);
            mountData.getMountedShip()
                .getShipTransformationManager()
                .getCurrentTickTransform()
                .rotate(playerEyes, TransformType.SUBSPACE_TO_GLOBAL);
            // Add the new rotate player eyes to the position
            playerPosition.add(playerEyes);
            callbackInfo.setReturnValue(playerPosition.toVec3d());
            callbackInfo.cancel(); // return the value, as opposed to the default one
        }
    }
}
