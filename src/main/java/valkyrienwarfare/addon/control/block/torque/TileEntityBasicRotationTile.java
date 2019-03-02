package valkyrienwarfare.addon.control.block.torque;

import com.google.common.collect.ImmutableMap;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.Tuple;

import java.util.Optional;

/**
 * A simple implementation of an IRotationNodeProvider tile entity.
 */
public class TileEntityBasicRotationTile extends TileEntity implements IRotationNodeProvider {

    // Maps EnumFacing.Axis to both possible EnumFacing values.
    public static final ImmutableMap<EnumFacing.Axis, Tuple<EnumFacing, EnumFacing>> AXIS_TO_FACING_MAP;
    static {
        AXIS_TO_FACING_MAP = ImmutableMap.of(EnumFacing.Axis.X, new Tuple<>(EnumFacing.EAST, EnumFacing.WEST), EnumFacing.Axis.Y, new Tuple<>(EnumFacing.UP, EnumFacing.DOWN), EnumFacing.Axis.Z, new Tuple<>(EnumFacing.SOUTH, EnumFacing.NORTH));
    }

    protected final IRotationNode rotationNode;

    public TileEntityBasicRotationTile() {
        super();
        this.rotationNode = new ImplRotationNode<>(this);
        this.rotationNode.setRotationalInertia(.05);
    }

    @Override
    public Optional<IRotationNode> getRotationNode() {
        if (rotationNode.isInitialized()) {
            return Optional.of(rotationNode);
        } else {
            return Optional.empty();
        }
    }

    @Override
    public void readFromNBT(NBTTagCompound compound) {
        super.readFromNBT(compound);
        rotationNode.readFromNBT(compound);
    }

    @Override
    public NBTTagCompound writeToNBT(NBTTagCompound compound) {
        super.writeToNBT(compound);
        rotationNode.writeToNBT(compound);
        return compound;
    }
}
