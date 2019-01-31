package valkyrienwarfare.ship_handling;

import net.minecraft.util.math.ChunkPos;

import java.util.*;

public class ImplShipChunkClaims implements IShipChunkClaims {

    private final Set<ChunkPos> claimedPositions;

    protected ImplShipChunkClaims() {
        this.claimedPositions = new TreeSet<>();
    }

    @Override
    public Iterator<ChunkPos> getClaimedChunkPos() {
        return claimedPositions.iterator();
    }

    @Override
    public boolean isPosClaimed(ChunkPos pos) {
        return claimedPositions.contains(pos);
    }

    @Override
    public boolean removeClaim(ChunkPos pos) {
        return claimedPositions.remove(pos);
    }

    @Override
    public boolean claimPos(ChunkPos pos) {
        return claimedPositions.add(pos);
    }
}
