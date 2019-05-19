package valkyrienwarfare.ship_handling;

import net.minecraft.util.math.ChunkPos;

import java.util.Iterator;

public interface IShipChunkClaims {

    Iterator<ChunkPos> getClaimedChunkPos();

    boolean isPosClaimed(ChunkPos pos);

    boolean claimPos(ChunkPos pos);

    boolean removeClaim(ChunkPos pos);

    boolean loadAllChunkClaims();

    boolean areChunkClaimsFullyLoaded();

    void initializeTransients(IWorldShipManager worldShipManager, ShipHolder shipHolder);
}
