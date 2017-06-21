package ValkyrienWarfareBase.Mixin.entity;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;
import org.spongepowered.asm.mixin.Shadow;

import ValkyrienWarfareBase.ValkyrienWarfareMod;
import ValkyrienWarfareBase.API.RotationMatrices;
import ValkyrienWarfareBase.API.Vector;
import ValkyrienWarfareBase.Interaction.IDraggable;
import ValkyrienWarfareBase.PhysicsManagement.CoordTransformObject;
import ValkyrienWarfareBase.PhysicsManagement.PhysicsWrapperEntity;
import net.minecraft.block.Block;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.entity.EntityPlayerSP;
import net.minecraft.entity.Entity;
import net.minecraft.entity.MoverType;
import net.minecraft.entity.projectile.EntityArrow;
import net.minecraft.util.MovementInput;
import net.minecraft.util.SoundEvent;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;

@Mixin(Entity.class)
public abstract class MixinEntity implements IDraggable {

    public PhysicsWrapperEntity worldBelowFeet;

    public Vector velocityAddedToPlayer = new Vector();

    public double yawDifVelocity;

    public boolean cancelNextMove = false;
    public boolean cancelNextMove2 = false;

    private Vector tempOverrideForMove = null;

    private boolean dontLoopForever = true;

    public Entity thisClassAsAnEntity = Entity.class.cast(this);

    @Shadow
    public float rotationYaw;

    @Shadow
    public float rotationPitch;

    @Shadow
    public float prevRotationYaw;

    @Shadow
    public float prevRotationPitch;

    @Shadow
    public boolean onGround;

    @Shadow
    public float distanceWalkedModified;

    @Shadow
    public float distanceWalkedOnStepModified;

    @Shadow
    public World world;

    @Shadow
    public double posX;

    @Shadow
    public double posY;

    @Shadow
    public double posZ;

    @Shadow
    public void setPosition(double x, double y, double z) {
    }

    @Shadow
    public boolean isSneaking() {
        return true;
    }

    @Shadow
    public void setSneaking(boolean sneaking) {
    }

