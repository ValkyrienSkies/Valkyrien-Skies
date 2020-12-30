package org.valkyrienskies.mod.common.util.datastructures;

public interface ITerrainOctreeProvider {

    IBitOctree getSolidOctree();

    IBitOctree getLiquidOctree();
}
