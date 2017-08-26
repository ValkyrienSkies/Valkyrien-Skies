package ValkyrienWarfareBase.Collision;

import net.minecraft.entity.Entity;
import net.minecraft.util.math.AxisAlignedBB;

public class EntityPolygon extends Polygon {

	public Entity entityFor;

	public EntityPolygon(AxisAlignedBB bb, Entity ent) {
		super(bb);
		entityFor = ent;
	}

	public EntityPolygon(AxisAlignedBB bb, double[] matrix, Entity ent) {
		super(bb, matrix);
		entityFor = ent;
	}

}