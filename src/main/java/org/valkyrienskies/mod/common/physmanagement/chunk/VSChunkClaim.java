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

import java.util.stream.Stream;
import lombok.Getter;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.ChunkPos;

/**
 * This stores the chunk claims for a PhysicsObject; not the chunks themselves
 *
 * @author thebest108
 */
public class VSChunkClaim {

    public final boolean[][] chunkOccupiedInLocal;
    @Getter
    private final int centerX;
    @Getter
    private final int centerZ;
    @Getter
    private final int radius;

    // For Kryo
    private VSChunkClaim() {
        radius = 0;
        centerX = 0;
        centerZ = 0;
        chunkOccupiedInLocal = null;
    }

    public VSChunkClaim(int x, int z, int size) {
        this.centerX = x;
        this.centerZ = z;
        this.radius = size;
        this.chunkOccupiedInLocal = new boolean[(radius() * 2) + 1][(radius() * 2) + 1];
    }

    public VSChunkClaim(NBTTagCompound readFrom) {
        this(readFrom.getInteger("centerX"), readFrom.getInteger("centerZ"),
            readFrom.getInteger("radius"));
    }

    public void writeToNBT(NBTTagCompound toSave) {
        toSave.setInteger("centerX", centerX());
        toSave.setInteger("centerZ", centerZ());
        toSave.setInteger("radius", radius());
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

    @Override
    public String toString() {
        return centerX() + ":" + centerZ() + ":" + radius();
    }

    @Override
    public boolean equals(Object o) {
        if (o instanceof VSChunkClaim) {
            VSChunkClaim other = (VSChunkClaim) o;
            return other.centerX() == centerX() && other.centerZ() == centerZ()
                && other.radius() == radius();
        }
        return false;
    }

    /**
     * @return A stream of the {@link ChunkPos} of every chunk inside of this claim.
     */
    public Stream<ChunkPos> stream() {
        Stream.Builder<ChunkPos> builder = Stream.builder();

        for (int x = minX(); x <= maxX(); x++) {
            for (int z = minZ(); z <= maxZ(); z++) {
                builder.add(new ChunkPos(x, z));
            }
        }

        return builder.build();
    }

    /**
     * @return the maxX
     */
    public int maxX() {
        return centerX() + radius();
    }

    /**
     * @return the maxZ
     */
    public int maxZ() {
        return centerZ() + radius();
    }

    /**
     * @return the minZ
     */
    public int minZ() {
        return centerZ() - radius();
    }

    /**
     * @return the minX
     */
    public int minX() {
        return centerX() - radius();
    }

    public BlockPos regionCenter() {
        return new BlockPos(this.centerX() * 16, 128, this.centerZ() * 16);
    }

    public int chunkLengthX() {
        return maxX() - minX() + 1;
    }

    public int chunkLengthZ() {
        return maxZ() - minZ() + 1;
    }
}
