package ValkyrienWarfareBase.Physics;

import ValkyrienWarfareBase.Math.Vector;

public class Force {

	public final Vector force;
	public final boolean inLocal;
	
	public Force(Vector theForce,boolean isInLocalCoords){
		force = theForce;
		inLocal = isInLocalCoords;
	}
	
}
