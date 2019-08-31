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

package org.valkyrienskies.mod.common.physmanagement.chunk;

import java.util.Objects;
import net.minecraft.world.World;
import org.valkyrienskies.mod.common.ValkyrienSkiesMod;

/**
 * This class is responsible for finding/allocating the Chunks for PhysicsObjects; also ensures the
 * custom chunk-loading system in place
 *
 * @author thebest108
 */
public class PhysicsChunkManager {

    // The +50 is used to make sure chunks too close to ships dont interfere
    public static boolean isLikelyShipChunk(int chunkX, int chunkZ) {
        boolean likelyLegacy = chunkZ < -1870000 + 12 + 50;
        return likelyLegacy || ShipChunkAllocator.isLikelyShipChunk(chunkX, chunkZ);
    }

    PhysicsChunkManager(World worldFor) {
        worldObj = worldFor;
    }

    public final World worldObj;

    /**
     * This finds the next empty chunkSet for use, currently only increases the xPos to get new
     * positions
     */
    public VSChunkClaim getNextAvailableChunkSet(int radius) {
        IVWWorldDataCapability worldDataCapability =
            worldObj.getCapability(ValkyrienSkiesMod.vwWorldData, null);

        // TODO: Add the ship id to the allocation eventually.
        ShipChunkAllocator.ChunkAllocation allocatedChunks = Objects
            .requireNonNull(worldDataCapability)
            .getChunkAllocator()
            .allocateChunks("insert ship id here", radius);

        return new VSChunkClaim(
            allocatedChunks.lowerChunkX + ShipChunkAllocator.MAX_SHIP_CHUNK_RADIUS,
            allocatedChunks.lowerChunkZ + ShipChunkAllocator.MAX_SHIP_CHUNK_RADIUS, radius);
    }

}
