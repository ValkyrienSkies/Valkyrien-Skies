package org.valkyrienskies.mod.common.collision;

import net.minecraft.util.math.AxisAlignedBB;
import org.joml.Vector3dc;
import org.valkyrienskies.mod.common.ships.ship_transform.ShipTransform;
import org.valkyrienskies.mod.common.ships.ship_world.PhysicsObject;
import valkyrienwarfare.api.TransformType;

public class ShipPolygon extends Polygon {

    public Vector3dc[] normals;
    public PhysicsObject shipFrom;

    public ShipPolygon(AxisAlignedBB bb, ShipTransform transformation, TransformType type,
                       Vector3dc[] norms, PhysicsObject shipFor) {
        super(bb, transformation, type);
        normals = norms;
        shipFrom = shipFor;
    }

}