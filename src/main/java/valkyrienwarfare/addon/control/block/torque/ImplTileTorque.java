package valkyrienwarfare.addon.control.block.torque;

import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.BlockPos;

import java.util.Optional;

public class ImplTileTorque<T extends TileEntity & ITileTorqueProvider> implements ITileTorque {

    private final T tileEntity;
    private final Optional<Double>[] angularVelocityRatios;
    private double angularTorque;
    private double angularVelocity;
    private double angularRotation;

    public ImplTileTorque(T entity) {
        this.tileEntity = entity;
        // Size 6 because there are 6 sides
        this.angularVelocityRatios = new Optional[6];
        this.angularTorque = 0;
        this.angularVelocity = 0;
        this.angularRotation = 0;
    }

    @Override
    public double calculateInstantaneousTorque() {
        // TODO: Implement
        return 0;
    }

    @Override
    public Optional<Double> getAngularVelocityRatioFor(EnumFacing side) {
        return angularVelocityRatios[side.ordinal()];
    }

    private TileEntity getTileEntityFromSide(EnumFacing side) {
        BlockPos checkPos = tileEntity.getPos().add(side.getDirectionVec());
        return tileEntity.getWorld().getTileEntity(checkPos);
    }

    @Override
    public ITileTorque getTileOnSide(EnumFacing side) {
        if (!hasTileOnSide(side)) {
            throw new IllegalArgumentException("No TorqueTile on for side " + side);
        }
        return ITileTorqueProvider.class.cast(getTileOnSide(side)).getTileTorque();
    }

    @Override
    public boolean hasTileOnSide(EnumFacing side) {
        return getTileEntityFromSide(side) instanceof ITileTorqueProvider;
    }

    @Override
    public double getAngularTorque() {
        return angularTorque;
    }

    @Override
    public double getAngularVelocity() {
        return angularVelocity;
    }

    @Override
    public double getAngularRotation() {
        return angularRotation;
    }

    @Override
    public void writeToNBT(NBTTagCompound compound) {
        // TODO: Implement
    }

    @Override
    public void readFromNBT(NBTTagCompound compound) {
        // TODO: Implement
    }

}
