package valkyrienwarfare.optimization;

import valkyrienwarfare.collision.PhysPolygonCollider;
import net.minecraft.block.state.IBlockState;

public class CollisionInformationHolder {
	
	public final PhysPolygonCollider collider;
	public final int inWorldX, inWorldY, inWorldZ, inLocalX, inLocalY, inLocalZ;
	public final IBlockState inWorldState, inLocalState;
	
	public CollisionInformationHolder(PhysPolygonCollider collider, int inWorldX, int inWorldY, int inWorldZ, int inLocalX, int inLocalY, int inLocalZ, IBlockState inWorldState, IBlockState inLocalState) {
		this.collider = collider;
		
		this.inWorldX = inWorldX;
		this.inWorldY = inWorldY;
		this.inWorldZ = inWorldZ;
		
		this.inLocalX = inLocalX;
		this.inLocalY = inLocalY;
		this.inLocalZ = inLocalZ;
		
		this.inWorldState = inWorldState;
		this.inLocalState = inLocalState;
	}
}
