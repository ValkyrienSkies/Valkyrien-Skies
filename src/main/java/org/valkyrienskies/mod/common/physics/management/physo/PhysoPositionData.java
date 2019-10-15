package org.valkyrienskies.mod.common.physics.management.physo;

import lombok.Getter;
import org.valkyrienskies.mod.common.math.Vector;

@Getter
public class PhysoPositionData {

    /**
     * Pitch, Yaw, Roll
     */
    Vector rotation = new Vector();
    /**
     * Position: X, Y, Z
     */
    Vector pos = new Vector();
    /**
     * Center Position: X, Y, Z
     */
    Vector centerPos = new Vector();

}
