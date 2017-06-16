package ValkyrienWarfareControl.TileEntity;

import ValkyrienWarfareBase.ValkyrienWarfareMod;
import ValkyrienWarfareBase.API.RotationMatrices;
import ValkyrienWarfareBase.API.Vector;
import ValkyrienWarfareBase.PhysicsManagement.PhysicsWrapperEntity;
import ValkyrienWarfareControl.Block.BlockShipHelm;
import net.minecraft.block.state.IBlockState;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.ITickable;
import net.minecraft.util.math.BlockPos;

public class TileEntityShipHelm extends TileEntity implements ITickable {

	public double compassAngle = 0;
	public double lastCompassAngle = 0;

	public double wheelRotation = 0;
	public double lastWheelRotation = 0;

	@Override
	public void update() {
		if(this.getWorld().isRemote){
			calculateCompassAngle();
//			lastWheelRotation = wheelRotation;

		}
	}

	public void calculateCompassAngle(){
		lastCompassAngle = compassAngle;

		IBlockState helmState = getWorld().getBlockState(getPos());
		EnumFacing enumfacing = (EnumFacing)helmState.getValue(BlockShipHelm.FACING);
		double wheelAndCompassStateRotation = enumfacing.getHorizontalAngle();

		BlockPos spawnPos = getWorld().getSpawnPoint();
		Vector compassPoint = new Vector(getPos().getX(), getPos().getY(), getPos().getZ());
		compassPoint.add(1D, 2, 1D);

		PhysicsWrapperEntity wrapper = ValkyrienWarfareMod.physicsManager.getObjectManagingPos(getWorld(), getPos());
		if(wrapper != null){
			RotationMatrices.applyTransform(wrapper.wrapping.coordTransform.lToWTransform, compassPoint);
		}

		Vector compassDirection = new Vector(compassPoint);
		compassDirection.subtract(spawnPos.getX(), spawnPos.getY(), spawnPos.getZ());

		if(wrapper != null){
			RotationMatrices.applyTransform(wrapper.wrapping.coordTransform.wToLRotation, compassDirection);
		}

		compassDirection.normalize();
		compassAngle = Math.toDegrees(Math.atan2(compassDirection.X, compassDirection.Z)) - wheelAndCompassStateRotation;
		compassAngle = (compassAngle + 360D)%360D;
	}
}
