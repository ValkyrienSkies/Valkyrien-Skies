package ValkyrienWarfareCombat.Entity;

import ValkyrienWarfareBase.API.Vector;
import ValkyrienWarfareBase.CoreMod.CallRunner;
import net.minecraft.entity.Entity;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.RayTraceResult;
import net.minecraft.util.math.RayTraceResult.Type;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

public class EntityCannonBall extends Entity {

	public float explosionPower = 4f;
	private int lerpSteps;
	private double boatPitch, lerpY, lerpZ, lerpXRot, boatYaw;

	public EntityCannonBall(World worldIn) {
		super(worldIn);
		setSize(.4F, .4F);
		explosionPower = 3f;
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

		RayTraceResult traceResult = CallRunner.onRayTraceBlocks(worldObj, origin, traceEnd, false, true, false);

		if (traceResult == null || traceResult.typeOfHit == Type.MISS) {
			posX += motionX;
			posY += motionY;
			posZ += motionZ;

			// double drag = .98D;
			// motionX*=drag;
			// motionY*=drag;
			// motionZ*=drag;
			motionY -= .01;
		} else {
			if (traceResult.hitVec != null && !worldObj.isRemote) {
				processCollision(traceResult);
				kill();
			}
		}
	}

	private void processCollision(RayTraceResult collisionTrace) {
		worldObj.createExplosion(this, collisionTrace.hitVec.xCoord, collisionTrace.hitVec.yCoord, collisionTrace.hitVec.zCoord, explosionPower, true);
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
