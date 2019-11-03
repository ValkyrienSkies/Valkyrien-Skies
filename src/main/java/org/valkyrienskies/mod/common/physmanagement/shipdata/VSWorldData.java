package org.valkyrienskies.mod.common.physmanagement.shipdata;

import lombok.Getter;
import org.valkyrienskies.mod.common.physmanagement.chunk.ShipChunkAllocator;


public class VSWorldData {

    @Getter
    private final QueryableShipData queryableShipData = new QueryableShipData();

    @Getter
    private final ShipChunkAllocator shipChunkAllocator = new ShipChunkAllocator();

}
