package valkyrienwarfare.addon.control.tileentity;

import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.EnumFacing;
import valkyrienwarfare.addon.control.block.torque.TileEntityBasicRotationNodeTile;
import valkyrienwarfare.addon.control.block.torque.TileEntityBasicRotationTile;

import java.util.Optional;

public class TileEntityGearbox extends TileEntityBasicRotationNodeTile {

    private EnumFacing inputFacing;
    private Optional<Double> outputRatio;

    public TileEntityGearbox() {
        this(EnumFacing.NORTH);
    }

    public TileEntityGearbox(EnumFacing inputFacing) {
        super();
        this.inputFacing = inputFacing;
        this.outputRatio = Optional.of(1D);
        updateRotationNodeRatios();
    }

    @Override
    public void update() {
        super.update();
        this.markDirty();
    }

    public void setOutputRatio(Optional<Double> outputRatio) {
        this.outputRatio = outputRatio;
        this.updateRotationNodeRatios();
    }

    public void setInputFacing(EnumFacing inputFacing) {
        this.inputFacing = inputFacing;
        this.updateRotationNodeRatios();
    }

    private void updateRotationNodeRatios() {
        final EnumFacing inputFacingFinal = inputFacing;
        final Optional<Double> outputRatioFinal = outputRatio;
        this.rotationNode.queueTask(() -> {
            for (EnumFacing facing : EnumFacing.values()) {
                double convention = TileEntityBasicRotationTile.FACING_TO_RATIO_MAP.get(facing);
                if (facing == inputFacingFinal) {
                    rotationNode.setAngularVelocityRatio(facing, Optional.of(convention));
                } else {
                    if (outputRatioFinal.isPresent()) {
                        rotationNode.setAngularVelocityRatio(facing, Optional.of(convention * outputRatioFinal.get()));
                    } else {
                        rotationNode.setAngularVelocityRatio(facing, Optional.empty());
                    }
                }
            }
        });
    }

    @Override
    public void readFromNBT(NBTTagCompound compound) {
        super.readFromNBT(compound);
        if (compound.hasKey("inputFacing")) {
            this.inputFacing = EnumFacing.values()[compound.getByte("inputFacing")];
        }
        if (compound.hasKey("hasOutput")) {
            if (compound.getBoolean("hasOutput")) {
                if (compound.hasKey("outputRatio")) {
                    this.outputRatio = Optional.of(compound.getDouble("outpuRatio"));
                }
            }
        }
    }

    @Override
    public NBTTagCompound writeToNBT(NBTTagCompound compound) {
        super.writeToNBT(compound);
        compound.setByte("inputFacing", (byte) this.inputFacing.ordinal());
        // Use a final copy to prevent data race.
        final Optional<Double> outputRatioFinal = this.outputRatio;
        compound.setBoolean("hasOutput", outputRatioFinal.isPresent());
        if (outputRatioFinal.isPresent()) {
            compound.setDouble("outputRatio", outputRatioFinal.get());
        }
        return compound;
    }


}
