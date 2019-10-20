/*
 * Adapted from the Wizardry License
 *
 * Copyright (c) 2015-2019 the Valkyrien Skies team
 *
 * Permission is hereby granted to any persons and/or organizations using this software to copy, modify, merge, publish, and distribute it.
 * Said persons and/or organizations are not allowed to use the software or any derivatives of the work for commercial use or any other means to generate income unless it is to be used as a part of a larger project (IE: "modpacks"), nor are they allowed to claim this software as their own.
 *
 * The persons and/or organizations are also disallowed from sub-licensing and/or trademarking this software without explicit permission from the Valkyrien Skies team.
 *
 * Any persons and/or organizations using this software must disclose their source code and have it publicly available, include this license, provide sufficient credit to the original authors of the project (IE: The Valkyrien Skies team), as well as provide a link to the original project.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND NON INFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
 *
 */

package org.valkyrienskies.mod.common.physmanagement.chunk;

import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Streams;
import java.util.Iterator;
import java.util.NoSuchElementException;
import java.util.stream.Stream;
import javax.annotation.concurrent.Immutable;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;
import lombok.Value;
import lombok.experimental.Accessors;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.ChunkPos;

/**
 * This stores the chunk claims for a PhysicsObject; not the chunks themselves
 *
 * @author thebest108
 */
@Immutable
@Accessors(fluent = false)
@Value
@AllArgsConstructor
@NoArgsConstructor(access = AccessLevel.PRIVATE, force = true) // For Jackson
public final class VSChunkClaim {

    private final int centerX;
    private final int centerZ;
    private final int radius;

    // NON-DATA CACHE FIELDS
    private final transient ImmutableSet<Long> chunkLongs = calculateChunkLongs();

    public VSChunkClaim(NBTTagCompound readFrom) {
        this(readFrom.getInteger("centerX"), readFrom.getInteger("centerZ"),
            readFrom.getInteger("radius"));
    }

    public void writeToNBT(NBTTagCompound toSave) {
        toSave.setInteger("centerX", getCenterX());
        toSave.setInteger("centerZ", getCenterZ());
        toSave.setInteger("radius", getRadius());
    }

    /**
     * Checks if a chunk is contained within this {@link VSChunkClaim}
     *
     * @param chunkX The X value of the chunk
     * @param chunkZ The Y value of the chunk
     * @return True if the specified chunk is contained within this {@link VSChunkClaim}
     */
    public boolean containsChunk(int chunkX, int chunkZ) {
        boolean inX = (chunkX >= minX()) && (chunkX <= maxX());
        boolean inZ = (chunkZ >= minZ()) && (chunkZ <= maxZ());
        return inX && inZ;
    }

    public boolean containsChunk(ChunkPos pos) {
        return containsChunk(pos.x, pos.z);
    }

    /**
     * Checks if a block is contained within this {@link VSChunkClaim}
     *
     * @return True if the specified block is contained within this {@link VSChunkClaim}
     */
    public boolean containsBlock(BlockPos pos) {
        return containsChunk(pos.getX() >> 4, pos.getZ() >> 4);
    }

    public ChunkPos absoluteToRelative(ChunkPos pos) {
        return new ChunkPos(pos.x - minX(), pos.z - minZ());
    }

    public ChunkPos relativeToAbsolute(ChunkPos pos) {
        return new ChunkPos(pos.x + minX(), pos.z + minZ());
    }

    @Override
    public String toString() {
        return getCenterX() + ":" + getCenterZ() + ":" + getRadius();
    }

    /**
     * @return A stream of the {@link ChunkPos} of every chunk inside of this claim.
     */
    public Stream<ChunkPos> stream() {
        return Streams.stream(new ChunkPosIterator());
    }

    /**
     * @return the maxX
     */
    public int maxX() {
        return getCenterX() + getRadius();
    }

    /**
     * @return the maxZ
     */
    public int maxZ() {
        return getCenterZ() + getRadius();
    }

    /**
     * @return the minZ
     */
    public int minZ() {
        return getCenterZ() - getRadius();
    }

    /**
     * @return the minX
     */
    public int minX() {
        return getCenterX() - getRadius();
    }

    /**
     * @return the size of this chunk claim. E.g., if the chunk claim has a radius of 2, then it is
     * 5x5 and the dimension is 5
     */
    public int dimension() {
        return getRadius() * 2 + 1;
    }

    public BlockPos getRegionCenter() {
        return new BlockPos(this.getCenterX() * 16, 128, this.getCenterZ() * 16);
    }

    public int getChunkLengthX() {
        return maxX() - minX() + 1;
    }

    public int getChunkLengthZ() {
        return maxZ() - minZ() + 1;
    }

    private ImmutableSet<Long> calculateChunkLongs() {
        return this.stream()
            .map(pos -> ChunkPos.asLong(pos.x, pos.z))
            .collect(ImmutableSet.toImmutableSet());
    }

    class ChunkPosIterator implements Iterator<ChunkPos> {
        int index = 0;

        @Override
        public boolean hasNext() {
            return index < dimension() * dimension();
        }

        @Override
        public ChunkPos next() {
            if (!hasNext()) throw new NoSuchElementException();

            int x = (index % dimension()) + minX();
            int z = (index / dimension()) + minZ();
            index++;
            return new ChunkPos(x, z);
        }
    }
}
