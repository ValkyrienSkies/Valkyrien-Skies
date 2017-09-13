package valkyrienwarfare.physics;

import valkyrienwarfare.api.Vector;

public class Force extends Vector {

	public final boolean inLocal;

	public Force(double x, double y, double z, boolean isInLocalCoords) {
		super(x, y, z);
		inLocal = isInLocalCoords;
	}

}
