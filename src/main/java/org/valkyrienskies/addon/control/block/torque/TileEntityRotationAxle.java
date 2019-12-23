package org.valkyrienskies.addon.control.block.torque;

import java.util.Optional;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.Tuple;

public class TileEntityRotationAxle extends TileEntityBasicRotationTile {

    // Used internally by Minecraft
    @SuppressWarnings("WeakerAccess")
    public TileEntityRotationAxle() {
    }

    public TileEntityRotationAxle(EnumFacing.Axis axleAxis) {
        super();
        setAxleAxis(axleAxis);
    }

    public void setAxleAxis(EnumFacing.Axis axleAxis) {
        this.rotationNode.queueTask(() -> {
            for (EnumFacing facing : EnumFacing.values()) {
                rotationNode.setAngularVelocityRatio(facing, Optional.empty());
            }
            Tuple<EnumFacing, EnumFacing> enumFacingFromAxis = AXIS_TO_FACING_MAP.get(axleAxis);
            rotationNode.setAngularVelocityRatio(enumFacingFromAxis.getFirst(), Optional.of(1D));
            rotationNode.setAngularVelocityRatio(enumFacingFromAxis.getSecond(), Optional.of(-1D));
        });
    }
}
