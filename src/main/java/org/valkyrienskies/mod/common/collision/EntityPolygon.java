package org.valkyrienskies.mod.common.collision;

import net.minecraft.entity.Entity;
import net.minecraft.util.math.AxisAlignedBB;
import org.valkyrienskies.mod.common.ships.ship_transform.ShipTransform;
import valkyrienwarfare.api.TransformType;

public class EntityPolygon extends Polygon {

    private final Entity entityFor;

    public EntityPolygon(AxisAlignedBB bb, Entity ent) {
        super(bb);
        entityFor = ent;
    }

    public EntityPolygon(AxisAlignedBB bb, ShipTransform transform, TransformType transformType,
        Entity ent) {
        super(bb, transform, transformType);
        entityFor = ent;
    }

}