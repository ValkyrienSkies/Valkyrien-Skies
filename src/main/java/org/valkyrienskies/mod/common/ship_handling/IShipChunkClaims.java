package org.valkyrienskies.mod.common.ship_handling;

import java.util.Iterator;
import net.minecraft.util.math.ChunkPos;

public interface IShipChunkClaims {

    Iterator<ChunkPos> getClaimedChunkPos();

    boolean isPosClaimed(ChunkPos pos);

    boolean claimPos(ChunkPos pos);

    boolean removeClaim(ChunkPos pos);

    boolean loadAllChunkClaims();

    boolean areChunkClaimsFullyLoaded();

    void initializeTransients(IWorldShipManager worldShipManager, ShipHolder shipHolder);
}
