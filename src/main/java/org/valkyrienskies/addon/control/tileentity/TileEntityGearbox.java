package org.valkyrienskies.addon.control.tileentity;

import java.util.Optional;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.network.NetworkManager;
import net.minecraft.network.play.server.SPacketUpdateTileEntity;
import net.minecraft.util.EnumFacing;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import org.valkyrienskies.addon.control.block.torque.IRotationNode;
import org.valkyrienskies.addon.control.block.torque.TileEntityBasicRotationNodeTile;
import org.valkyrienskies.addon.control.block.torque.TileEntityBasicRotationTile;

public class TileEntityGearbox extends TileEntityBasicRotationNodeTile {

    public static final int GEARBOX_SORTING_PRIORITY = 10;
    private EnumFacing inputFacing;
    private Optional<Double> outputRatio;
    private Optional<Double>[] connectedSidesRatios;

    public TileEntityGearbox() {
        this(EnumFacing.NORTH);
    }

    public TileEntityGearbox(EnumFacing inputFacing) {
        super(GEARBOX_SORTING_PRIORITY);
        this.inputFacing = inputFacing;
        this.outputRatio = Optional.of(1D);
        this.connectedSidesRatios = new Optional[]{Optional.empty(), Optional.empty(),
            Optional.empty(), Optional.empty(), Optional.empty(), Optional.empty()};
        updateRotationNodeRatios();
    }

    @Override
    public void update() {
        super.update();
        if (!this.getWorld().isRemote) {
            Optional<Double>[] rotationNodeRatios = this.rotationNode
                .connectedRotationRatiosUnsychronized();
            for (EnumFacing facing : EnumFacing.values()) {
                if (!this.rotationNode.isConnectedToSideUnsynchronized(facing)) {
                    rotationNodeRatios[facing.ordinal()] = Optional.empty();
                }
            }
            connectedSidesRatios = rotationNodeRatios;
        }
        this.markDirty();
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
                        rotationNode.setAngularVelocityRatio(facing,
                            Optional.of(convention * outputRatioFinal.get()));
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

    @SideOnly(Side.CLIENT)
    public IRotationNode getClientNode() {
        return rotationNode;
    }

    @Override
    public SPacketUpdateTileEntity getUpdatePacket() {
        NBTTagCompound tagToSend = new NBTTagCompound();
        tagToSend.setDouble("rotation", rotation);
        tagToSend.setShort("input_facing", (byte) inputFacing.ordinal());
        byte validSides = 0;
        for (int i = 0; i < 6; i++) {
            if (connectedSidesRatios[i].isPresent()) {
                validSides |= 1 << i;
                tagToSend.setFloat("side_rotation_ratio" + i,
                    connectedSidesRatios[i].get().floatValue());
            }
        }

        tagToSend.setByte("valid_sides_byte", validSides);
        return new SPacketUpdateTileEntity(this.getPos(), 0, tagToSend);
    }

    @Override
    public void onDataPacket(NetworkManager net, SPacketUpdateTileEntity pkt) {
        nextRotation = pkt.getNbtCompound().getDouble("rotation");
        inputFacing = EnumFacing.values()[pkt.getNbtCompound()
            .getByte("input_facing")];
        byte validSidesByte = pkt.getNbtCompound().getByte("valid_sides_byte");
        for (int i = 0; i < 6; i++) {
            if ((validSidesByte & (1 << i)) != 0) {
                this.connectedSidesRatios[i] = Optional
                    .of((double) pkt.getNbtCompound().getFloat("side_rotation_ratio" + i));
            } else {
                this.connectedSidesRatios[i] = Optional.empty();
            }
        }
    }

    public Optional<Double> getOutputRatio() {
        return outputRatio;
    }

    public void setOutputRatio(Optional<Double> outputRatio) {
        this.outputRatio = outputRatio;
        this.updateRotationNodeRatios();
    }

    public Optional<Double>[] getConnectedSidesRatios() {
        return connectedSidesRatios;
    }

    /**
     * Should only be used for client rendering purposes.
     *
     * @return
     */
    @SideOnly(Side.CLIENT)
    public EnumFacing getRenderFacing() {
        return inputFacing;
    }

}
