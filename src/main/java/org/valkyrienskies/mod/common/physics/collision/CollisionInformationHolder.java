package org.valkyrienskies.mod.common.physics.collision;

import net.minecraft.block.state.IBlockState;
import org.valkyrienskies.mod.common.physics.collision.polygons.PhysPolygonCollider;

public class CollisionInformationHolder {

    protected final PhysPolygonCollider collider;
    protected final int inWorldX, inWorldY, inWorldZ, inLocalX, inLocalY, inLocalZ;
    protected final IBlockState inWorldState, inLocalState;

    public CollisionInformationHolder(PhysPolygonCollider collider, int inWorldX, int inWorldY,
        int inWorldZ, int inLocalX, int inLocalY, int inLocalZ, IBlockState inWorldState,
        IBlockState inLocalState) {
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
