package ValkyrienWarfareControl.TileEntity;

import ValkyrienWarfareBase.API.RotationMatrices;
import ValkyrienWarfareBase.API.Vector;
import ValkyrienWarfareBase.PhysicsManagement.PhysicsWrapperEntity;
import ValkyrienWarfareBase.ValkyrienWarfareMod;
import ValkyrienWarfareControl.Block.BlockShipHelm;
import ValkyrienWarfareControl.Piloting.ControllerInputType;
import ValkyrienWarfareControl.Piloting.PilotControlsMessage;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.network.play.server.SPacketUpdateTileEntity;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.ITickable;
import net.minecraft.util.math.BlockPos;

public class TileEntityShipHelm extends ImplTileEntityPilotable implements ITickable {

	public double compassAngle = 0;
	public double lastCompassAngle = 0;

	public double wheelRotation = 0;
	public double lastWheelRotation = 0;

	double nextWheelRotation;

	@Override
	public void update() {
		if (this.getWorld().isRemote) {
			calculateCompassAngle();

			lastWheelRotation = wheelRotation;
			wheelRotation = nextWheelRotation;
		} else {
			double toOriginRate = 5D;
			if (Math.abs(wheelRotation) < toOriginRate) {
				wheelRotation = 0;
			} else {
//            	wheelRotation -= Math.signum(wheelRotation) * wheelRotation;

				wheelRotation += -Math.signum(wheelRotation) * toOriginRate;
			}

			sendUpdatePacketToAllNearby();
		}
	}

	@Override
	public void onDataPacket(net.minecraft.network.NetworkManager net, net.minecraft.network.play.server.SPacketUpdateTileEntity pkt) {
//		lastWheelRotation = wheelRotation;

		nextWheelRotation = pkt.getNbtCompound().getDouble("wheelRotation");
	}

	@Override
	public SPacketUpdateTileEntity getUpdatePacket() {
		NBTTagCompound tagToSend = new NBTTagCompound();
		tagToSend.setDouble("wheelRotation", wheelRotation);
		return new SPacketUpdateTileEntity(this.getPos(), 0, tagToSend);
	}

	@Override
	public NBTTagCompound getUpdateTag() {
		NBTTagCompound toReturn = super.getUpdateTag();
		toReturn.setDouble("wheelRotation", wheelRotation);
		return toReturn;
	}

	public void calculateCompassAngle() {
		lastCompassAngle = compassAngle;

		IBlockState helmState = getWorld().getBlockState(getPos());
		EnumFacing enumfacing = (EnumFacing) helmState.getValue(BlockShipHelm.FACING);
		double wheelAndCompassStateRotation = enumfacing.getHorizontalAngle();

		BlockPos spawnPos = getWorld().getSpawnPoint();
		Vector compassPoint = new Vector(getPos().getX(), getPos().getY(), getPos().getZ());
		compassPoint.add(1D, 2D, 1D);

		PhysicsWrapperEntity wrapper = ValkyrienWarfareMod.physicsManager.getObjectManagingPos(getWorld(), getPos());
		if (wrapper != null) {
			RotationMatrices.applyTransform(wrapper.wrapping.coordTransform.lToWTransform, compassPoint);
		}

		Vector compassDirection = new Vector(compassPoint);
		compassDirection.subtract(spawnPos.getX(), spawnPos.getY(), spawnPos.getZ());

		if (wrapper != null) {
			RotationMatrices.applyTransform(wrapper.wrapping.coordTransform.wToLRotation, compassDirection);
		}

		compassDirection.normalize();
		compassAngle = Math.toDegrees(Math.atan2(compassDirection.X, compassDirection.Z)) - wheelAndCompassStateRotation;
		compassAngle = (compassAngle + 360D) % 360D;
	}

	public void readFromNBT(NBTTagCompound compound) {
		super.readFromNBT(compound);

		lastWheelRotation = wheelRotation = compound.getDouble("wheelRotation");
	}

	public NBTTagCompound writeToNBT(NBTTagCompound compound) {
		NBTTagCompound toReturn = super.writeToNBT(compound);

		compound.setDouble("wheelRotation", wheelRotation);

		return toReturn;
	}

	@Override
	ControllerInputType getControlInputType() {
		return ControllerInputType.ShipHelm;
	}

	@Override
	boolean setClientPilotingEntireShip() {
		return false;
	}

	@Override
	void processControlMessage(PilotControlsMessage message, EntityPlayerMP sender) {
//		System.out.println("We Gotem!");
		if (message.airshipLeft_KeyDown) {
			wheelRotation -= 10D;
		}
		if (message.airshipRight_KeyDown) {
			wheelRotation += 10D;
		}
	}

}
