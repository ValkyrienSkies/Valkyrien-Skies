/*
 * Adapted from the Wizardry License
 *
 * Copyright (c) 2015-2018 the Valkyrien Warfare team
 *
 * Permission is hereby granted to any persons and/or organizations using this software to copy, modify, merge, publish, and distribute it.
 * Said persons and/or organizations are not allowed to use the software or any derivatives of the work for commercial use or any other means to generate income unless it is to be used as a part of a larger project (IE: "modpacks"), nor are they allowed to claim this software as their own.
 *
 * The persons and/or organizations are also disallowed from sub-licensing and/or trademarking this software without explicit permission from the Valkyrien Warfare team.
 *
 * Any persons and/or organizations using this software must disclose their source code and have it publicly available, include this license, provide sufficient credit to the original authors of the project (IE: The Valkyrien Warfare team), as well as provide a link to the original project.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND NON INFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
 *
 */

package valkyrienwarfare.physics.collision.optimization;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Callable;

import net.minecraft.block.state.IBlockState;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos.MutableBlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.world.chunk.Chunk;
import net.minecraft.world.chunk.storage.ExtendedBlockStorage;
import valkyrienwarfare.api.Vector;
import valkyrienwarfare.mod.physmanagement.relocation.SpatialDetector;
import valkyrienwarfare.physics.collision.CollisionInformationHolder;
import valkyrienwarfare.physics.collision.PhysPolygonCollider;
import valkyrienwarfare.physics.collision.Polygon;
import valkyrienwarfare.physics.collision.WorldPhysicsCollider;

public class ShipCollisionTask implements Callable<Void> {

	public final static int MAX_TASKS_TO_CHECK = 45;
	private final WorldPhysicsCollider toTask;
	private final int taskStartIndex;
	private final int tasksToCheck;
	private final MutableBlockPos mutablePos;
	private final MutableBlockPos inLocalPos;
	private final Vector inWorld;
	private final List<CollisionInformationHolder> collisionInformationGenerated;
	private IBlockState inWorldState;
	// public TIntArrayList foundPairs = new TIntArrayList();

	public ShipCollisionTask(WorldPhysicsCollider toTask, int taskStartIndex) {
		this.taskStartIndex = taskStartIndex;
		this.toTask = toTask;
		this.mutablePos = new MutableBlockPos();
		this.inLocalPos = new MutableBlockPos();
		this.inWorld = new Vector();
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

		return null;
	}

	public List<CollisionInformationHolder> getCollisionInformationGenerated() {
		return collisionInformationGenerated;
	}

	private void processNumber(int integer) {
		SpatialDetector.setPosWithRespectTo(integer, toTask.getCenterPotentialHit(), mutablePos);
		inWorldState = toTask.getParent().surroundingWorldChunksCache.getBlockState(mutablePos);

		inWorld.X = mutablePos.getX() + .5;
		inWorld.Y = mutablePos.getY() + .5;
		inWorld.Z = mutablePos.getZ() + .5;

		toTask.getParent().coordTransform.fromGlobalToLocal(inWorld);

		int midX = MathHelper.floor(inWorld.X + .5D);
		int midY = MathHelper.floor(inWorld.Y + .5D);
		int midZ = MathHelper.floor(inWorld.Z + .5D);

		// Check the 27 possible positions
		checkPosition(midX - 1, midY - 1, midZ - 1, integer);
		checkPosition(midX - 1, midY - 1, midZ, integer);
		checkPosition(midX - 1, midY - 1, midZ + 1, integer);
		checkPosition(midX - 1, midY, midZ - 1, integer);
		checkPosition(midX - 1, midY, midZ, integer);
		checkPosition(midX - 1, midY, midZ + 1, integer);
		checkPosition(midX - 1, midY + 1, midZ - 1, integer);
		checkPosition(midX - 1, midY + 1, midZ, integer);
		checkPosition(midX - 1, midY + 1, midZ + 1, integer);

		checkPosition(midX, midY - 1, midZ - 1, integer);
		checkPosition(midX, midY - 1, midZ, integer);
		checkPosition(midX, midY - 1, midZ + 1, integer);
		checkPosition(midX, midY, midZ - 1, integer);
		checkPosition(midX, midY, midZ, integer);
		checkPosition(midX, midY, midZ + 1, integer);
		checkPosition(midX, midY + 1, midZ - 1, integer);
		checkPosition(midX, midY + 1, midZ, integer);
		checkPosition(midX, midY + 1, midZ + 1, integer);

		checkPosition(midX + 1, midY - 1, midZ - 1, integer);
		checkPosition(midX + 1, midY - 1, midZ, integer);
		checkPosition(midX + 1, midY - 1, midZ + 1, integer);
		checkPosition(midX + 1, midY, midZ - 1, integer);
		checkPosition(midX + 1, midY, midZ, integer);
		checkPosition(midX + 1, midY, midZ + 1, integer);
		checkPosition(midX + 1, midY + 1, midZ - 1, integer);
		checkPosition(midX + 1, midY + 1, midZ, integer);
		checkPosition(midX + 1, midY + 1, midZ + 1, integer);

	}

	public void checkPosition(int x, int y, int z, int positionHash) {
		final Chunk chunkIn = toTask.getParent().VKChunkCache.getChunkAt(x >> 4, z >> 4);
		y = Math.max(0, Math.min(y, 255));

		ExtendedBlockStorage storage = chunkIn.storageArrays[y >> 4];
		if (storage != null) {
			IBitOctreeProvider provider = IBitOctreeProvider.class.cast(storage.data);
			IBitOctree octree = provider.getBitOctree();

			if (octree.get(x & 15, y & 15, z & 15)) {
				IBlockState inLocalState = chunkIn.getBlockState(x, y, z);
				// Only if you want to stop short
				// foundPairs.add(positionHash);
				// foundPairs.add(x);
				// foundPairs.add(y);
				// foundPairs.add(z);

				inLocalPos.setPos(x, y, z);

				AxisAlignedBB inLocalBB = new AxisAlignedBB(inLocalPos.getX(), inLocalPos.getY(), inLocalPos.getZ(),
						inLocalPos.getX() + 1, inLocalPos.getY() + 1, inLocalPos.getZ() + 1);
				AxisAlignedBB inGlobalBB = new AxisAlignedBB(mutablePos.getX(), mutablePos.getY(), mutablePos.getZ(),
						mutablePos.getX() + 1, mutablePos.getY() + 1, mutablePos.getZ() + 1);

				// This changes the box bounding box to the real bounding box, not sure if this
				// is better or worse for this mod
				// List<AxisAlignedBB> colBB = worldObj.getCollisionBoxes(inLocalBB);
				// inLocalBB = colBB.get(0);

				Polygon shipInWorld = new Polygon(inLocalBB, toTask.getParent().coordTransform.lToWTransform);
				Polygon worldPoly = new Polygon(inGlobalBB);

				PhysPolygonCollider collider = new PhysPolygonCollider(shipInWorld, worldPoly,
						toTask.getParent().coordTransform.normals);

				if (!collider.seperated) {
					// return handleActualCollision(collider, mutablePos, inLocalPos, inWorldState,
					// inLocalState);
					CollisionInformationHolder holder = new CollisionInformationHolder(collider, mutablePos.getX(),
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
