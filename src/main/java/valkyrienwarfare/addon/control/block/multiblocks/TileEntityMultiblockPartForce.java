package valkyrienwarfare.addon.control.block.multiblocks;

import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.math.BlockPos;
import valkyrienwarfare.addon.control.MultiblockRegistry;
import valkyrienwarfare.addon.control.nodenetwork.IForceTile;
import valkyrienwarfare.math.Vector;
import valkyrienwarfare.physics.management.PhysicsObject;

public abstract class TileEntityMultiblockPartForce<E extends IMulitblockSchematic> extends TileEntityMultiblockPart<E> implements IForceTile {

	private double thrustMultiplierGoal;
	private double maxThrust;
	
	public TileEntityMultiblockPartForce() {
		super();
		this.thrustMultiplierGoal = 0;
		this.maxThrust = 0;
	}

	@Override
	public double getMaxThrust() {
		return maxThrust;
	}

	@Override
	public double getThrustMultiplierGoal() {
		return thrustMultiplierGoal;
	}

	@Override
	public void setThrustMultiplierGoal(double thrustMultiplierGoal) {
		this.thrustMultiplierGoal = thrustMultiplierGoal;
	}
	
	@Override
	public void setMaxThrust(double maxThrust) {
		this.maxThrust = maxThrust;
	}

	@Override
	public NBTTagCompound writeToNBT(NBTTagCompound compound) {
		NBTTagCompound toReturn = super.writeToNBT(compound);
		toReturn.setDouble("thrustMultiplierGoal", thrustMultiplierGoal);
		toReturn.setDouble("maxThrust", maxThrust);
		return toReturn;
	}
	
	@Override
	public void readFromNBT(NBTTagCompound compound) {
		super.readFromNBT(compound);
		this.thrustMultiplierGoal = compound.getDouble("thrustMultiplierGoal");
		this.maxThrust = compound.getDouble("maxThrust");
	}
	
}
