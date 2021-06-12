package org.valkyrienskies.mod.common.compat;

import lombok.Getter;
import lombok.Setter;
import org.joml.Vector3d;
import org.valkyrienskies.mod.common.ships.ship_world.PhysicsObject;

public class VSHooks {

    @Getter
    @Setter
    private static BuoyantForceProvider buoyantForceProvider = (p, b) -> b;

    @FunctionalInterface
    public interface BuoyantForceProvider {
        Vector3d getBuoyantForce(PhysicsObject physo, Vector3d originalBuoyantForce);
    }
}
