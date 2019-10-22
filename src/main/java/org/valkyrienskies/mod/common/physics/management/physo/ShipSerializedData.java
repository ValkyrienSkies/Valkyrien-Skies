package org.valkyrienskies.mod.common.physics.management.physo;

import com.fasterxml.jackson.annotation.JsonBackReference;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import org.valkyrienskies.mod.common.coordinates.ShipTransform;

@Data
@Accessors(fluent = false)
@AllArgsConstructor
@NoArgsConstructor(access = AccessLevel.PACKAGE, force = true) // For Jackson
public class ShipSerializedData {

    @JsonBackReference
    private final ShipIndexedData indexed;

    /**
     * Physics information -- mutable but final. References to this <strong>should be guaranteed to
     * never change</strong> for the duration of a game.
     */
    private final ShipPhysicsData physicsData;

    private ShipTransform shipTransform;

    private AxisAlignedBB shipBB;

    /**
     * Whether or not physics are enabled on this physo
     */
    private boolean physicsEnabled;

    /**
     * The position of the physics infuser this ship has.
     */
    private BlockPos physInfuserPos;

}
