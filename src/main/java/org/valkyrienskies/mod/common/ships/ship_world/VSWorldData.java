package org.valkyrienskies.mod.common.ships.ship_world;

import lombok.Getter;
import org.valkyrienskies.mod.common.ships.QueryableShipData;
import org.valkyrienskies.mod.common.ships.chunk_claims.ShipChunkAllocator;


public class VSWorldData {

    @Getter
    private final QueryableShipData queryableShipData = new QueryableShipData();

    @Getter
    private final ShipChunkAllocator shipChunkAllocator = new ShipChunkAllocator();

}
