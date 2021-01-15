package org.valkyrienskies.mod.common.collision;

import net.minecraft.block.state.IBlockState;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos.MutableBlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.world.chunk.Chunk;
import net.minecraft.world.chunk.storage.ExtendedBlockStorage;
import org.joml.Vector3d;
import org.joml.Vector3dc;
import org.valkyrienskies.mod.common.ships.block_relocation.SpatialDetector;
import org.valkyrienskies.mod.common.ships.ship_transform.ShipTransform;
import org.valkyrienskies.mod.common.util.VSIterationUtils;
import org.valkyrienskies.mod.common.util.datastructures.IBitOctree;
import org.valkyrienskies.mod.common.util.datastructures.ITerrainOctreeProvider;
import valkyrienwarfare.api.TransformType;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.ThreadLocalRandom;

public class ShipCollisionTask implements Callable<Void> {

    public final static int MAX_TASKS_TO_CHECK = 45;
    private final WorldPhysicsCollider toTask;
    private final int taskStartIndex;
    private final int tasksToCheck;
    private final MutableBlockPos mutablePos;
    private final MutableBlockPos inLocalPos;
    private final Vector3d inWorld;
    private final List<CollisionInformationHolder> collisionInformationGenerated;
    private IBlockState inWorldState;
    // public TIntArrayList foundPairs = new TIntArrayList();

    public ShipCollisionTask(WorldPhysicsCollider toTask, int taskStartIndex) {
        this.taskStartIndex = taskStartIndex;
        this.toTask = toTask;
        this.mutablePos = new MutableBlockPos();
        this.inLocalPos = new MutableBlockPos();
        this.inWorld = new Vector3d();
        this.collisionInformationGenerated = new ArrayList<>();
        this.inWorldState = null;

        int size = toTask.getCachedPotentialHitSize();
        if (taskStartIndex + MAX_TASKS_TO_CHECK > size + 1) {
            tasksToCheck = size + 1 - taskStartIndex;
        } else {
            tasksToCheck = MAX_TASKS_TO_CHECK;
        }
    }

    @Override
    public Void call() {
        for (int index = taskStartIndex; index < tasksToCheck + 1; index++) {
            int integer = toTask.getCachedPotentialHit(index);
            processNumber(integer);
        }

        // Shuffle this so that WorldPhysicsCollider performs better
        Collections.shuffle(collisionInformationGenerated, ThreadLocalRandom.current());

        return null;
    }

    public List<CollisionInformationHolder> getCollisionInformationGenerated() {
        return collisionInformationGenerated;
    }

    /**
     * Returns an iterator that loops over the collision information in quasi-random order. This is
     * important to avoid biasing one side over another, because otherwise one side would slowly
     * sink into the ground.
     *
     * @return
     */
    public Iterator<CollisionInformationHolder> getCollisionInformationIterator() {
        return collisionInformationGenerated.iterator();
    }

    private void processNumber(int integer) {
        SpatialDetector.setPosWithRespectTo(integer, toTask.getCenterPotentialHit(), mutablePos);
        inWorldState = toTask.getParent().getCachedSurroundingChunks().getBlockState(mutablePos);

        inWorld.x = mutablePos.getX() + .5;
        inWorld.y = mutablePos.getY() + .5;
        inWorld.z = mutablePos.getZ() + .5;

        toTask.getParent().getShipTransformationManager().getCurrentPhysicsTransform()
            .transformPosition(inWorld, TransformType.GLOBAL_TO_SUBSPACE);

        int midX = MathHelper.floor(inWorld.x + .5D);
        int midY = MathHelper.floor(inWorld.y + .5D);
        int midZ = MathHelper.floor(inWorld.z + .5D);

        // Check the 27 possible positions
        VSIterationUtils.expand3d(midX, midY, midZ, (x, y, z) -> checkPosition(x, y, z, integer));
    }

    // Temp variable used in checkPosition()
    private final Vector3d temp0 = new Vector3d();

    public void checkPosition(int x, int y, int z, int positionHash) {
        if (!toTask.getParent().getChunkClaim().containsChunk(x >> 4, z >> 4)) {
            return;
        }
        final Chunk chunkIn = toTask.getParent().getChunkAt(x >> 4, z >> 4);
        y = Math.max(0, Math.min(y, 255));

        ExtendedBlockStorage storage = chunkIn.storageArrays[y >> 4];
        if (storage != null) {
            ITerrainOctreeProvider provider = (ITerrainOctreeProvider) storage.data;
            IBitOctree octree = provider.getSolidOctree();

            if (octree.get(x & 15, y & 15, z & 15)) {
                IBlockState inLocalState = chunkIn.getBlockState(x, y, z);

                inLocalPos.setPos(x, y, z);

                final ShipTransform shipTransform = toTask.getParent().getShipTransformationManager().getCurrentPhysicsTransform();
                final Vector3dc shipBlockInGlobal = shipTransform.transformPositionNew(temp0.set(inLocalPos.getX() + .5, inLocalPos.getY() + .5, inLocalPos.getZ() + .5), TransformType.SUBSPACE_TO_GLOBAL);

                final double distanceSq = shipBlockInGlobal.distanceSquared(mutablePos.getX() + .5, mutablePos.getY() + .5, mutablePos.getZ() + .5);

                // If the distance between the center of two cubes is greater than sqrt(3) then it is impossible for those cubes to touch.
                // If it is less than sqrt(3) then collision is possible.
                if (distanceSq < 3) {
                    CollisionInformationHolder holder = new CollisionInformationHolder(
                        mutablePos.getX(),
                        mutablePos.getY(), mutablePos.getZ(), inLocalPos.getX(), inLocalPos.getY(),
                        inLocalPos.getZ(), inWorldState, inLocalState);

                    collisionInformationGenerated.add(holder);
                }
            }
        }
    }

    public WorldPhysicsCollider getToTask() {
        return toTask;
    }

}
