package valkyrienwarfare.mod.physmanagement.chunk;

import valkyrienwarfare.physics.management.PhysicsObject;

public class VWQueuedChunkLoadRunnable implements Runnable {

    private final int chunkX;
    private final int chunkZ;
    private final PhysicsObject parent;
    private boolean isChunkLoaded;

    public VWQueuedChunkLoadRunnable(int chunkX, int chunkZ, PhysicsObject parent) {
        this.chunkX = chunkX;
        this.chunkZ = chunkZ;
        this.parent = parent;
        this.isChunkLoaded = false;
    }

    @Override
    public void run() {
        this.isChunkLoaded = true;
        System.out.println("Chunk loaded!");
    }

    public boolean isChunkLoaded() {
        return isChunkLoaded;
    }

    public int getQueuedChunkX() {
        return chunkX;
    }

    public int getQueuedChunkZ() {
        return chunkZ;
    }

}
