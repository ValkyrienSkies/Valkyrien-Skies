package org.valkyrienskies.mod.common.physics.collision.polygons;

import net.minecraft.util.math.AxisAlignedBB;
import org.valkyrienskies.mod.common.coordinates.ShipTransform;
import org.valkyrienskies.mod.common.math.Vector;
import org.valkyrienskies.mod.common.physics.management.physo.PhysicsObject;
import valkyrienwarfare.api.TransformType;

public class ShipPolygon extends Polygon {

    public Vector[] normals;
    public PhysicsObject shipFrom;

    public ShipPolygon(AxisAlignedBB bb, ShipTransform transformation, TransformType type,
        Vector[] norms, PhysicsObject shipFor) {
        super(bb, transformation, type);
        normals = norms;
        shipFrom = shipFor;
    }

}