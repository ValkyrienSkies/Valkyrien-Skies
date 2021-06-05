package org.valkyrienskies.mod.common.util.datastructures;

public interface ITerrainOctreeProvider {

    IBitOctree getSolidOctree();

    IBitOctree getLiquidOctree();

    /**
     * Octree used to mark blocks as air pockets. Used to add buoyancy force to the inside of ship hulls.
     */
    IBitOctree getAirPocketOctree();
}