    @Override
    //TODO: Finishme
    public void tickAddedVelocity() {
        if (worldBelowFeet != null && !ValkyrienWarfareMod.physicsManager.isEntityFixed(thisClassAsAnEntity)) {
            CoordTransformObject coordTransform = worldBelowFeet.wrapping.coordTransform;

            float rotYaw = this.rotationYaw;
            float rotPitch = this.rotationPitch;
            float prevYaw = this.prevRotationYaw;
            float prevPitch = this.prevRotationPitch;

            Vector oldPos = new Vector(thisClassAsAnEntity);

            RotationMatrices.applyTransform(coordTransform.prevwToLTransform, coordTransform.prevWToLRotation, thisClassAsAnEntity);
            RotationMatrices.applyTransform(coordTransform.lToWTransform, coordTransform.lToWRotation, thisClassAsAnEntity);

            Vector newPos = new Vector(thisClassAsAnEntity);

            //Move the entity back to its old position, the added velocity will be used afterwards
            this.setPosition(oldPos.X, oldPos.Y, oldPos.Z);
            Vector addedVel = oldPos.getSubtraction(newPos);

            velocityAddedToPlayer = addedVel;

            this.rotationYaw = rotYaw;
            this.rotationPitch = rotPitch;
            this.prevRotationYaw = prevYaw;
            this.prevRotationPitch = prevPitch;

            Vector oldLookingPos = new Vector(this.getLook(1.0F));
            RotationMatrices.applyTransform(coordTransform.prevWToLRotation, oldLookingPos);
            RotationMatrices.applyTransform(coordTransform.lToWRotation, oldLookingPos);

            double newPitch = Math.asin(oldLookingPos.Y) * -180D / Math.PI;
            double f4 = -Math.cos(-newPitch * 0.017453292D);
            double radianYaw = Math.atan2((oldLookingPos.X / f4), (oldLookingPos.Z / f4));
            radianYaw += Math.PI;
            radianYaw *= -180D / Math.PI;


            if (!(Double.isNaN(radianYaw) || Math.abs(newPitch) > 85)) {
                double wrappedYaw = MathHelper.wrapDegrees(radianYaw);
                double wrappedRotYaw = MathHelper.wrapDegrees(this.rotationYaw);
                double yawDif = wrappedYaw - wrappedRotYaw;
                if (Math.abs(yawDif) > 180D) {
                    if (yawDif < 0) {
                        yawDif += 360D;
                    } else {
                        yawDif -= 360D;
                    }
                }
                yawDif %= 360D;
                final double threshold = .1D;
                if (Math.abs(yawDif) < threshold) {
                    yawDif = 0D;
                }
                yawDifVelocity = yawDif;
            }
        }

        boolean onGroundOrig = this.onGround;

        if (!ValkyrienWarfareMod.physicsManager.isEntityFixed(thisClassAsAnEntity)) {
            float originalWalked = this.distanceWalkedModified;
            float originalWalkedOnStep = this.distanceWalkedOnStepModified;
            boolean originallySneaking = this.isSneaking();

            this.setSneaking(false);

            if (this.world.isRemote && EntityPlayerSP.class.isInstance(this)) {
                EntityPlayerSP playerSP = EntityPlayerSP.class.cast(this);
                MovementInput moveInput = playerSP.movementInput;
                originallySneaking = moveInput.sneak;
                moveInput.sneak = false;
            }

            this.move(MoverType.SELF, velocityAddedToPlayer.X, velocityAddedToPlayer.Y, velocityAddedToPlayer.Z);
//			CallRunner.onEntityMove(this, velocityAddedToPlayer.X, velocityAddedToPlayer.Y, velocityAddedToPlayer.Z);

            if (!(EntityPlayerSP.class.isInstance(this))) {
                if (EntityArrow.class.isInstance(this)) {
                    this.prevRotationYaw = this.rotationYaw;
                    this.rotationYaw -= yawDifVelocity;
                } else {
                    this.prevRotationYaw = this.rotationYaw;
                    this.rotationYaw += yawDifVelocity;
                }
            } else {
                if (this.world.isRemote) {
                    this.prevRotationYaw = this.rotationYaw;
                    this.rotationYaw += yawDifVelocity;
                }
            }

            //Do not add this movement as if the entity were walking it
            this.distanceWalkedModified = originalWalked;
            this.distanceWalkedOnStepModified = originalWalkedOnStep;
            this.setSneaking(originallySneaking);

            if (this.world.isRemote && EntityPlayerSP.class.isInstance(this)) {
                EntityPlayerSP playerSP = EntityPlayerSP.class.cast(this);
                MovementInput moveInput = playerSP.movementInput;
                moveInput.sneak = originallySneaking;
            }
        }

        if (onGroundOrig) {
            this.onGround = onGroundOrig;
        }

        velocityAddedToPlayer.multiply(.99D);
        yawDifVelocity *= .95D;
    }

    public PhysicsWrapperEntity getWorldBelowFeet() {
        return worldBelowFeet;
    }

    public void setWorldBelowFeet(PhysicsWrapperEntity toSet) {
        worldBelowFeet = toSet;
    }

    public Vector getVelocityAddedToPlayer() {
        return velocityAddedToPlayer;
    }

    public void setVelocityAddedToPlayer(Vector toSet) {
        velocityAddedToPlayer = toSet;
    }

    public double getYawDifVelocity() {
        return yawDifVelocity;
    }

    public void setYawDifVelocity(double toSet) {
        yawDifVelocity = toSet;
    }

    public void setCancelNextMove(boolean toSet){
    	cancelNextMove = toSet;
    }

    /*
    @Overwrite
    public Vec3d getLook(float partialTicks) {
        Vec3d original = getLookOriginal(partialTicks);

        PhysicsWrapperEntity wrapper = ValkyrienWarfareMod.physicsManager.getShipFixedOnto(Entity.class.cast(this));
        if (wrapper != null) {
            Vector newOutput = new Vector(original);
            RotationMatrices.applyTransform(wrapper.wrapping.coordTransform.RlToWRotation, newOutput);
            return newOutput.toVec3d();
        } else {
            return original;
        }
    }

    public Vec3d getLookOriginal(float partialTicks) {
        if (partialTicks == 1.0F) {
            return this.getVectorForRotation(this.rotationPitch, this.rotationYaw);
        } else {
            float f = this.prevRotationPitch + (this.rotationPitch - this.prevRotationPitch) * partialTicks;
            float f1 = this.prevRotationYaw + (this.rotationYaw - this.prevRotationYaw) * partialTicks;
            return this.getVectorForRotation(f, f1);
        }
    }*/

