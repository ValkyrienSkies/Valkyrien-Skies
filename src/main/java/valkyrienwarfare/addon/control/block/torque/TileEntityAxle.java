package valkyrienwarfare.addon.control.block.torque;

import net.minecraft.util.EnumFacing;
import net.minecraft.util.Tuple;

import java.util.Optional;

public class TileEntityAxle extends TileEntityBasicRotationTile {

    public TileEntityAxle() {
        super();
    }

    public TileEntityAxle(EnumFacing.Axis axleAxis) {
        super();
        setAxleAxis(axleAxis);
    }

    public void setAxleAxis(EnumFacing.Axis axleAxis) {
        for (EnumFacing facing : EnumFacing.values()) {
            rotationNode.setAngularVelocityRatio(facing, Optional.empty());
        }
        Tuple<EnumFacing, EnumFacing> enumFacingFromAxis = AXIS_TO_FACING_MAP.get(axleAxis);
        rotationNode.setAngularVelocityRatio(enumFacingFromAxis.getFirst(), Optional.of(1D));
        rotationNode.setAngularVelocityRatio(enumFacingFromAxis.getSecond(), Optional.of(-1D));
    }
}
