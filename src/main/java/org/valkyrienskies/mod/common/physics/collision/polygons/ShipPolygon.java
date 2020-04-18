package org.valkyrienskies.mod.common.physics.collision.polygons;

import net.minecraft.util.math.AxisAlignedBB;
import org.joml.Vector3dc;
import org.valkyrienskies.mod.common.coordinates.ShipTransform;
import org.valkyrienskies.mod.common.ship_handling.PhysicsObject;
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