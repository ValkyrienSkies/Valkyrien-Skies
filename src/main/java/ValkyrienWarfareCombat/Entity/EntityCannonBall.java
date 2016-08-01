package ValkyrienWarfareCombat.Entity;

import ValkyrienWarfareBase.API.Vector;
import ValkyrienWarfareBase.CoreMod.CallRunner;
import net.minecraft.entity.Entity;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.math.RayTraceResult;
import net.minecraft.util.math.RayTraceResult.Type;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;

public class EntityCannonBall extends Entity{
	
	public float explosionPower = 500f;

	public EntityCannonBall(World worldIn) {
		super(worldIn);
		setSize(.4F, .4F);
	}

	public EntityCannonBall(World worldIn,Vector velocityVector,Entity parent){
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
	public void onUpdate(){
		super.onUpdate();
		processMovementForTick();
	}
	
	private void processMovementForTick(){
		Vec3d origin = new Vec3d(posX,posY,posZ);
		Vec3d traceEnd = origin.addVector(motionX, motionY, motionZ);
		
		RayTraceResult traceResult = CallRunner.onRayTraceBlocks(worldObj, origin, traceEnd, false, true, false);
		
		if(traceResult==null||traceResult.typeOfHit==Type.MISS){
			posX+=motionX;
			posY+=motionZ;
			posZ+=motionZ;
			
			double drag = .98D;
			motionX*=drag;
			motionY*=drag;
			motionZ*=drag;
			motionY -= .5;
		}else{
			processCollision(traceResult);
			kill();
		}
	}
	
	private void processCollision(RayTraceResult collisionTrace){
		worldObj.createExplosion(null, collisionTrace.hitVec.xCoord,collisionTrace.hitVec.yCoord,collisionTrace.hitVec.zCoord, explosionPower, false);
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
