package org.valkyrienskies.mod.common.util;

import net.minecraft.util.math.Vec3d;
import org.valkyrienskies.mod.common.ship_handling.PhysicsObject;

public class EntityShipMountData {

    private final boolean isMounted;
    private final PhysicsObject mountedShip;
    private final Vec3d mountPos;

    protected EntityShipMountData() {
        this.isMounted = false;
        this.mountedShip = null;
        this.mountPos = null;
    }

    protected EntityShipMountData(PhysicsObject physicsObject, Vec3d mountPos) {
        this.isMounted = true;
        this.mountedShip = physicsObject;
        this.mountPos = mountPos;
    }

    public boolean isMounted() {
        return isMounted;
    }

    public PhysicsObject getMountedShip() {
        return mountedShip;
    }

    public Vec3d getMountPos() {
        return mountPos;
    }
}
