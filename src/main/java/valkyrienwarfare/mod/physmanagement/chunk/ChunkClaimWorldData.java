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

package valkyrienwarfare.mod.physmanagement.chunk;

import gnu.trove.list.TIntList;
import gnu.trove.list.array.TIntArrayList;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.world.World;
import net.minecraft.world.storage.MapStorage;
import net.minecraft.world.storage.WorldSavedData;

public class ChunkClaimWorldData extends WorldSavedData {

    private static final String CHUNK_POS_DATA_KEY = "ChunkKeys";
    private final TIntList avalibleChunkKeys;
    private int chunkKey;

    public ChunkClaimWorldData(String key) {
        super(key);
        this.avalibleChunkKeys = new TIntArrayList();
        this.markDirty();
    }

    public ChunkClaimWorldData() {
        super(CHUNK_POS_DATA_KEY);
        this.avalibleChunkKeys = new TIntArrayList();
        this.markDirty();
    }

    public static ChunkClaimWorldData get(World world) {
        MapStorage storage = world.getPerWorldStorage();
        ChunkClaimWorldData data = (ChunkClaimWorldData) storage.getOrLoadData(ChunkClaimWorldData.class, CHUNK_POS_DATA_KEY);
        if (data == null) {
            System.err.println("Had to create a null ChunkKeysWorldData; could this be corruption?");
            data = new ChunkClaimWorldData();
            world.setData(CHUNK_POS_DATA_KEY, data);
        }
        return data;
    }

    @Override
    public void readFromNBT(NBTTagCompound nbt) {
        setChunkKey(nbt.getInteger("chunkKey"));
        int[] array = nbt.getIntArray("avalibleChunkKeys");
        for (int i : array) {
            getAvalibleChunkKeys().add(i);
        }
    }

    @Override
    public NBTTagCompound writeToNBT(NBTTagCompound nbt) {
        nbt.setInteger("chunkKey", getChunkKey());
        int[] array = new int[getAvalibleChunkKeys().size()];
        for (int i = 0; i < getAvalibleChunkKeys().size(); i++) {
            array[i] = getAvalibleChunkKeys().get(i);
        }
        nbt.setIntArray("avalibleChunkKeys", array);
        return nbt;
    }

    /**
     * @return the avalibleChunkKeys
     */
    public TIntList getAvalibleChunkKeys() {
        return avalibleChunkKeys;
    }

    /**
     * @return the chunkKey
     */
    public int getChunkKey() {
        return chunkKey;
    }

    /**
     * @param chunkKey the chunkKey to set
     */
    public void setChunkKey(int chunkKey) {
        this.chunkKey = chunkKey;
    }

}
