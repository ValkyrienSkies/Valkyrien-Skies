package ValkyrienWarfareWorld.TileEntity;

import javax.vecmath.Vector2d;

import ValkyrienWarfareBase.NBTUtils;
import ValkyrienWarfareBase.API.Vector;
import ValkyrienWarfareBase.Physics.PhysicsCalculations;
import ValkyrienWarfareBase.Physics.PhysicsCalculationsManualControl;
import ValkyrienWarfareBase.PhysicsManagement.PhysicsObject;
import ValkyrienWarfareControl.TileEntity.ImplPhysicsProcessorNodeTileEntity;
import net.minecraft.nbt.NBTTagCompound;

public class TileEntitySkyTempleController extends ImplPhysicsProcessorNodeTileEntity {

	private Vector originPos;
	private double orbitDistance;

	double yawChangeRate = .3D;

	@Override
	public void onPhysicsTick(PhysicsObject object, PhysicsCalculations calculations, double secondsToSimulate) {
		if(calculations instanceof PhysicsCalculationsManualControl) {
			PhysicsCalculationsManualControl manualControl = (PhysicsCalculationsManualControl) calculations;

//			yawChangeRate = 1D;
			((PhysicsCalculationsManualControl) calculations).useLinearMomentumForce = true;

			if(originPos == null || originPos.isZero()) {
				setOriginPos(new Vector(object.wrapper.posX, object.wrapper.posY, object.wrapper.posZ));
			}

			manualControl.yawRate = 0D;

			Vector2d distanceFromCenter = new Vector2d(object.wrapper.posX - originPos.X, object.wrapper.posZ - originPos.Z);

//			distanceFromCenter.normalize();

			double yaw = Math.toDegrees(Math.atan2(distanceFromCenter.y, distanceFromCenter.x));

			double nextYaw = 50D;//yaw + 1000D;

			double nextOffsetX = Math.cos(nextYaw) * orbitDistance;
			double nextOffsetZ = Math.sin(nextYaw) * orbitDistance;

//			System.out.println(nextOffsetX - (distanceFromCenter.x));

//			manualControl.linearMomentum.X = nextOffsetX - object.wrapper.posX;
//			manualControl.linearMomentum.Z = nextOffsetZ - object.wrapper.posZ;
		}
	}

	public void setOriginPos(Vector newPos)	{
		originPos = newPos;
		//Minimum orbit of 40, maximum orbit of 100
		double orbitDistance =  40D + (Math.random() * 60D);
		//Assume we are at 0 degrees in our orbit
		originPos.X -= orbitDistance;

		this.markDirty();
	}

	@Override
    public void readFromNBT(NBTTagCompound compound) {
    	super.readFromNBT(compound);
    	originPos = NBTUtils.readVectorFromNBT("originPos", compound);
    	orbitDistance = compound.getDouble("orbitDistance");
    }

    @Override
    public NBTTagCompound writeToNBT(NBTTagCompound compound) {
    	compound = super.writeToNBT(compound);
    	NBTUtils.writeVectorToNBT("originPos", originPos, compound);
    	compound.setDouble("orbitDistance", orbitDistance);
        return compound;
    }

}
