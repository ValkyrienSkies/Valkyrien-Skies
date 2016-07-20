package ValkyrienWarfareBase.Physics;

import ValkyrienWarfareBase.Vector;

public class Force extends Vector{

	public final boolean inLocal;
	
	public Force(double x,double y,double z,boolean isInLocalCoords){
		super(x,y,z);
		inLocal = isInLocalCoords;
	}
	
}
