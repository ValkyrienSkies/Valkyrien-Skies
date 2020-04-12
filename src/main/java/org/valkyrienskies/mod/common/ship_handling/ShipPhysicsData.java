package org.valkyrienskies.mod.common.ship_handling;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.joml.Vector3d;
import org.joml.Vector3dc;

/**
 * Stores data used by {@link org.valkyrienskies.mod.common.physics.PhysicsCalculations}
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
public class ShipPhysicsData {
    @JsonSerialize(as = Vector3d.class)
    @JsonDeserialize(as = Vector3d.class)
    private Vector3dc linearVelocity;
    @JsonSerialize(as = Vector3d.class)
    @JsonDeserialize(as = Vector3d.class)
    private Vector3dc angularVelocity;
}