    @Overwrite
    protected final Vec3d getVectorForRotation(float pitch, float yaw) {
        float f = MathHelper.cos(-yaw * 0.017453292F - (float)Math.PI);
        float f1 = MathHelper.sin(-yaw * 0.017453292F - (float)Math.PI);
        float f2 = -MathHelper.cos(-pitch * 0.017453292F);
        float f3 = MathHelper.sin(-pitch * 0.017453292F);
        Vec3d vanilla = new Vec3d((double)(f1 * f2), (double)f3, (double)(f * f2));

        PhysicsWrapperEntity wrapper = ValkyrienWarfareMod.physicsManager.getShipFixedOnto(Entity.class.cast(this));
        if (wrapper != null) {
            return RotationMatrices.applyTransform(wrapper.wrapping.coordTransform.RlToWRotation, vanilla);
        }

        return vanilla;
    }

    @Shadow
    public abstract Vec3d getLook(float partialTicks);

    /*@Inject(method = "move(Lnet/minecraft/entity/MoverType;DDD)V", at = @At("HEAD"), cancellable = true)
    public void preMove(MoverType type, double dx, double dy, double dz, CallbackInfo callbackInfo) {
    	if(cancelNextMove){
    		cancelNextMove = false;
    		cancelNextMove2 = true;
    		return;
    	}

    	if(cancelNextMove2){
    		cancelNextMove2 = false;
    		callbackInfo.cancel();
    		return;
    	}

        double movDistSq = (dx * dx) + (dy * dy) + (dz * dz);
        if (movDistSq > 10000) {
            //Assume this will take us to Ship coordinates
            double newX = this.posX + dx;
            double newY = this.posY + dy;
            double newZ = this.posZ + dz;
            BlockPos newPosInBlock = new BlockPos(newX, newY, newZ);

            PhysicsWrapperEntity wrapper = ValkyrienWarfareMod.physicsManager.getObjectManagingPos(this.world, newPosInBlock);

            if (wrapper == null) {
//				Just forget this even happened
                callbackInfo.cancel();
                return;
            }

            Vector endPos = new Vector(newX, newY, newZ);
            RotationMatrices.applyTransform(wrapper.wrapping.coordTransform.wToLTransform, endPos);

            dx = endPos.X - this.posX;
            dy = endPos.Y - this.posY;
            dz = endPos.Z - this.posZ;
        }

        //callbackInfo.cancel() gets called by the method directly now
        if(EntityCollisionInjector.alterEntityMovement(thisClassAsAnEntity, dx, dy, dz, callbackInfo)){
//        	callbackInfo.cancel();
        }
    }*/



    @Shadow
    public abstract void move(MoverType type, double x, double y, double z);

    /*@Inject(method = "move(Lnet/minecraft/entity/MoverType;DDD)V", at = @At("RETURN"), cancellable = true)
    public void postMove(MoverType type, double dx, double dy, double dz, CallbackInfo callbackInfo) {

    }*/

    @Shadow
    protected void updateFallState(double y, boolean onGroundIn, IBlockState state, BlockPos pos) {}

    @Shadow
    protected boolean canTriggerWalking() {
        return true;
    }

    @Shadow
    protected SoundEvent getSwimSound() { return null; }

    @Shadow
    protected void playStepSound(BlockPos pos, Block blockIn){}

    @Shadow
    protected int getFireImmuneTicks(){
    	return 0;
    }

    @Shadow
    protected void dealFireDamage(int amount){}

    @Overwrite
    public double getDistanceSq(double x, double y, double z) {
        double d0 = this.posX - x;
        double d1 = this.posY - y;
        double d2 = this.posZ - z;
        double vanilla = d0 * d0 + d1 * d1 + d2 * d2;
        if (vanilla < 64.0D) {
            return vanilla;
        } else {
            PhysicsWrapperEntity wrapper = ValkyrienWarfareMod.physicsManager.getObjectManagingPos(this.world, new BlockPos(x, y, z));
            if (wrapper != null) {
                Vector posVec = new Vector(x, y, z);
                wrapper.wrapping.coordTransform.fromLocalToGlobal(posVec);
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

    @Overwrite
    public double getDistanceSq(BlockPos pos) {
    	double vanilla = pos.getDistance((int) posX, (int) posY, (int) posZ);
        if (vanilla < 64.0D) {
            return vanilla;
        } else {
            PhysicsWrapperEntity wrapper = ValkyrienWarfareMod.physicsManager.getObjectManagingPos(this.world, pos);
            if (wrapper != null) {
                Vector posVec = new Vector(pos.getX() + .5D, pos.getY() + .5D, pos.getZ() + .5D);
                wrapper.wrapping.coordTransform.fromLocalToGlobal(posVec);
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

}
